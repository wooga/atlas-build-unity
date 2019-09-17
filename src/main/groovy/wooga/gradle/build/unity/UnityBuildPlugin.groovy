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

package wooga.gradle.build.unity

import org.apache.commons.io.FilenameUtils
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTreeElement
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.StopExecutionException
import org.gradle.language.base.plugins.LifecycleBasePlugin
import wooga.gradle.build.unity.ios.internal.utils.PropertyUtils
import wooga.gradle.unity.UnityPlugin
import wooga.gradle.build.unity.internal.DefaultUnityBuildPluginExtension
import wooga.gradle.build.unity.tasks.GradleBuild
import wooga.gradle.build.unity.tasks.UnityBuildPlayerTask
import wooga.gradle.unity.utils.GenericUnityAsset

class UnityBuildPlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = "unityBuild"

    @Override
    void apply(Project project) {
        project.pluginManager.apply(BasePlugin.class)
        project.pluginManager.apply(PublishingPlugin.class)
        project.pluginManager.apply(UnityPlugin.class)

        def extension = project.extensions.create(UnityBuildPluginExtension, EXTENSION_NAME, DefaultUnityBuildPluginExtension, project)
        def baseLifecycleTaskNames = [LifecycleBasePlugin.ASSEMBLE_TASK_NAME,
                                      LifecycleBasePlugin.CHECK_TASK_NAME,
                                      LifecycleBasePlugin.BUILD_TASK_NAME,
                                      PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME]

        def baseLifecycleTaskGroups = [LifecycleBasePlugin.BUILD_GROUP,
                                       LifecycleBasePlugin.VERIFICATION_GROUP,
                                       LifecycleBasePlugin.BUILD_GROUP,
                                       PublishingPlugin.PUBLISH_TASK_GROUP]

        project.tasks.withType(UnityBuildPlayerTask, new Action<UnityBuildPlayerTask>() {
            @Override
            void execute(UnityBuildPlayerTask task) {
                task.exportMethodName.set(extension.exportMethodName)
                task.toolsVersion.set(extension.toolsVersion)
                task.outputDirectoryBase.set(extension.outputDirectoryBase)
                task.version.set(project.provider({PropertyUtils.convertToString(project.version)}))
                task.inputFiles.from({

                    def assetsDir = new File(task.getProjectPath(), "Assets")
                    def assetsFileTree = project.fileTree(assetsDir)

                    def includeSpec = new Spec<FileTreeElement>() {
                        @Override
                        boolean isSatisfiedBy(FileTreeElement element) {
                            def path = element.getRelativePath().getPathString().toLowerCase()
                            def name = element.name.toLowerCase()
                            def status = true
                            if (path.contains("plugins")
                                    && !((name == "plugins") || (name == "plugins.meta"))) {
                                /*
                                 Why can we use / here? Because {@code element} is a {@code FileTreeElement} object.
                                 The getPath() method is not the same as {@code File.getPath()}
                                 From the docs:

                                 * Returns the path of this file, relative to the root of the containing file tree. Always uses '/' as the hierarchy
                                 * separator, regardless of platform file separator. Same as calling <code>getRelativePath().getPathString()</code>.
                                 *
                                 * @return The path. Never returns null.
                                 */
                                if (task.getBuildPlatform()) {
                                    status = path.contains("plugins/" + task.getBuildPlatform().toLowerCase())
                                } else {
                                    status = true
                                }
                            }

                            status
                        }
                    }

                    def excludeSpec = new Spec<FileTreeElement>() {
                        @Override
                        boolean isSatisfiedBy(FileTreeElement element) {
                            return extension.ignoreFilesForExportUpToDateCheck.contains(element.getFile())
                        }
                    }

                    assetsFileTree.include(includeSpec)
                    assetsFileTree.exclude(excludeSpec)

                    def projectSettingsDir = new File(task.getProjectPath(), "ProjectSettings")
                    def projectSettingsFileTree = project.fileTree(projectSettingsDir)
                    projectSettingsFileTree.exclude(excludeSpec)

                    def packageManagerDir = new File(task.getProjectPath(), "UnityPackageManager")
                    def packageManagerDirFileTree = project.fileTree(packageManagerDir)
                    packageManagerDirFileTree.exclude(excludeSpec)

                    project.files(assetsFileTree, projectSettingsFileTree, packageManagerDirFileTree)
                })
            }
        })

        project.tasks.withType(GradleBuild, new Action<GradleBuild>() {
            @Override
            void execute(GradleBuild t) {
                t.gradleVersion.set(project.provider({project.gradle.gradleVersion}))
            }
        })

        project.afterEvaluate {
            def defaultAppConfigName = extension.getDefaultAppConfigName().getOrNull()
            extension.getAppConfigs().each { File appConfig ->
                def appConfigName = FilenameUtils.removeExtension(appConfig.name)
                def config = new GenericUnityAsset(appConfig)

                def characterPattern = ':_\\-<>|*\\\\?/ '
                def baseName = appConfigName.capitalize().replaceAll(~/([$characterPattern]+)([\w])/) { all, delimiter, firstAfter -> "${firstAfter.capitalize()}" }
                baseName = baseName.replaceAll(~/[$characterPattern]/, '')

                UnityBuildPlayerTask exportTask = project.tasks.create("export${baseName}", UnityBuildPlayerTask) { UnityBuildPlayerTask t ->
                    t.group = "build unity"
                    t.description = "exports gradle project for app config ${appConfigName}"
                    t.appConfigFile.set(appConfig)
                } as UnityBuildPlayerTask

                FileCollection exportInitScripts = project.fileTree(project.projectDir) {
                    it.include('exportInit.gradle')
                }
                List<String> args = []
                args << "-Pexport.buildDirBase=../buildCache" << "--project-cache-dir=../buildCache/.gradle"

                if (exportInitScripts.size() > 0) {
                    args << "--init-script=${exportInitScripts.files.first().path}".toString()
                }

                [baseLifecycleTaskNames, baseLifecycleTaskGroups].transpose().each { String taskName, String groupName ->
                    def gradleBuild = project.tasks.create("${taskName}${baseName.capitalize()}", GradleBuild) { GradleBuild t ->
                        t.dependsOn exportTask
                        t.group = groupName
                        t.description = "executes :${taskName} task on exported project for app config ${appConfigName}"
                        t.dir.set(exportTask.outputDirectory)
                        t.buildArguments.set(args)
                        t.tasks.add(taskName)
                        t.gradleVersion.set(project.provider({
                            if (!config.isValid()) {
                                throw new StopExecutionException('provided appConfig is invalid')
                            }
                            (config.get("gradleVersion", null) ?: project.gradle.gradleVersion).toString()
                        }))
                    }

                    if (defaultAppConfigName == appConfigName) {
                        project.tasks.getByName(taskName).dependsOn(gradleBuild)
                    }
                }
            }
        }
    }
}
