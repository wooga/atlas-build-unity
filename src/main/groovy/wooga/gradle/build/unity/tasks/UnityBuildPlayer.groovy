package wooga.gradle.build.unity.tasks

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional

class UnityBuildPlayer extends UnityBuildEngineTask {

    private final Property<String> version
    private final Property<String> versionCode
    private final Property<String> toolsVersion
    private final Property<String> commitHash

    UnityBuildPlayer() {
        this.version = project.objects.property(String)
        this.versionCode = project.objects.property(String)
        this.toolsVersion = project.objects.property(String)
        this.commitHash = project.objects.property(String)

        super.build.convention("Player")
        this.doFirst {
            if(!configPath.present && !config.present) {
                throw new IllegalArgumentException("configPath or config task property must be present in PlayerBuildEngineUnityTask")
            }
        }

        def exportArgs = super.defaultArgs()
        exportArgs.with {
            addArg("--version", version)
            addArg("--versionCode", versionCode)
            addArg("--toolsVersion", toolsVersion)
            addArg("--commitHash", commitHash)
        }
        super.setupExecution(exportArgs)
    }

    @Input
    Property<String> getVersion() {
        return version
    }

    @Optional @InputFile
    RegularFileProperty getAppConfigFile() {
        return configPath
    }

    @Optional @Input
    Property<String> getVersionCode() {
        return versionCode
    }

    @Optional @Input
    Property<String> getToolsVersion() {
        return toolsVersion
    }

    @Optional @Input
    Property<String> getCommitHash() {
        return commitHash
    }

    void setVersion(String version) {
        this.version.set(version)
    }

    void setAppConfigFile(String appConfigFile) {
        this.configPath.set(new File(appConfigFile))
    }

    void setAppConfigFile(File appConfigFile) {
        this.configPath.set(appConfigFile)
    }


    void setVersionCode(String versionCode) {
        this.versionCode.set(versionCode)
    }

    void setToolsVersion(String toolsVersion) {
        this.toolsVersion.set(toolsVersion)
    }

    void setCommitHash(String commitHash) {
        this.commitHash.set(commitHash)
    }
}
