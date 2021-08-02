package wooga.gradle.build.unity.tasks

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional;


class UnityBuildEnginePlayerTask extends AbstractUnityBuildEngineTask {

    private final Property<String> appConfigFile
    private final Property<String> version
    private final Property<String> versionCode
    private final Property<String> toolsVersion
    private final Property<String> commitHash

    UnityBuildEnginePlayerTask() {
        this.appConfigFile = project.objects.property(String)
        this.version = project.objects.property(String)
        this.versionCode = project.objects.property(String)
        this.toolsVersion = project.objects.property(String)
        this.commitHash = project.objects.property(String)

        super.build.convention("Player")
        def exportArgs = super.defaultArgs()

        exportArgs.with {
            addArg("--config", appConfigFile.orElse(super.config))
            addArg("--version", version)
            addOptArg("--versionCode", versionCode)
            addOptArg("--toolsVersion", toolsVersion)
            addOptArg("--commitHash", commitHash)
        }
        super.setupExecution(exportArgs)
    }

    @InputFile
    Property<String> getAppConfigFile() {
        return appConfigFile
    }

    @Input
    Property<String> getVersion() {
        return version
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

    void setAppConfigFile(String appConfigFile) {
        this.appConfigFile.set(appConfigFile)
    }

    void setAppConfigFile(File appConfigFile) {
        this.appConfigFile.set(appConfigFile.absolutePath)
    }

    void setVersion(String version) {
        this.version.set(version)
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
