package wooga.gradle.build.unity

import org.apache.commons.io.FilenameUtils
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import wooga.gradle.build.unity.tasks.GradleBuild
import wooga.gradle.build.unity.tasks.PlayerBuildEngineUnityTask
import wooga.gradle.build.unity.tasks.UnityBuildPlayerTask
import wooga.gradle.secrets.SecretsPluginExtension
import wooga.gradle.secrets.tasks.FetchSecrets
import wooga.gradle.unity.utils.GenericUnityAssetFile

import javax.crypto.spec.SecretKeySpec

class AppConfig {

    final String name
    final File file
    final GenericUnityAssetFile asset
    final String tasksBaseName

    AppConfig(File appConfigFile) {
        this.name = FilenameUtils.removeExtension(appConfigFile.name)
        this.file = appConfigFile
        this.asset = new GenericUnityAssetFile(appConfigFile)
        this.tasksBaseName = createBaseName(name)
    }

    List<String> fetchAppConfigSecrets(String appConfigSecretsKey) {
        if (asset.containsKey(appConfigSecretsKey)) {
            return asset[appConfigSecretsKey] as List<String>
        }
        return []
    }


    private static String createBaseName(String name) {
        def characterPattern = ':_\\-<>|*\\\\?/ '
        def baseName = name.capitalize().replaceAll(~/([$characterPattern]+)([\w])/) { all, delimiter, firstAfter -> "${firstAfter.capitalize()}" }
        return baseName.replaceAll(~/[$characterPattern]/, '')
    }
}

class AppConfigTasksConfiguration {

    private final static String[] baseLifecycleTaskNames = [LifecycleBasePlugin.ASSEMBLE_TASK_NAME,
                                                            LifecycleBasePlugin.CHECK_TASK_NAME,
                                                            LifecycleBasePlugin.BUILD_TASK_NAME,
                                                            PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME]

    private final static String[] baseLifecycleTaskGroups = [LifecycleBasePlugin.BUILD_GROUP,
                                                             LifecycleBasePlugin.VERIFICATION_GROUP,
                                                             LifecycleBasePlugin.BUILD_GROUP,
                                                             PublishingPlugin.PUBLISH_TASK_GROUP]

    final Project project

    AppConfigTasksConfiguration(Project project) {
        this.project = project
    }

    def configure(File appConfigFile, UnityBuildPluginExtension extension, Provider<SecretKeySpec> secretsKey) {
        def defaultAppConfigName = extension.getDefaultAppConfigName().getOrNull()
        def appConfig = new AppConfig(appConfigFile)
        def baseName = appConfig.tasksBaseName

        TaskProvider<FetchSecrets> fetchSecretsTask = project.tasks.register("fetchSecrets${baseName}", FetchSecrets) { FetchSecrets t ->
            t.group = "secrets"
            t.description = "fetches all secrets configured in ${appConfig.name}"
            t.secretIds.convention(project.provider {
                appConfig.fetchAppConfigSecrets(extension.appConfigSecretsKey.get())
            })
        }
        def secretsFileProvider = fetchSecretsTask.flatMap({ it.secretsFile })
        def exportTask = configureExportTask(appConfig, extension.ubsCompatibilityVersion, secretsFileProvider, secretsKey)

        def lifecycleTaskGroupPairs = [baseLifecycleTaskNames, baseLifecycleTaskGroups].transpose()
        lifecycleTaskGroupPairs.each { String taskName, String groupName ->
            def gradleBuild = project.tasks.register("${taskName}${baseName.capitalize()}", GradleBuild) { GradleBuild t ->
                t.dependsOn exportTask
                t.group = groupName
                t.description = "executes :${taskName} task on exported project for app config ${appConfig.name}"
                t.dir.set(exportTask.flatMap { it.outputDirectory })
                t.initScript.set(extension.exportInitScript)
                t.buildDirBase.set(extension.exportBuildDirBase)
                t.cleanBuildDirBeforeBuild.set(extension.cleanBuildDirBeforeBuild)
                t.secretsFile.set(secretsFileProvider)
                t.secretsKey.set(secretsKey)
                t.tasks.add(taskName)
                t.gradleVersion.set(project.provider({
                    if (!appConfig.asset.isValid()) {
                        throw new StopExecutionException('provided appConfig is invalid')
                    }
                    (appConfig.asset.get("gradleVersion", null) ?: project.gradle.gradleVersion).toString()
                }))
            }

            if (defaultAppConfigName == appConfig.name) {
                project.tasks.getByName(taskName).dependsOn(gradleBuild)
            }
        }
    }

    TaskProvider configureExportTask(AppConfig appConfig, Provider<UBSVersion> ubsCompatibilityVersion,
                                     Provider<RegularFile> secretsFileProvider,
                                     Provider<SecretKeySpec> secretsKey) {
        String exportTaskName = "export${appConfig.tasksBaseName}".toString()

        def ubsVersion = ubsCompatibilityVersion.getOrElse(UBSVersion.v100)
        if (ubsVersion >= UBSVersion.v120) {
            return project.tasks.register(exportTaskName, PlayerBuildEngineUnityTask) {
                PlayerBuildEngineUnityTask t ->
                    t.group = "build unity"
                    t.description = "exports player targeted gradle project for app config ${appConfig.name}"
                    t.configPath.set(appConfig.file)
                    t.secretsFile.set(secretsFileProvider)
                    t.secretsKey.set(secretsKey)
            }
        } else {
            return project.tasks.register(exportTaskName, UnityBuildPlayerTask) {
                UnityBuildPlayerTask t ->
                    t.group = "build unity"
                    t.description = "exports gradle project for app config ${appConfig.name}"
                    t.appConfigFile.set(appConfig.file)
                    t.secretsFile.set(secretsFileProvider)
                    t.secretsKey.set(secretsKey)
            }
        }
    }
}
