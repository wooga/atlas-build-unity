package wooga.gradle.build.unity.tasks

import wooga.gradle.build.unity.internal.BuildRequestArguments
import wooga.gradle.build.unity.models.BuildRequestOption
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
            addArg(BuildRequestOption.version.flag, version)
            addArg(BuildRequestOption.versionCode.flag, versionCode)
            addArg(BuildRequestOption.toolsVersion.flag, toolsVersion)
            addArg(BuildRequestOption.commitHash.flag, commitHash)
        }
    }
}

