package wooga.gradle.build.unity.tasks

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional

class PlayerBuildEngineUnityTask extends BuildEngineUnityTask {

    private final Property<String> version
    private final RegularFileProperty appConfigFile
    private final Property<String> versionCode
    private final Property<String> toolsVersion
    private final Property<String> commitHash

    PlayerBuildEngineUnityTask() {
        this.version = project.objects.property(String)
        this.appConfigFile = project.objects.fileProperty()
        this.versionCode = project.objects.property(String)
        this.toolsVersion = project.objects.property(String)
        this.commitHash = project.objects.property(String)

        this.configPath.convention(appConfigFile)
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
        this.appConfigFile.set(new File(appConfigFile))
    }

    void setAppConfigFile(File appConfigFile) {
        this.appConfigFile.set(appConfigFile)
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