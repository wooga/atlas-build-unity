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

package wooga.gradle.unity.build

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.GradleBuild
import org.gradle.internal.impldep.aQute.bnd.service.lifecycle.LifeCyclePlugin
import org.gradle.language.base.plugins.LifecycleBasePlugin
import wooga.gradle.unity.UnityPlugin
import wooga.gradle.unity.build.internal.DefaultBuildUnityPluginExtension
import wooga.gradle.unity.tasks.Unity

class UnityBuildPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply(BasePlugin.class)
        project.pluginManager.apply(UnityPlugin.class)

        def extension = project.extensions.create(BuildUnityPluginExtension, "unityBuild", DefaultBuildUnityPluginExtension)
        def exportLifecycleTask = project.tasks.create("exportAll")
        extension.platforms.each { String platform ->
            def platformLifecycleTask = project.tasks.create("export${platform.capitalize()}")

            extension.environments.each { String environment ->
                def environmentLifecycleTask = project.tasks.maybeCreate("export${environment.capitalize()}")

                def exportTask = project.tasks.create(name: "export${platform.capitalize()}${environment.capitalize()}", type: Unity) {
                    outputs.dir temporaryDir

                    args "-executeMethod", "Wooga.UnityBuild.NewAutomatedBuild.Export"
                    args "-CustomArgs:platform=${platform};environment=${environment};outputPath=${temporaryDir.path}"
                }

                ['assemble', 'publish', 'check', 'build'].each { String taskName ->
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

        [LifecycleBasePlugin.ASSEMBLE_TASK_NAME, LifecycleBasePlugin.CHECK_TASK_NAME, LifecycleBasePlugin.BUILD_TASK_NAME].each {
            project.tasks[it].dependsOn project.tasks[getDefaultTaskNameFor(project, extension, it)]
        }
    }

    private static String getDefaultTaskNameFor(final Project project, final BuildUnityPluginExtension extension, final String taskName) {
        def platform = System.env['RELEASE_PLATFORM'] ?: project.properties.get('release.platform', extension.platforms.first())
        def environment = System.env['RELEASE_ENVIRONMENT'] ?: project.properties.get('release.environment', extension.environments.first())

        "${taskName}${platform.capitalize()}${environment.capitalize()}"
    }
}
