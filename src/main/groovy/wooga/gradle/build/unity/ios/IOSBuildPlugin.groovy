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
import wooga.gradle.build.unity.ios.internal.DefaultIOSBuildPluginExtension
import wooga.gradle.build.unity.ios.tasks.ImportCodeSigningIdentities
import wooga.gradle.build.unity.ios.tasks.PodInstallTask
import wooga.gradle.fastlane.FastlanePlugin
import wooga.gradle.fastlane.FastlanePluginExtension
import wooga.gradle.fastlane.tasks.PilotUpload
import wooga.gradle.fastlane.tasks.SighRenew
import wooga.gradle.macOS.security.tasks.*
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

        extension.exportOptionsPlist.convention(project.layout.projectDirectory.file("exportOptions.plist"))
        extension.teamId.convention(extension.exportOptions.map({ it.teamID }))
        extension.signingIdentities.convention(extension.exportOptions.map({it.signingCertificate ? [it.signingCertificate] : []}).orElse(project.provider({ new ArrayList<String>() })))
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

        def createKeychain = tasks.create("createKeychain", SecurityCreateKeychain)

        ImportCodeSigningIdentities buildKeychain = tasks.create("importCodeSigningIdentities", ImportCodeSigningIdentities) {
            it.inputKeychain.set(createKeychain.getKeychain())
            it.signingIdentities.convention(extension.signingIdentities)
            it.passphrase.convention(extension.codeSigningIdentityFilePassphrase)
            it.p12.convention(extension.codeSigningIdentityFile)
            dependsOn(createKeychain)
        }

        def unlockKeychain = tasks.create("unlockKeychain", SecurityUnlockKeychain) {
            it.dependsOn(buildKeychain, buildKeychain)
            it.password.set(createKeychain.password)
            it.keychain.set(buildKeychain.keychain)
        }

        def lockKeychain = tasks.create("lockKeychain", SecurityLockKeychain) {
            it.dependsOn(buildKeychain)
            it.keychain(buildKeychain.keychain.map({ it.asFile }))
        }

        def resetKeychains = tasks.create("resetKeychains", SecurityResetKeychainSearchList)

        def addKeychain = tasks.create("addKeychain", SecuritySetKeychainSearchList) {
            it.dependsOn(buildKeychain)
            it.action = SecuritySetKeychainSearchList.Action.add
            it.keychain(buildKeychain.keychain.map({ it.asFile }))
            dependsOn(resetKeychains)
        }

        def removeKeychain = tasks.create("removeKeychain", SecuritySetKeychainSearchList) {
            it.dependsOn(buildKeychain)
            it.action = SecuritySetKeychainSearchList.Action.remove
            it.keychain(buildKeychain.keychain.map({ it.asFile }))
        }

        buildKeychain.finalizedBy(removeKeychain, lockKeychain)

        def shutdownHook = new Thread({
            System.err.println("shutdown hook called")
            System.err.flush()
            if (addKeychain.didWork) {
                System.err.println("task ${addKeychain.name} did run. Execute ${removeKeychain.name} shutdown action")
                removeKeychain.shutdown()
            } else {
                System.err.println("no actions to be executed")
                System.err.flush()
            }
            System.err.flush()
        })

        addKeychain.doLast {
            addKeychain.logger.info("Add shutdown hook")
            Runtime.getRuntime().addShutdownHook(shutdownHook)
        }

        removeKeychain.doLast {
            removeKeychain.logger.info("Remove shutdown hook")
            Runtime.getRuntime().removeShutdownHook(shutdownHook)
        }

        def importProvisioningProfiles = tasks.create("importProvisioningProfiles", SighRenew) {
            it.dependsOn addKeychain, buildKeychain, unlockKeychain
            it.finalizedBy removeKeychain, lockKeychain
            it.fileName.set("${'signing'}.mobileprovision".toString())
        }

        PodInstallTask podInstall = tasks.create("podInstall", PodInstallTask) {
            it.projectDirectory.set(extension.xcodeProjectDirectory)
            it.xcodeWorkspaceFileName.set(extension.xcodeWorkspaceFileName)
            it.xcodeProjectFileName.set(extension.xcodeProjectFileName)
        }

        def xcodeArchive = tasks.create("xcodeArchive", XcodeArchive) {
            it.dependsOn addKeychain, unlockKeychain, importProvisioningProfiles, podInstall, buildKeychain
            it.projectPath.set(extension.projectPath)
            it.buildKeychain.set(buildKeychain.keychain)
        }

        ExportArchive xcodeExport = tasks.getByName(xcodeArchive.name + XcodeBuildPlugin.EXPORT_ARCHIVE_TASK_POSTFIX) as ExportArchive
        def publishTestFlight = tasks.create("publishTestFlight", PilotUpload) {
            it.ipa.set(xcodeExport.outputPath)
            it.group = PublishingPlugin.PUBLISH_TASK_GROUP
            it.description = "Upload binary to TestFlightApp"
        }

        project.afterEvaluate(new Action<Project>() {
            @Override
            void execute(Project _) {
                if (extension.publishToTestFlight.getOrElse(false)) {
                    def lifecyclePublishTask = tasks.getByName(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME)
                    lifecyclePublishTask.dependsOn(publishTestFlight)
                }
            }
        })

        removeKeychain.mustRunAfter([xcodeArchive, xcodeExport])
        lockKeychain.mustRunAfter([xcodeArchive, xcodeExport])

        def archiveDSYM = tasks.getByName(xcodeArchive.name + XcodeBuildPlugin.ARCHIVE_DEBUG_SYMBOLS_TASK_POSTFIX) as ArchiveDebugSymbols

        def collectOutputs = tasks.create("collectOutputs", Sync) {
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
