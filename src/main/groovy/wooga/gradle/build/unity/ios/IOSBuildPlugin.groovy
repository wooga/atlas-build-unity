/*
 * Copyright 2018 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.build.unity.ios

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.Sync
import org.gradle.util.GUtil
import wooga.gradle.build.unity.ios.internal.DefaultIOSBuildPluginExtension
import wooga.gradle.build.unity.ios.internal.utils.PropertyUtils
import wooga.gradle.build.unity.ios.tasks.ArchiveDsymTask
import wooga.gradle.build.unity.ios.tasks.ImportProvisioningProfile
import wooga.gradle.build.unity.ios.tasks.KeychainTask
import wooga.gradle.build.unity.ios.tasks.ListKeychainTask
import wooga.gradle.build.unity.ios.tasks.LockKeychainTask
import wooga.gradle.build.unity.ios.tasks.PublishTestFlight
import wooga.gradle.build.unity.ios.tasks.XCodeArchiveTask
import wooga.gradle.build.unity.ios.tasks.XCodeExportTask

class IOSBuildPlugin implements Plugin<Project> {

    private static final Logger LOG = Logging.getLogger(IOSBuildPlugin.class)
    static final String EXTENSION_NAME = "iosBuild"

    @Override
    void apply(Project project) {
        //check if system is running mac os
        String osName = System.getProperty("os.name").toLowerCase()
        if (!osName.contains('mac os')) {
            LOG.warn("This plugin is only supported on Mac OS systems.")
            return
        }

        project.pluginManager.apply(BasePlugin.class)
        project.pluginManager.apply(PublishingPlugin.class)
        def extension = project.getExtensions().create(IOSBuildPluginExtension, EXTENSION_NAME, DefaultIOSBuildPluginExtension.class)

        //register some defaults
        project.tasks.withType(XCodeArchiveTask.class, new Action<XCodeArchiveTask>() {
            @Override
            void execute(XCodeArchiveTask task) {
                def conventionMapping = task.getConventionMapping()
                conventionMapping.map("version", { PropertyUtils.convertToString(project.version) })
                conventionMapping.map("clean", { false })
                conventionMapping.map("destinationDir", {
                    project.file("${project.buildDir}/archives")
                })
                conventionMapping.map("baseName", { project.name })
                conventionMapping.map("extension", { "xcarchive" })
                conventionMapping.map("scheme", { extension.getScheme() })
                conventionMapping.map("configuration", { extension.getConfiguration() })
                conventionMapping.map("teamId", { extension.getTeamId() })
            }
        })

        project.tasks.withType(ArchiveDsymTask.class, new Action<ArchiveDsymTask>() {
            @Override
            void execute(ArchiveDsymTask task) {
                def conventionMapping = task.getConventionMapping()
                conventionMapping.map("version", { PropertyUtils.convertToString(project.version) })
                conventionMapping.map("destinationDir", {
                    project.file("${project.buildDir}/symbols")
                })
                conventionMapping.map("baseName", { project.name })
                conventionMapping.map("classifier", { "dSYM" })
                conventionMapping.map("extension", { "zip" })
            }
        })

        project.tasks.withType(XCodeExportTask.class, new Action<XCodeExportTask>() {
            @Override
            void execute(XCodeExportTask task) {
                def conventionMapping = task.getConventionMapping()
                conventionMapping.map("version", { PropertyUtils.convertToString(project.version) })
                conventionMapping.map("destinationDir", {
                    project.file("${project.buildDir}/ipas")
                })
                conventionMapping.map("baseName", { project.name })
                conventionMapping.map("extension", { "ipa" })
            }
        })

        project.tasks.withType(KeychainTask.class, new Action<KeychainTask>() {
            @Override
            void execute(KeychainTask task) {
                def conventionMapping = task.getConventionMapping()
                conventionMapping.map("baseName", { "build" })
                conventionMapping.map("extension", { "keychain" })
                conventionMapping.map("password", { extension.getKeychainPassword() })
                conventionMapping.map("certificatePassword", { extension.getCertificatePassphrase() })
                conventionMapping.map("destinationDir", {
                    project.file("${project.buildDir}/sign/keychains")
                })
            }
        })

        project.tasks.withType(ImportProvisioningProfile.class, new Action<ImportProvisioningProfile>() {
            @Override
            void execute(ImportProvisioningProfile task) {
                def conventionMapping = task.getConventionMapping()
                conventionMapping.map("username", { extension.fastlaneCredentials.username })
                conventionMapping.map("password", { extension.fastlaneCredentials.password })
                conventionMapping.map("teamId", { extension.getTeamId() })
                conventionMapping.map("appIdentifier", { extension.getAppIdentifier() })
                conventionMapping.map("destinationDir", { task.getTemporaryDir() })
                conventionMapping.map("provisioningName", { extension.getProvisioningName() })
                conventionMapping.map("adhoc", { extension.getAdhoc() })
                conventionMapping.map("profileName", { 'signing.mobileprovision' })
            }
        })

        project.tasks.withType(PublishTestFlight.class, new Action<PublishTestFlight>() {
            @Override
            void execute(PublishTestFlight task) {
                def conventionMapping = task.getConventionMapping()
                conventionMapping.map("username", { extension.fastlaneCredentials.username })
                conventionMapping.map("password", { extension.fastlaneCredentials.password })
                conventionMapping.map("devPortalTeamId", { extension.getTeamId() })
                conventionMapping.map("appIdentifier", { extension.getAppIdentifier() })
                conventionMapping.map("ipa", { extension.getAppIdentifier() })
            }
        })

        def projects = project.fileTree(project.projectDir) { it.include("*.xcodeproj/project.pbxproj") }.files
        projects.each { File xcodeProject ->
            def base = xcodeProject.parentFile
            def taskNameBase = base.name.replace('.xcodeproj', '').toLowerCase().replaceAll(/[-_.]/, '')
            if (projects.size() == 1) {
                taskNameBase = ""
            }
            generateBuildTasks(taskNameBase, project, base, extension)
        }
    }

    private static String maybeBaseName(String baseName, String taskName) {
        if (GUtil.isTrue(taskName)) {
            if (GUtil.isTrue(baseName)) {
                return baseName + taskName.capitalize()
            } else {
                return taskName
            }
        }
        return ""
    }

    void generateBuildTasks(final String baseName, final Project project, File xcodeProject, IOSBuildPluginExtension extension) {
        def tasks = project.tasks
        def buildKeychain = tasks.create(maybeBaseName(baseName, "buildKeychain"), KeychainTask) {
            it.baseName = maybeBaseName(baseName, "build")
            it.certificates = project.fileTree(project.projectDir) { it.include("*.p12") }
        }

        def unlockKeychain = tasks.create(maybeBaseName(baseName, "unlockKeychain"), LockKeychainTask) {
            it.lockAction = LockKeychainTask.LockAction.unlock
            it.password = { buildKeychain.getPassword() }
            it.keychain = buildKeychain
        }

        def lockKeychain = tasks.create(maybeBaseName(baseName, "lockKeychain"), LockKeychainTask) {
            it.lockAction = LockKeychainTask.LockAction.lock
            it.password = { buildKeychain.getPassword() }
            it.keychain = buildKeychain
        }

        def addKeychain = tasks.create(maybeBaseName(baseName, "addKeychain"), ListKeychainTask) {
            it.action = ListKeychainTask.Action.add
            it.keychain buildKeychain
        }

        def removeKeychain = tasks.create(maybeBaseName(baseName, "removeKeychain"), ListKeychainTask) {
            it.action = ListKeychainTask.Action.remove
            it.keychain buildKeychain
        }

        def importProvisioningProfiles = tasks.create(maybeBaseName(baseName, "importProvisioningProfiles"), ImportProvisioningProfile) {
            it.dependsOn addKeychain, unlockKeychain
            it.finalizedBy removeKeychain, lockKeychain
            it.profileName = "${maybeBaseName(baseName, 'signing')}.mobileprovision"
        }

        def xcodeArchive = tasks.create(maybeBaseName(baseName, "xcodeArchive"), XCodeArchiveTask) {
            it.dependsOn addKeychain, unlockKeychain

            it.provisioningProfile = importProvisioningProfiles
            it.projectPath = xcodeProject
            it.buildKeychain = buildKeychain
            it.destinationDir = project.file("${project.buildDir}/archives")
        }

        def xcodeExport = tasks.create(maybeBaseName(baseName, "xcodeExport"), XCodeExportTask) {
            it.exportOptionsPlist project.file("exportOptions.plist")
            it.xcarchivePath xcodeArchive
        }

        def publishTestFlight = tasks.create(maybeBaseName(baseName, "publishTestFlight"), PublishTestFlight) {
            it.ipa xcodeExport
            it.group = PublishingPlugin.PUBLISH_TASK_GROUP
            it.description = "Upload binary to TestFlightApp"
        }

        project.afterEvaluate(new Action<Project>() {
            @Override
            void execute(Project _) {
                if(extension.publishToTestFlight) {
                    def lifecyclePublishTask = tasks.getByName(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME)
                    lifecyclePublishTask.dependsOn(publishTestFlight)
                }
            }
        })

        removeKeychain.mustRunAfter([xcodeArchive, xcodeExport])
        lockKeychain.mustRunAfter([xcodeArchive, xcodeExport])

        def archiveDSYM = tasks.create(maybeBaseName(baseName, "archiveDSYM"), ArchiveDsymTask) {
            it.dependsOn xcodeArchive
            it.from({project.file("${xcodeArchive.getArchivePath()}/dSYMs")})
        }

        def collectOutputs = tasks.create(maybeBaseName(baseName, "collectOutputs"), Sync) {
            it.from(xcodeExport, archiveDSYM)
            into(project.file("${project.buildDir}/outputs"))
        }

        project.artifacts {
            archives(xcodeExport) {
                it.type = "iOS application archive"
            }
            archives(archiveDSYM) {
                it.type = "iOS application symbols"
            }
        }

        archiveDSYM.mustRunAfter xcodeExport // not to spend time archiving if export fails
        project.tasks.getByName(BasePlugin.ASSEMBLE_TASK_NAME).dependsOn xcodeExport, archiveDSYM, collectOutputs
    }
}
