/*
 * Copyright 2017 the original author or authors.
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
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTreeElement
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.specs.Spec
import org.gradle.internal.impldep.org.apache.ivy.util.FileUtil
import org.gradle.language.base.plugins.LifecycleBasePlugin
import wooga.gradle.build.unity.ios.internal.utils.PropertyUtils
import wooga.gradle.unity.UnityPlugin
import wooga.gradle.build.unity.internal.DefaultUnityBuildPluginExtension
import wooga.gradle.build.unity.tasks.GradleBuild
import wooga.gradle.build.unity.tasks.UnityBuildPlayerTask

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
                def conventionMapping = task.getConventionMapping()
                conventionMapping.map("exportMethodName", { extension.getExportMethodName() })
                conventionMapping.map("toolsVersion", { extension.getToolsVersion() })
                conventionMapping.map("outputDirectoryBase", { extension.getOutputDirectoryBase() })
                conventionMapping.map("version", { PropertyUtils.convertToString(project.version) })
                conventionMapping.map("inputFiles", {

                    def assetsDir = new File(task.getProjectPath(), "Assets")
                    def assetsFileTree = project.fileTree(assetsDir)

                    assetsFileTree.include(new Spec<FileTreeElement>() {
                        @Override
                        boolean isSatisfiedBy(FileTreeElement element) {
                            def path = element.path.toLowerCase()
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
                    })

                    def projectSettingsDir = new File(task.getProjectPath(), "ProjectSettings")
                    def projectSettingsFileTree = project.fileTree(projectSettingsDir)

                    def packageManagerDir = new File(task.getProjectPath(), "UnityPackageManager")
                    def packageManagerDirFileTree = project.fileTree(packageManagerDir)

                    project.files(assetsFileTree, projectSettingsFileTree, packageManagerDirFileTree)
                })
            }
        })

        project.afterEvaluate {
            def defaultAppConfigName = extension.getDefaultAppConfigName()
            extension.getAppConfigs().each { File appConfig ->
                def appConfigName = FilenameUtils.removeExtension(appConfig.name)

                def characterPattern = ':_\\-<>|*\\\\?/ '
                def baseName = appConfigName.capitalize().replaceAll(~/([$characterPattern]+)([\w])/) { all, delimiter, firstAfter -> "${firstAfter.capitalize()}" }
                baseName = baseName.replaceAll(~/[$characterPattern]/,'')

                def exportTask = project.tasks.create("export${baseName}", UnityBuildPlayerTask) {
                    it.group = "build unity"
                    it.description = "exports gradle project for app config ${appConfigName}"
                    it.appConfigFile appConfig
                }

                FileCollection exportInitScripts = project.fileTree(project.projectDir) {
                    it.include('exportInit.gradle')
                }
                List<String> args = []
                args << "-Pexport.buildDirBase=../buildCache" << "--project-cache-dir=../buildCache/.gradle"

                if (exportInitScripts.size() > 0) {
                    args << "--init-script=${exportInitScripts.files.first().path}".toString()
                }

                [baseLifecycleTaskNames, baseLifecycleTaskGroups].transpose().each { String taskName, String groupName ->
                    def t = project.tasks.create("${taskName}${baseName.capitalize()}", GradleBuild)
                    t.with {
                        dependsOn exportTask
                        group = groupName
                        description = "executes :${taskName} task on exported project for app config ${appConfigName}"
                        dir = exportTask.getOutputDirectory()
                        buildArguments = args
                        tasks = [taskName]
                    }

                    if (defaultAppConfigName == appConfigName) {
                        project.tasks.getByName(taskName).dependsOn(t)
                    }
                }
            }
        }
    }
}
