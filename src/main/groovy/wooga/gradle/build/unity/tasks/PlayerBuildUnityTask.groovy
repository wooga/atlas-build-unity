package wooga.gradle.build.unity.tasks

import wooga.gradle.build.unity.internal.BuildRequestArguments
import wooga.gradle.build.unity.models.VersionSpec

/**
 * The build task that executes default build request (pipeline) provided by the Unified Build System.
 */
class PlayerBuildUnityTask extends BuildUnityTask implements VersionSpec {

    PlayerBuildUnityTask() {
    }

    @Override
    protected void appendBuildArguments(BuildRequestArguments args) {
        super.appendBuildArguments(args)

        args.with {
            addArg("--build-version", version)
            addArg("--build-version-code", versionCode)
            addArg("--toolsVersion", toolsVersion)
            addArg("--commitHash", commitHash)
        }
    }
}

