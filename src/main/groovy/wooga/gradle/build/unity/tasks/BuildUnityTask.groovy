package wooga.gradle.build.unity.tasks


import org.gradle.api.file.Directory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import wooga.gradle.build.unity.internal.BuildRequestArguments
import wooga.gradle.build.unity.models.BuildRequestOption
import wooga.gradle.build.unity.models.UnityBuildEngineSpec
import wooga.gradle.unity.UnityTask
/**
 * The base task that can run builds via the Unified Build System, our Unity-side package for executing builds in the Unity Editor.
 */
class BuildUnityTask extends UnityTask implements UnityBuildEngineSpec {

    BuildUnityTask() {
        additionalArguments.addAll(project.provider {
            def args  = new BuildRequestArguments(project.providers)
            appendBuildArguments(args)
            def list = args.getArguments().get()
            list
        })
    }

    protected void appendBuildArguments(BuildRequestArguments args) {
        Provider<Directory> logDir = gradleDirectoryFrom(logPath)
        args.with {
            addArg(BuildRequestOption.build.flag, build)
            addArg(BuildRequestOption.configPath.flag, configFile)
            addArg(BuildRequestOption.config.flag, configName)
            addArg(BuildRequestOption.outputPath.flag, outputDirectory.map { out -> out.asFile.path })
            addArg(BuildRequestOption.logPath.flag, logDir.map { out -> out.asFile.path })
        }
    }

    protected Provider<Directory> gradleDirectoryFrom(Property<String> pathProperty) {
        return pathProperty.map { path ->
            project.layout.projectDirectory.dir(path)
        }
    }

}
