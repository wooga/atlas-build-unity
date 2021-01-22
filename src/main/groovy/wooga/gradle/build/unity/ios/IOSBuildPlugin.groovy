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
import wooga.gradle.build.unity.ios.tasks.*
import wooga.gradle.fastlane.FastlanePlugin
import wooga.gradle.fastlane.FastlanePluginExtension
import wooga.gradle.fastlane.tasks.PilotUpload
import wooga.gradle.fastlane.tasks.SighRenew
import wooga.gradle.xcodebuild.XcodeBuildPlugin
import wooga.gradle.xcodebuild.tasks.ArchiveDebugSymbols
import wooga.gradle.xcodebuild.tasks.ExportArchive
import wooga.gradle.xcodebuild.tasks.XcodeArchive

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
        project.pluginManager.apply(XcodeBuildPlugin.class)
        project.pluginManager.apply(FastlanePlugin.class)
        project.pluginManager.apply(PublishingPlugin.class)

        def extension = project.getExtensions().create(IOSBuildPluginExtension, EXTENSION_NAME, DefaultIOSBuildPluginExtension.class)
        def fastlaneExtension = project.getExtensions().getByType(FastlanePluginExtension)

        //register some defaults
        project.tasks.withType(XcodeArchive.class, new Action<XcodeArchive>() {
            @Override
            void execute(XcodeArchive task) {
                task.clean(false)
                task.scheme.set(project.provider({ extension.getScheme() }))
                task.configuration.set(project.provider({ extension.getConfiguration() }))
                task.teamId.set(project.provider({ extension.getTeamId() }))
            }
        })

        project.tasks.withType(ExportArchive.class, new Action<ExportArchive>() {
            @Override
            void execute(ExportArchive task) {
                task.exportOptionsPlist.set(project.file("exportOptions.plist"))
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

        project.tasks.withType(SighRenew.class, new Action<SighRenew>() {
            @Override
            void execute(SighRenew task) {
                task.username.set(project.provider({
                    if (extension.fastlaneCredentials.username) {
                        return extension.fastlaneCredentials.username
                    }
                    fastlaneExtension.username.getOrNull()
                }))

                task.password.set(project.provider({
                    if (extension.fastlaneCredentials.password) {
                        return extension.fastlaneCredentials.password
                    }
                    fastlaneExtension.password.getOrNull()
                }))

                task.teamId.set(project.provider({ extension.getTeamId() }))
                task.appIdentifier.set(project.provider({ extension.getAppIdentifier() }))
                task.destinationDir.set(task.temporaryDir)
                task.provisioningName.set(project.provider({ extension.getProvisioningName() }))
                task.adhoc.set(project.provider({ extension.getAdhoc() }))
                task.fileName.set('signing.mobileprovision')
            }
        })

        project.tasks.withType(PilotUpload.class, new Action<PilotUpload>() {
            @Override
            void execute(PilotUpload task) {
                task.username.set(project.provider({
                    if (extension.fastlaneCredentials.username) {
                        return extension.fastlaneCredentials.username
                    }
                    fastlaneExtension.username.getOrNull()
                }))

                task.password.set(project.provider({
                    if (extension.fastlaneCredentials.password) {
                        return extension.fastlaneCredentials.password
                    }
                    fastlaneExtension.password.getOrNull()
                }))

                task.devPortalTeamId.set(project.provider({ extension.getTeamId() }))
                task.appIdentifier.set(project.provider({ extension.getAppIdentifier() }))
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

        def resetKeychains = tasks.create(maybeBaseName(baseName, "resetKeychains"), ListKeychainTask) {
            it.action = ListKeychainTask.Action.reset
            it.keychain buildKeychain
        }

        def addKeychain = tasks.create(maybeBaseName(baseName, "addKeychain"), ListKeychainTask) {
            it.action = ListKeychainTask.Action.add
            it.keychain buildKeychain
            dependsOn(resetKeychains)
        }

        def removeKeychain = tasks.create(maybeBaseName(baseName, "removeKeychain"), ListKeychainTask) {
            it.action = ListKeychainTask.Action.remove
            it.keychain buildKeychain
        }

        def importProvisioningProfiles = tasks.create(maybeBaseName(baseName, "importProvisioningProfiles"), SighRenew) {
            it.dependsOn addKeychain, unlockKeychain
            it.finalizedBy removeKeychain, lockKeychain
            it.fileName.set("${maybeBaseName(baseName, 'signing')}.mobileprovision".toString() )
        }

        PodInstallTask podInstall = tasks.create(maybeBaseName(baseName, "podInstall"), PodInstallTask) {
            it.projectPath = xcodeProject
        }

        def xcodeArchive = tasks.create(maybeBaseName(baseName, "xcodeArchive"), XcodeArchive) {
            it.dependsOn addKeychain, unlockKeychain, podInstall, importProvisioningProfiles
            it.projectPath.set(project.provider({
                def d = project.layout.buildDirectory.get()
                if (podInstall.workspace.exists()) {
                    return d.dir(podInstall.workspace.path)
                }
                return d.dir(xcodeProject.path)
            }))
            it.buildKeychain = buildKeychain.outputPath
        }

        ExportArchive xcodeExport = tasks.getByName(xcodeArchive.name + XcodeBuildPlugin.EXPORT_ARCHIVE_TASK_POSTFIX) as ExportArchive
        def publishTestFlight = tasks.create(maybeBaseName(baseName, "publishTestFlight"), PilotUpload) {
            it.ipa.set(xcodeExport.outputPath)
            it.group = PublishingPlugin.PUBLISH_TASK_GROUP
            it.description = "Upload binary to TestFlightApp"
        }

        project.afterEvaluate(new Action<Project>() {
            @Override
            void execute(Project _) {
                if (extension.publishToTestFlight) {
                    def lifecyclePublishTask = tasks.getByName(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME)
                    lifecyclePublishTask.dependsOn(publishTestFlight)
                }
            }
        })

        removeKeychain.mustRunAfter([xcodeArchive, xcodeExport])
        lockKeychain.mustRunAfter([xcodeArchive, xcodeExport])

        def archiveDSYM = tasks.getByName(xcodeArchive.name + XcodeBuildPlugin.ARCHIVE_DEBUG_SYMBOLS_TASK_POSTFIX) as ArchiveDebugSymbols

        def collectOutputs = tasks.create(maybeBaseName(baseName, "collectOutputs"), Sync) {
            it.from(xcodeExport, archiveDSYM)
            into(project.file("${project.buildDir}/outputs"))
        }

        project.artifacts {
            archives(xcodeExport.publishArtifact) {
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
