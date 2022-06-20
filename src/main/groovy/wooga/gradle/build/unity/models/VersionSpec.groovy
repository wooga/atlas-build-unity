package wooga.gradle.build.unity.models

import com.wooga.gradle.BaseSpec
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

trait VersionSpec extends BaseSpec {

    private final Property<String> version = objects.property(String)

    @Input
    Property<String> getVersion() {
        version
    }

    void setVersion(String version) {
        this.version.set(version)
    }

    private final Property<String> versionCode = objects.property(String)

    @Optional
    @Input
    Property<String> getVersionCode() {
        versionCode
    }

    void setVersionCode(String versionCode) {
        this.versionCode.set(versionCode)
    }

    private final Property<String> toolsVersion = objects.property(String)

    @Optional
    @Input
    Property<String> getToolsVersion() {
        toolsVersion
    }

    void setToolsVersion(String toolsVersion) {
        this.toolsVersion.set(toolsVersion)
    }

    private final Property<String> commitHash = objects.property(String)

    @Optional
    @Input
    Property<String> getCommitHash() {
        commitHash
    }

    void setCommitHash(String commitHash) {
        this.commitHash.set(commitHash)
    }
}
