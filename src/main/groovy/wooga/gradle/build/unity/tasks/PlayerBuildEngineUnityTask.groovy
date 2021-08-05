package wooga.gradle.build.unity.tasks


import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional

class PlayerBuildEngineUnityTask extends BuildEngineUnityTask {

    private final Property<String> version
    private final Property<String> appConfigFile
    private final Property<String> versionCode
    private final Property<String> toolsVersion
    private final Property<String> commitHash

    PlayerBuildEngineUnityTask() {
        this.version = project.objects.property(String)
        this.appConfigFile = project.objects.property(String)
        this.versionCode = project.objects.property(String)
        this.toolsVersion = project.objects.property(String)
        this.commitHash = project.objects.property(String)

        super.build.convention("Player")
        def exportArgs = super.defaultArgs()

        exportArgs.with {
            addArg("--config", appConfigFile.orElse(super.config))
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
    Property<String> getAppConfigFile() {
        return appConfigFile
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
        this.appConfigFile.set(appConfigFile)
    }

    void setAppConfigFile(File appConfigFile) {
        this.appConfigFile.set(appConfigFile.absolutePath)
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
