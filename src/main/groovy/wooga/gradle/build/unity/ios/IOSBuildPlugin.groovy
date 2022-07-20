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

import com.wooga.security.Domain
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider
import wooga.gradle.build.unity.ios.internal.DefaultIOSBuildPluginExtension
import wooga.gradle.build.unity.ios.tasks.ImportCodeSigningIdentities
import wooga.gradle.build.unity.ios.tasks.PodInstallTask
import wooga.gradle.fastlane.FastlanePlugin
import wooga.gradle.fastlane.FastlanePluginExtension
import wooga.gradle.fastlane.tasks.PilotUpload
import wooga.gradle.fastlane.tasks.SighRenew
import wooga.gradle.fastlane.tasks.SighRenewBatch
import wooga.gradle.macOS.security.tasks.*
import wooga.gradle.xcodebuild.XcodeBuildPlugin
import wooga.gradle.xcodebuild.tasks.ArchiveDebugSymbols
import wooga.gradle.xcodebuild.tasks.ExportArchive
import wooga.gradle.xcodebuild.tasks.XcodeArchive

class IOSBuildPlugin implements Plugin<Project> {

    private static final Logger LOG = Logging.getLogger(IOSBuildPlugin.class)
    static final String EXTENSION_NAME = "iosBuild"
    static final String PUBLISH_LIFECYCLE_TASK_NAME = "publish"

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

        extension.exportOptionsPlist.convention(project.layout.projectDirectory.file("exportOptions.plist"))
        extension.teamId.convention(extension.exportOptions.map({ it.teamID }))
        extension.signingIdentities.convention(extension.exportOptions.map({ it.signingCertificate ? [it.signingCertificate] : [] }).orElse(project.provider({ new ArrayList<String>() })))
        extension.adhoc.convention(extension.exportOptions.map({ it.method == 'ad-hoc' }).orElse(false))
        extension.appIdentifier.convention(extension.exportOptions.map({ it.distributionBundleIdentifier }))

        extension.preferWorkspace.convention(true)
        extension.xcodeProjectDirectory.convention(project.layout.projectDirectory)
        extension.projectBaseName.convention("Unity-iPhone")
        extension.xcodeProjectPath.convention(extension.xcodeProjectDirectory.dir(extension.xcodeProjectFileName))
        extension.xcodeWorkspacePath.convention(extension.xcodeProjectDirectory.dir(extension.xcodeWorkspaceFileName))

        //register some defaults
        project.tasks.withType(XcodeArchive.class, new Action<XcodeArchive>() {
            @Override
            void execute(XcodeArchive task) {
                task.projectPath.convention(extension.projectPath)
                task.clean(false)
                task.scheme.set(extension.getScheme())
                task.configuration.set(extension.getConfiguration())
                task.teamId.set(extension.getTeamId())
            }
        })

        project.tasks.withType(ExportArchive.class, new Action<ExportArchive>() {
            @Override
            void execute(ExportArchive task) {
                task.exportOptionsPlist.convention(extension.finalExportOptionsPlist)
            }
        })

        project.tasks.withType(SecurityCreateKeychain.class, new Action<SecurityCreateKeychain>() {
            @Override
            void execute(SecurityCreateKeychain task) {
                task.baseName.convention("build")
                task.extension.convention("keychain")
                task.password.convention(extension.keychainPassword)
                task.lockKeychainAfterTimeout.convention(-1)
                task.lockKeychainWhenSleep.convention(true)
                task.destinationDir.convention(project.layout.buildDirectory.dir("sign/keychains"))
            }
        })

        project.tasks.withType(ImportCodeSigningIdentities.class, new Action<ImportCodeSigningIdentities>() {
            @Override
            void execute(ImportCodeSigningIdentities task) {
                task.baseName.convention("build")
                task.extension.convention("keychain")
                task.password.convention(extension.keychainPassword)
                task.destinationDir.convention(project.layout.buildDirectory.dir("sign/keychains"))
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

                task.teamId.convention(extension.getTeamId())
                task.appIdentifier.convention(extension.getAppIdentifier())
                task.destinationDir.convention(project.layout.dir(project.provider({ task.getTemporaryDir() })))
                task.provisioningName.convention(extension.getProvisioningName())
                task.adhoc.convention(extension.adhoc)
                task.fileName.convention(extension.appIdentifier.map({ "signing${it}.mobileprovision".toString() }).orElse("signing.mobileprovision"))
            }
        })

        project.tasks.withType(PilotUpload.class, new Action<PilotUpload>() {
            @Override
            void execute(PilotUpload task) {
                task.username.convention(project.provider({
                    if (extension.fastlaneCredentials.username) {
                        return extension.fastlaneCredentials.username
                    }
                    null
                }).orElse(fastlaneExtension.username))

                task.password.set(project.provider({
                    if (extension.fastlaneCredentials.password) {
                        return extension.fastlaneCredentials.password
                    }
                    null
                }).orElse(fastlaneExtension.getPassword()))

                task.devPortalTeamId.convention(extension.getTeamId())
                task.appIdentifier.convention(extension.getAppIdentifier())
            }
        })

        project.tasks.withType(ImportCodeSigningIdentities.class, new Action<ImportCodeSigningIdentities>() {
            @Override
            void execute(ImportCodeSigningIdentities task) {
                task.applicationAccessPaths.convention(["/usr/bin/codesign"])
            }
        })

        generateBuildTasks(project, extension)
    }

    void generateBuildTasks(final Project project, IOSBuildPluginExtension extension) {
        def tasks = project.tasks

        def createKeychain = tasks.register("createKeychain", SecurityCreateKeychain)

        TaskProvider<ImportCodeSigningIdentities> buildKeychain = tasks.register("importCodeSigningIdentities", ImportCodeSigningIdentities) {
            it.inputKeychain.set(createKeychain.flatMap({ it.keychain }))
            it.signingIdentities.convention(extension.signingIdentities)
            it.passphrase.convention(extension.codeSigningIdentityFilePassphrase)
            it.p12.convention(extension.codeSigningIdentityFile)
            dependsOn(createKeychain)
        }

        def unlockKeychain = tasks.register("unlockKeychain", SecurityUnlockKeychain) {
            it.dependsOn(buildKeychain, buildKeychain)
            it.password.set(createKeychain.flatMap({ it.password }))
            it.keychain.set(buildKeychain.flatMap({ it.keychain }))
        }

        def lockKeychain = tasks.register("lockKeychain", SecurityLockKeychain) {
            it.dependsOn(buildKeychain)
            it.keychain(buildKeychain.flatMap({ it.keychain }).map({ it.asFile }))
        }

        def resetKeychains = tasks.register("resetKeychains", SecurityResetKeychainSearchList)

        def addKeychain = tasks.register("addKeychain", SecuritySetKeychainSearchList) {
            it.dependsOn(buildKeychain)
            it.domain.set(Domain.user)
            it.action = SecuritySetKeychainSearchList.Action.add
            it.keychain(buildKeychain.flatMap({ it.keychain }).map({ it.asFile }))
            dependsOn(resetKeychains)
        }

        def removeKeychain = tasks.register("removeKeychain", SecuritySetKeychainSearchList) {
            it.dependsOn(buildKeychain)
            it.domain.set(Domain.user)
            it.action = SecuritySetKeychainSearchList.Action.remove
            it.keychain(buildKeychain.flatMap({ it.keychain }).map({ it.asFile }))
        }

        buildKeychain.configure({it.finalizedBy(removeKeychain, lockKeychain)})

        def shutdownHook = new Thread({
            System.err.println("shutdown hook called")
            System.err.flush()
            if (addKeychain.get().didWork) {
                System.err.println("task ${addKeychain.get().name} did run. Execute ${removeKeychain.get().name} shutdown action")
                removeKeychain.get().shutdown()
            } else {
                System.err.println("no actions to be executed")
                System.err.flush()
            }
            System.err.flush()
        })

        addKeychain.configure({ Task t ->
            t.doLast {
                t.logger.info("Add shutdown hook")
                Runtime.getRuntime().addShutdownHook(shutdownHook)
            }
        })

        removeKeychain.configure({ Task t ->
            t.doLast {
                t.logger.info("Remove shutdown hook")
                Runtime.getRuntime().removeShutdownHook(shutdownHook)
            }
        })

        def importProvisioningProfiles = tasks.register("importProvisioningProfiles", SighRenewBatch) {
            it.profiles.set(extension.exportOptions.map({ it.getProvisioningProfiles() }))
            it.dependsOn addKeychain, buildKeychain, unlockKeychain
            it.finalizedBy removeKeychain, lockKeychain
        }

        TaskProvider<PodInstallTask> podInstall = tasks.register("podInstall", PodInstallTask) {
            it.projectDirectory.set(extension.xcodeProjectDirectory)
            it.xcodeWorkspaceFileName.set(extension.xcodeWorkspaceFileName)
            it.xcodeProjectFileName.set(extension.xcodeProjectFileName)
        }

        def xcodeArchive = tasks.register("xcodeArchive", XcodeArchive) {
            it.dependsOn addKeychain, unlockKeychain, importProvisioningProfiles, podInstall, buildKeychain
            it.projectPath.set(extension.projectPath)
            it.buildKeychain.set(buildKeychain.flatMap({ it.keychain }))
        }

        def xcodeExport = tasks.named(xcodeArchive.name + XcodeBuildPlugin.EXPORT_ARCHIVE_TASK_POSTFIX, ExportArchive)
        def archiveDSYM = tasks.named(xcodeArchive.name + XcodeBuildPlugin.ARCHIVE_DEBUG_SYMBOLS_TASK_POSTFIX, ArchiveDebugSymbols)

        def publishTestFlight = tasks.register("publishTestFlight", PilotUpload) {
            it.ipa.set(xcodeExport.flatMap({ it.outputPath }))
            it.group = PublishingPlugin.PUBLISH_TASK_GROUP
            it.description = "Upload binary to TestFlightApp"
        }

        tasks.named(PUBLISH_LIFECYCLE_TASK_NAME, {task ->
            if (extension.publishToTestFlight.present && extension.publishToTestFlight.get()) {
                task.dependsOn(publishTestFlight)
            }
        })

        removeKeychain.configure({ it.mustRunAfter([xcodeArchive, xcodeExport]) })
        lockKeychain.configure({ it.mustRunAfter([xcodeArchive, xcodeExport]) })

        def collectOutputs = tasks.register("collectOutputs", Sync) {
            it.from(xcodeExport, archiveDSYM)
            into(project.file("${project.buildDir}/outputs"))
        }

        project.artifacts {
            archives(xcodeExport.flatMap({ it.outputPath })) {
                it.type = "iOS application archive"
            }
            archives(archiveDSYM.flatMap({ it.archiveFile })) {
                it.type = "iOS application symbols"
            }
        }

        archiveDSYM.configure({ it.mustRunAfter(xcodeExport) })
        tasks.named(BasePlugin.ASSEMBLE_TASK_NAME).configure({ it.dependsOn(xcodeExport, archiveDSYM, collectOutputs) })
    }
}
