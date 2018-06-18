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
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.publish.plugins.PublishingPlugin
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
                conventionMapping.map("exportMethodName", {extension.getExportMethodName()})
                conventionMapping.map("buildEnvironment", {extension.getDefaultEnvironment()})
                conventionMapping.map("buildPlatform", {extension.getDefaultPlatform()})
                conventionMapping.map("toolsVersion", {extension.getToolsVersion()})
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

                    baseLifecycleTaskNames.each { String taskName ->
                        def t = project.tasks.maybeCreate("${taskName}${platform.capitalize()}${environment.capitalize()}", GradleBuild)
                        t.with {
                            group = environment.capitalize()
                            dependsOn exportTask
                            dir = exportTask.outputs.files.singleFile
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
