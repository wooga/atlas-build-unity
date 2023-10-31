package wooga.gradle.build.unity.models

import com.wooga.gradle.BaseSpec
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

/**
 * Version properties used by the Unified Build System
 */
trait VersionSpec extends BaseSpec {

    private final Property<String> version = objects.property(String)

    /**
     * @return This is the version string seen by the user. It isn't used for internal comparisons or anything,
     * it's just for users to see.
     */
    @Input
    Property<String> getVersion() {
        version
    }

    void setVersion(String version) {
        this.version.set(version)
    }

    private final Property<String> versionCode = objects.property(String)

    /**
     * @return An internal version number. This number is used only to determine whether one version is more
     * recent than another, with higher numbers indicating more recent versions. This is not the version number
     * shown to users; that number is set by the versionName attribute. The value must be set as an integer.
     */
    @Optional
    @Input
    Property<String> getVersionCode() {
        versionCode
    }

    void setVersionCode(String versionCode) {
        this.versionCode.set(versionCode)
    }

    private final Property<String> toolsVersion = objects.property(String)

    /**
     * @return The android tools version to use
     */
    @Optional
    @Input
    Property<String> getToolsVersion() {
        toolsVersion
    }

    void setToolsVersion(String toolsVersion) {
        this.toolsVersion.set(toolsVersion)
    }

    private final Property<String> commitHash = objects.property(String)

    /**
     * @return The hash of the git commit the build was made from
     */
    @Optional
    @Optional
    @Input
    Property<String> getCommitHash() {
        commitHash
    }

    void setCommitHash(String commitHash) {
        this.commitHash.set(commitHash)
    }
}
