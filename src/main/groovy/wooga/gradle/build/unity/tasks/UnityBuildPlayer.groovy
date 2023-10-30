package wooga.gradle.build.unity.tasks


import wooga.gradle.build.unity.models.VersionSpec

import static wooga.gradle.build.unity.UBSVersion.v160

/**
 * The build task that executes default build request (pipeline) provided by the Unified Build System.
 */
class UnityBuildPlayer extends UnityBuildEngineTask implements VersionSpec {
    UnityBuildPlayer() {
        super.build.convention("Player")
        this.doFirst {
            if (!configPath.present && !config.present) {
                throw new IllegalArgumentException("configPath or config task property must be present in PlayerBuildEngineUnityTask")
            }
        }
    }

    @Override
    protected void setupExecution() {
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
}
