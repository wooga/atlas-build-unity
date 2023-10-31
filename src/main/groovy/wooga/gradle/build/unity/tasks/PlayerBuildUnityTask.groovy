package wooga.gradle.build.unity.tasks

import wooga.gradle.build.unity.internal.BuildEngineArgs
import wooga.gradle.build.unity.models.VersionSpec

import static wooga.gradle.build.unity.UBSVersion.v160

/**
 * The build task that executes default build request (pipeline) provided by the Unified Build System.
 */
class PlayerBuildUnityTask extends BuildUnityTask implements VersionSpec {

    PlayerBuildUnityTask() {
    }

    @Override
    protected void appendBuildArguments(BuildEngineArgs args) {
        super.appendBuildArguments(args)

        def versionFlag = ubsCompatibilityVersion.map({
            it >= v160 ? "--build-version" : "--version"
        })

        def versionCodeFlag = ubsCompatibilityVersion.map({
            it >= v160 ? "--build-version-code" : "--versionCode"
        })

        args.with {
            addArg(versionFlag, version)
            addArg(versionCodeFlag, versionCode)
            addArg("--toolsVersion", toolsVersion)
            addArg("--commitHash", commitHash)
        }
    }
}
