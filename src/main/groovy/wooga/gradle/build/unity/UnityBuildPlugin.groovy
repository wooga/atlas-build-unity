/*
 * Copyright 2018-2020 Wooga GmbH
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
import org.gradle.api.file.FileTreeElement
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import wooga.gradle.build.unity.internal.DefaultUnityBuildPluginExtension
import wooga.gradle.build.unity.ios.internal.utils.PropertyUtils
import wooga.gradle.build.unity.tasks.GradleBuild
import wooga.gradle.build.unity.tasks.UnityBuildPlayerTask
import wooga.gradle.secrets.SecretsPlugin
import wooga.gradle.secrets.SecretsPluginExtension
import wooga.gradle.secrets.tasks.FetchSecrets
import wooga.gradle.unity.UnityPlugin
import wooga.gradle.unity.UnityPluginExtension
import wooga.gradle.unity.utils.GenericUnityAssetFile

class UnityBuildPlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = "unityBuild"

    @Override
    void apply(Project project) {
        project.pluginManager.apply(BasePlugin.class)
        project.pluginManager.apply(PublishingPlugin.class)
        project.pluginManager.apply(UnityPlugin.class)
        project.pluginManager.apply(SecretsPlugin.class)

        def extension = project.extensions.create(UnityBuildPluginExtension, EXTENSION_NAME, DefaultUnityBuildPluginExtension, project)
        configureExtension(extension, project)
        configureTasks(extension, project)
    }

    static void configureExtension(UnityBuildPluginExtension extension, Project project) {

        extension.exportMethodName.set(UnityBuildPluginConventions.EXPORT_METHOD_NAME.getStringValueProvider(project))
        extension.defaultAppConfigName.set(UnityBuildPluginConventions.DEFAULT_APP_CONFIG_NAME.getStringValueProvider(project))
        extension.commitHash.set(UnityBuildPluginConventions.BUILD_COMMIT_HASH.getStringValueProvider(project))
        extension.toolsVersion.set(UnityBuildPluginConventions.BUILD_TOOLS_VERSION.getStringValueProvider(project))

        extension.version.convention(project.provider {
            def version = PropertyUtils.convertToString(project.version)
            if (!version || version == "unspecified") {
                return UnityBuildPluginConventions.BUILD_VERSION.getStringValueProvider(project).getOrElse("unspecified")
            }
            version
        })

        extension.versionCode.convention(UnityBuildPluginConventions.BUILD_VERSION_CODE.getStringValueProvider(project))
        extension.outputDirectoryBase.convention(project.layout.buildDirectory.dir(UnityBuildPluginConventions.DEFAULT_EXPORT_DIRECTORY_NAME))

        UnityPluginExtension unity = project.extensions.getByType(UnityPluginExtension)
        extension.assetsDir.convention(unity.assetsDir)

        extension.appConfigsDirectory.convention(extension.assetsDir.dir(UnityBuildPluginConventions.DEFAULT_APP_CONFIGS_DIRECTORY))
        extension.exportInitScript.convention(UnityBuildPluginConventions.EXPORT_INIT_SCRIPT.getFileValueProvider(project))

        extension.exportBuildDirBase.convention(UnityBuildPluginConventions.EXPORT_BUILD_DIR_BASE.getStringValueProvider(project).map({new File(it)}))

        extension.cleanBuildDirBeforeBuild.set(UnityBuildPluginConventions.CLEAN_BUILD_DIR_BEFORE_BUILD.getBooleanValueProvider(project))
        extension.appConfigSecretsKey.set(UnityBuildPluginConventions.APP_CONFIG_SECRETS_KEY.getStringValueProvider(project))
    }

    static void configureTasks(UnityBuildPluginExtension extension, Project project) {

        def secretsExtension = project.extensions.getByType(SecretsPluginExtension.class)

        def baseLifecycleTaskNames = [LifecycleBasePlugin.ASSEMBLE_TASK_NAME,
                                      LifecycleBasePlugin.CHECK_TASK_NAME,
                                      LifecycleBasePlugin.BUILD_TASK_NAME,
                                      PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME]

        def baseLifecycleTaskGroups = [LifecycleBasePlugin.BUILD_GROUP,
                                       LifecycleBasePlugin.VERIFICATION_GROUP,
                                       LifecycleBasePlugin.BUILD_GROUP,
                                       PublishingPlugin.PUBLISH_TASK_GROUP]

        project.tasks.withType(UnityBuildPlayerTask).configureEach({UnityBuildPlayerTask t ->
            t.exportMethodName.convention(extension.exportMethodName)
            t.toolsVersion.convention(extension.toolsVersion)
            t.commitHash.convention(extension.commitHash)
            t.outputDirectoryBase.convention(extension.outputDirectoryBase)
            t.version.convention(extension.version)
            t.versionCode.convention(extension.versionCode)
            t.customArguments.convention(extension.customArguments)
            t.inputFiles.from({

                def assetsDir = t.projectDirectory.dir("Assets")
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
                            if (t.getBuildPlatform()) {
                                status = path.contains("plugins/" + t.getBuildPlatform().get())
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

                def projectSettingsDir = t.projectDirectory.dir("ProjectSettings")
                def projectSettingsFileTree = project.fileTree(projectSettingsDir)
                projectSettingsFileTree.exclude(excludeSpec)

                def packageManagerDir = t.projectDirectory.dir("UnityPackageManager")
                def packageManagerDirFileTree = project.fileTree(packageManagerDir)
                packageManagerDirFileTree.exclude(excludeSpec)

                project.files(assetsFileTree, projectSettingsFileTree, packageManagerDirFileTree)
            })
        })

        project.tasks.withType(GradleBuild).configureEach({GradleBuild t ->
            t.gradleVersion.convention(project.provider({ project.gradle.gradleVersion }))
        })

        // Generate tasks for each app config in the Unity project
        project.afterEvaluate {

            def defaultAppConfigName = extension.getDefaultAppConfigName().getOrNull()

            // For every app config
            extension.getAppConfigs().each { File appConfig ->
                def appConfigName = FilenameUtils.removeExtension(appConfig.name)
                def config = new GenericUnityAssetFile(appConfig)

                // Compose a suffix based on the appconfig for the tasks about to be generated
                def characterPattern = ':_\\-<>|*\\\\?/ '
                def appConfigSuffix = appConfigName.capitalize().replaceAll(~/([$characterPattern]+)([\w])/) { all, delimiter, firstAfter -> "${firstAfter.capitalize()}" }
                appConfigSuffix = appConfigSuffix.replaceAll(~/[$characterPattern]/, '')

                // Generate a task to fetch secrets, which will be used by the build system
                TaskProvider<FetchSecrets> fetchSecretsTask = project.tasks.register("fetchSecrets${appConfigSuffix}", FetchSecrets) { FetchSecrets t ->
                    t.group = "secrets"
                    t.description = "fetches all secrets configured in ${appConfigName}"
                    t.secretIds.convention(project.provider({
                        if (config.containsKey(extension.appConfigSecretsKey.get())) {
                            return config[extension.appConfigSecretsKey.get()] as List<String>
                        }
                        []
                    }))
                }

                // Generate a task to export the gradle project
                // TODO: Explain
                TaskProvider<UnityBuildPlayerTask> exportTask = project.tasks.register("export${appConfigSuffix}", UnityBuildPlayerTask) { UnityBuildPlayerTask t ->
                    t.group = "build unity"
                    t.description = "exports gradle project for app config ${appConfigName}"
                    t.appConfigFile.set(appConfig)
                    t.secretsFile.set(fetchSecretsTask.flatMap({it.secretsFile}))
                    t.secretsKey.set(secretsExtension.secretsKey)
                }

                // Generate tasks based on the lifecycle tasks
                // TODO: Explain
                [baseLifecycleTaskNames, baseLifecycleTaskGroups].transpose().each { String taskName, String groupName ->
                    def gradleBuild = project.tasks.register("${taskName}${appConfigSuffix.capitalize()}", GradleBuild) { GradleBuild t ->
                        t.dependsOn exportTask
                        t.group = groupName
                        t.description = "executes :${taskName} task on exported project for app config ${appConfigName}"
                        t.dir.set(exportTask.flatMap({it.outputDirectory}))
                        t.initScript.set(extension.exportInitScript)
                        t.buildDirBase.set(extension.exportBuildDirBase)
                        t.cleanBuildDirBeforeBuild.set(extension.cleanBuildDirBeforeBuild)
                        t.secretsFile.set(fetchSecretsTask.flatMap({it.secretsFile}))
                        t.secretsKey.set(secretsExtension.secretsKey)
                        t.tasks.add(taskName)
                        t.gradleVersion.set(project.provider({
                            if (!config.isValid()) {
                                throw new StopExecutionException('provided appConfig is invalid')
                            }
                            (config.get("gradleVersion", null) ?: project.gradle.gradleVersion).toString()
                        }))
                    }

                    // If this is the default app config....
                    // TODO: Explain
                    if (defaultAppConfigName == appConfigName) {
                        project.tasks.getByName(taskName).dependsOn(gradleBuild)
                    }
                }
            }
        }
    }
}
