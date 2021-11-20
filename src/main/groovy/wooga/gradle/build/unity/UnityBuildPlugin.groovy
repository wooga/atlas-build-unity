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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.sonarqube.gradle.SonarQubeExtension
import wooga.gradle.build.unity.internal.UnityInputFilesFactory
import wooga.gradle.build.unity.internal.DefaultUnityBuildPluginExtension
import wooga.gradle.build.unity.ios.internal.utils.PropertyUtils
import wooga.gradle.build.unity.tasks.BuildEngineUnityTask
import wooga.gradle.build.unity.tasks.GradleBuild
import wooga.gradle.build.unity.tasks.PlayerBuildEngineUnityTask
import wooga.gradle.build.unity.tasks.UnityBuildPlayerTask
import wooga.gradle.dotnetsonar.DotNetSonarqubePlugin
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
        project.pluginManager.apply(DotNetSonarqubePlugin.class)

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

        def inputFiles = new UnityInputFilesFactory(project, extension)
        def secretsExtension = project.extensions.getByType(SecretsPluginExtension.class)

        project.tasks.withType(UnityBuildPlayerTask).configureEach({UnityBuildPlayerTask t ->
            t.exportMethodName.convention(extension.exportMethodName)
            t.toolsVersion.convention(extension.toolsVersion)
            t.commitHash.convention(extension.commitHash)
            t.outputDirectoryBase.convention(extension.outputDirectoryBase)
            t.version.convention(extension.version)
            t.versionCode.convention(extension.versionCode)
            t.customArguments.convention(extension.customArguments)
            t.inputFiles.from(inputFiles.unityTaskInputFilesProvider(t.buildTarget, t.projectDirectory))
        })

        project.tasks.withType(BuildEngineUnityTask).configureEach { t ->
            t.exportMethodName.convention("Wooga.UnifiedBuildSystem.Editor.BuildEngine.BuildFromEnvironment")
            def outputDir = extension.outputDirectoryBase.dir(t.build.map{new File(it, "project").path})
            t.outputDirectory.convention(outputDir)
            t.logPath.convention(t.unityLogFile.map{logFile ->
                return logFile.asFile.toPath().parent.toString()
            })
            t.customArguments.convention(extension.customArguments.map {[it] })
            t.inputFiles.from(inputFiles.unityTaskInputFilesProvider(t.buildTarget, t.projectDirectory))
            t.buildTarget.convention(t.configPath.map({
                def config = new GenericUnityAssetFile(it.asFile)
                return config["batchModeBuildTarget"]?.toString()?.toLowerCase()
            }))
        }

        project.tasks.withType(PlayerBuildEngineUnityTask).configureEach { task ->
            task.build.convention("Player")
            def appConfigName = task.config.orElse(
                    task.configPath.asFile.map{FilenameUtils.removeExtension(it.name)}
            )
            def configRelativePath = appConfigName.map{return new File(it, "project").path }
            def outputPath = extension.outputDirectoryBase.dir(configRelativePath)
            task.outputDirectory.convention(outputPath)
            task.toolsVersion.convention(extension.toolsVersion)
            task.commitHash.convention(extension.commitHash)
            task.version.convention(extension.version)
            task.versionCode.convention(extension.versionCode)
            task.buildTarget.convention(task.appConfigFile.map({
                def config = new GenericUnityAssetFile(it.asFile)
                return config["batchModeBuildTarget"]?.toString()?.toLowerCase()
            }))
        }

        project.tasks.withType(GradleBuild).configureEach({GradleBuild t ->
            t.gradleVersion.convention(project.provider({ project.gradle.gradleVersion }))
        })
        configureSonarqubeTasks(project)

        project.afterEvaluate {
            def appConfigConfiguration = new AppConfigTasksConfiguration(project)
            extension.getAppConfigs().each { File appConfig ->
                appConfigConfiguration.configure(appConfig, extension, secretsExtension.secretsKey)
            }
        }
    }

    static void configureSonarqubeTasks(Project project) {
        def unityExt = project.extensions.findByType(UnityPluginExtension)
        def sonarExt = project.extensions.findByType(SonarQubeExtension)
        def sonarConfiguration = new SonarQubeConfiguration(project).with {
            sonarTaskName = "sonarqube"
            buildTaskName = "sonarBuildUnity"
            return it
        }

        sonarConfiguration.configure(unityExt, sonarExt)
    }
}
