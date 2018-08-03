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

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTreeElement
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.specs.Spec
import org.gradle.language.base.plugins.LifecycleBasePlugin
import wooga.gradle.unity.UnityPlugin
import wooga.gradle.build.unity.internal.DefaultUnityBuildPluginExtension
import wooga.gradle.build.unity.tasks.GradleBuild
import wooga.gradle.build.unity.tasks.UnityBuildPlayerTask

class UnityBuildPlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = "unityBuild"
    static final String EXPORT_ALL_TASK_NAME = "exportAll"

    @Override
    void apply(Project project) {
        project.pluginManager.apply(BasePlugin.class)
        project.pluginManager.apply(UnityPlugin.class)

        def extension = project.extensions.create(UnityBuildPluginExtension, EXTENSION_NAME, DefaultUnityBuildPluginExtension, project)
        def exportLifecycleTask = project.tasks.create(EXPORT_ALL_TASK_NAME)

        def baseLifecycleTaskNames = [LifecycleBasePlugin.ASSEMBLE_TASK_NAME,
                                      LifecycleBasePlugin.CHECK_TASK_NAME,
                                      LifecycleBasePlugin.BUILD_TASK_NAME,
                                      PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME]

        project.tasks.withType(UnityBuildPlayerTask, new Action<UnityBuildPlayerTask>() {
            @Override
            void execute(UnityBuildPlayerTask task) {
                def conventionMapping = task.getConventionMapping()
                conventionMapping.map("version", {project.version})
                conventionMapping.map("exportMethodName", {extension.getExportMethodName()})
                conventionMapping.map("buildEnvironment", {extension.getDefaultEnvironment()})
                conventionMapping.map("buildPlatform", {extension.getDefaultPlatform()})
                conventionMapping.map("toolsVersion", {extension.getToolsVersion()})
                conventionMapping.map("outputDirectoryBase", {extension.getOutputDirectoryBase()})
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
                                status = path.contains("plugins/" + task.getBuildPlatform().toLowerCase())
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

        project.tasks.maybeCreate(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME)

        project.afterEvaluate {
            extension.platforms.each { String platform ->
                def platformLifecycleTask = project.tasks.create("export${platform.capitalize()}")

                extension.environments.each { String environment ->
                    def environmentLifecycleTask = project.tasks.maybeCreate("export${environment.capitalize()}")

                    def exportTask = project.tasks.create("export${platform.capitalize()}${environment.capitalize()}", UnityBuildPlayerTask, new Action<UnityBuildPlayerTask>() {
                        @Override
                        void execute(UnityBuildPlayerTask unityBuildPlayerTask) {
                            unityBuildPlayerTask.buildEnvironment(environment)
                            unityBuildPlayerTask.buildPlatform(platform)
                        }
                    })

                    FileCollection exportInitScripts = project.fileTree(project.projectDir) { it.include('exportInit.gradle') }
                    List<String> args = []
                    args << "-Pexport.buildDirBase=../buildCache" << "--project-cache-dir=../buildCache/.gradle"

                    if(exportInitScripts.size() > 0) {
                        args << "--init-script=${exportInitScripts.files.first().path}".toString()
                    }

                    baseLifecycleTaskNames.each { String taskName ->
                        def t = project.tasks.maybeCreate("${taskName}${platform.capitalize()}${environment.capitalize()}", GradleBuild)
                        t.with {
                            group = environment.capitalize()
                            dependsOn exportTask
                            dir = exportTask.getOutputDirectory()
                            buildArguments = args
                            tasks = [taskName]
                        }
                    }

                    platformLifecycleTask.dependsOn exportTask
                    environmentLifecycleTask.dependsOn exportTask
                    exportLifecycleTask.dependsOn environmentLifecycleTask
                }

                exportLifecycleTask.dependsOn platformLifecycleTask
            }

            baseLifecycleTaskNames.each {
                project.tasks[it].dependsOn project.tasks[getDefaultTaskNameFor(extension, it)]
            }
        }


    }

    private static String getDefaultTaskNameFor(final UnityBuildPluginExtension extension, final String taskName) {
        def platform = extension.defaultPlatform.capitalize()
        def environment = extension.defaultEnvironment.capitalize()
        "${taskName}${platform}${environment}"
    }
}
