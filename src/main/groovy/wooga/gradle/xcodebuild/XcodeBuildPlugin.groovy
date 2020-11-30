/*
 * Copyright 2018-2020 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *
 */

package wooga.gradle.xcodebuild

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Transformer
import org.gradle.api.artifacts.ConfigurablePublishArtifact
import org.gradle.api.artifacts.dsl.ArtifactHandler
import org.gradle.api.file.Directory
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Provider
import wooga.gradle.build.unity.ios.internal.utils.PropertyUtils
import wooga.gradle.xcodebuild.internal.DefaultXcodeBuildPluginExtension
import wooga.gradle.xcodebuild.internal.PropertyLookup
import wooga.gradle.xcodebuild.tasks.*

import static wooga.gradle.xcodebuild.XcodeBuildPluginConsts.*

class XcodeBuildPlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = "xcodebuild"
    static final String ARCHIVE_DEBUG_SYMBOLS_TASK_POSTFIX = "DSYMs"
    static final String EXPORT_ARCHIVE_TASK_POSTFIX = "Export"
    private Project project

    @Override
    void apply(Project project) {
        this.project = project
        def extension = project.extensions.create(XcodeBuildPluginExtension, EXTENSION_NAME, DefaultXcodeBuildPluginExtension, project)
        def tasks = project.tasks

        project.pluginManager.apply(BasePlugin.class)

        extension.logsDir.set(project.layout.buildDirectory.dir(lookupValueInEnvAndPropertiesProvider(LOGS_DIR_LOOKUP)))
        extension.derivedDataPath.set(project.layout.buildDirectory.dir(lookupValueInEnvAndPropertiesProvider(DERIVED_DATA_PATH_LOOKUP)))
        extension.xarchivesDir.set(project.layout.buildDirectory.dir(lookupValueInEnvAndPropertiesProvider(XARCHIVES_DIR_LOOKUP)))
        extension.debugSymbolsDir.set(project.layout.buildDirectory.dir(lookupValueInEnvAndPropertiesProvider(DEBUG_SYMBOLS_DIR_LOOKUP)))
        extension.consoleSettings.set(ConsoleSettings.fromGradleOutput(project.gradle.startParameter.consoleOutput))

        def archives = project.configurations.maybeCreate('archives')
        project.configurations['default'].extendsFrom(archives)

        project.tasks.withType(XcodeArchive.class, new Action<XcodeArchive>() {
            @Override
            void execute(XcodeArchive task) {

                task.group = BasePlugin.BUILD_GROUP
                task.description = "export .xcarchive from given Xcode project"

                task.extension.set("xcarchive")
                task.derivedDataPath.set(extension.derivedDataPath.dir(task.name))

                //each XcodeArchive task creates an archive symbols task
                def archiveDysim = tasks.create(task.name + ARCHIVE_DEBUG_SYMBOLS_TASK_POSTFIX, ArchiveDebugSymbols) {
                    it.dependsOn(task)
                    it.from(task.xcArchivePath.map(new Transformer<Directory, Directory>() {
                        @Override
                        Directory transform(Directory archivePath) {
                            archivePath.dir("dSYMs")
                        }
                    })
                    )
                }

                //each XcodeArchive task creates an exportArchive task
                def exportArchive = tasks.create(task.name + EXPORT_ARCHIVE_TASK_POSTFIX, ExportArchive) {
                    it.dependsOn(task)
                    it.xcArchivePath(task.xcArchivePath)
                }

                //add artifacts as publish artifacts
                project.artifacts(new Action<ArtifactHandler>() {
                    @Override
                    void execute(ArtifactHandler artifactHandler) {
                        artifactHandler.add("archives", exportArchive.publishArtifact, new Action<ConfigurablePublishArtifact>() {
                            @Override
                            void execute(ConfigurablePublishArtifact configurablePublishArtifact) {
                                configurablePublishArtifact.type = "iOS application archive"
                            }
                        })

                        artifactHandler.add("archives", archiveDysim, new Action<ConfigurablePublishArtifact>() {
                            @Override
                            void execute(ConfigurablePublishArtifact configurablePublishArtifact) {
                                configurablePublishArtifact.type = "iOS application symbols"
                            }
                        })
                    }
                })
            }
        })

        project.tasks.withType(ExportArchive.class, new Action<ExportArchive>() {
            @Override
            void execute(ExportArchive task) {
                task.group = BasePlugin.BUILD_GROUP
                task.description = "export ipa file from given .xcarchive"

                task.extension.set("ipa")
            }
        })

        project.tasks.withType(AbstractXcodeArchiveTask.class, new Action<AbstractXcodeArchiveTask>() {
            @Override
            void execute(AbstractXcodeArchiveTask task) {
                task.version.set(project.provider({ project.version.toString() }))
                task.baseName.set(project.name)
                task.destinationDir.set(extension.xarchivesDir)
            }
        })

        project.tasks.withType(AbstractXcodeTask.class, new Action<AbstractXcodeTask>() {
            @Override
            void execute(AbstractXcodeTask task) {
                task.logFile.set(extension.logsDir.file("${task.name}.log"))
                task.consoleSettings.set(extension.consoleSettings)
            }
        })

        project.tasks.withType(ArchiveDebugSymbols.class, new Action<ArchiveDebugSymbols>() {
            @Override
            void execute(ArchiveDebugSymbols task) {
                task.group = BasePlugin.BUILD_GROUP
                task.description = "export debug symbols (dSYMs) from given .xcarchive"

                def conventionMapping = task.getConventionMapping()
                conventionMapping.map("version", { PropertyUtils.convertToString(project.version) })
                conventionMapping.map("destinationDir", {
                    extension.debugSymbolsDir.get().asFile
                })
                conventionMapping.map("baseName", { project.name })
                conventionMapping.map("classifier", { "dSYM" })
                conventionMapping.map("extension", { "zip" })
            }
        })
    }

    private Provider<String> lookupValueInEnvAndPropertiesProvider(PropertyLookup lookup) {
        lookupValueInEnvAndPropertiesProvider(lookup.env, lookup.property, lookup.defaultValue)
    }

    private Provider<String> lookupValueInEnvAndPropertiesProvider(String env, String property, String defaultValue = null) {
        project.provider({
            lookupValueInEnvAndProperties(env, property, defaultValue)
        })
    }

    protected String lookupValueInEnvAndProperties(String env, String property, String defaultValue = null) {
        System.getenv().get(env) ?:
                project.properties.getOrDefault(property, defaultValue)
    }
}
