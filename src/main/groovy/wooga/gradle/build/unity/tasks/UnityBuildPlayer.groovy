package wooga.gradle.build.unity.tasks

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import wooga.gradle.build.unity.models.VersionSpec

class UnityBuildPlayer extends UnityBuildEngineTask implements VersionSpec {

    UnityBuildPlayer() {

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

    @Optional @InputFile
    RegularFileProperty getAppConfigFile() {
        return configPath
    }

    void setAppConfigFile(String appConfigFile) {
        this.configPath.set(new File(appConfigFile))
    }

    void setAppConfigFile(File appConfigFile) {
        this.configPath.set(appConfigFile)
    }
}
