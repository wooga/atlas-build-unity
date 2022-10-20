package wooga.gradle.build.unity.tasks

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import wooga.gradle.build.unity.models.VersionSpec

import static wooga.gradle.build.unity.UBSVersion.v160

class UnityBuildPlayer extends UnityBuildEngineTask implements VersionSpec {
    UnityBuildPlayer() {

        super.build.convention("Player")
        this.doFirst {
            if (!configPath.present && !config.present) {
                throw new IllegalArgumentException("configPath or config task property must be present in PlayerBuildEngineUnityTask")
            }
        }

        def exportArgs = super.defaultArgs()
        def versionFlag = ubsCompatibilityVersion.map({ it >= v160 ? "--build-version" : "--version" })
        def versionCodeFlag = ubsCompatibilityVersion.map({ it >= v160 ? "--build-version-code" : "--versionCode" })

        exportArgs.with {
            addArg(versionFlag, version)
            addArg(versionCodeFlag, versionCode)
            addArg("--toolsVersion", toolsVersion)
            addArg("--commitHash", commitHash)
        }
        super.setupExecution(exportArgs)
    }

    @Optional
    @InputFile
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
