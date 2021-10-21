package wooga.gradle.build.unity

import org.gradle.api.Project
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import wooga.gradle.build.unity.tasks.ScriptBuild
import wooga.gradle.build.unity.tasks.UnityBuildPlayerTask
import wooga.gradle.secrets.SecretsPluginExtension
import wooga.gradle.secrets.tasks.FetchSecrets

class ScriptBuildConfiguration {


    final Project project
    final String baseName
    final String appConfigName

    final SecretsPluginExtension secretsExtension
    final UnityBuildPluginExtension extension

    ScriptBuildConfiguration(Project project, String baseName, String appConfigName,
                             SecretsPluginExtension secretsExtension, UnityBuildPluginExtension extension) {
        this.project = project
        this.baseName = baseName
        this.appConfigName = appConfigName
        this.secretsExtension = secretsExtension
        this.extension = extension
    }

    def configureAssemble(TaskProvider<UnityBuildPlayerTask> exportTask, TaskProvider<FetchSecrets> fetchSecretsTask) {
        project.tasks.register("scriptAssemble${baseName.capitalize()}", ScriptBuild) { ScriptBuild t ->
            configureScriptBuildTask(t, exportTask, fetchSecretsTask)
            t.group = LifecycleBasePlugin.BUILD_GROUP
            t.description = "executes build.sh script on exported project for app config ${appConfigName}"
            t.script.convention(extension.exportBuildScript.map{script -> t.dir.get().file(script.path)})
            t.onlyIf { extension.exportBuildScript.present }
            def assembleTask = project.tasks.getByName(LifecycleBasePlugin.ASSEMBLE_TASK_NAME)
            assembleTask.dependsOn(t)
        }
    }


    def configureCheck(TaskProvider<UnityBuildPlayerTask> exportTask, TaskProvider<FetchSecrets> fetchSecretsTask) {
        project.tasks.register("scriptCheck${baseName.capitalize()}", ScriptBuild) { ScriptBuild t ->
            configureScriptBuildTask(t, exportTask, fetchSecretsTask)
            t.group = LifecycleBasePlugin.VERIFICATION_GROUP
            t.description = "executes publish.sh script on exported project for app config ${appConfigName}"
            t.script.convention(extension.exportTestScript.map{script -> t.dir.get().file(script.path)})
            t.onlyIf { extension.exportTestScript.present }
            def checkTask = project.tasks.getByName(LifecycleBasePlugin.CHECK_TASK_NAME)
            checkTask.dependsOn(t)
        }
    }

    def configurePublish(TaskProvider<UnityBuildPlayerTask> exportTask, TaskProvider<FetchSecrets> fetchSecretsTask) {
        project.tasks.register("scriptPublish${baseName.capitalize()}", ScriptBuild) { ScriptBuild t ->
            configureScriptBuildTask(t, exportTask, fetchSecretsTask)
            t.group = PublishingPlugin.PUBLISH_TASK_GROUP
            t.description = "executes publish.sh script on exported project for app config ${appConfigName}"
            t.script.convention(extension.exportPublishScript.map{script -> t.dir.get().file(script.path)})
            t.onlyIf { extension.exportPublishScript.present }
            def publishTask = project.tasks.getByName(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME)
            publishTask.dependsOn(t)
        }
    }
    private void configureScriptBuildTask(ScriptBuild t,
                                 TaskProvider<UnityBuildPlayerTask> exportTask,
                                 TaskProvider<FetchSecrets> fetchSecretsTask) {
        t.dependsOn exportTask
        t.dir.set(exportTask.flatMap({it.outputDirectory}))
        t.secretsFile.set(fetchSecretsTask.flatMap({it.secretsFile}))
        t.secretsKey.set(secretsExtension.secretsKey)
        t.logsShellOutput.convention(true)
    }
}
