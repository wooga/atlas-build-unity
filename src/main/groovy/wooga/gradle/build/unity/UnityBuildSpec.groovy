package wooga.gradle.build.unity

import com.wooga.gradle.BaseSpec
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

trait UnityBuildSpec implements BaseSpec {

    private final Property<String> version = objects.property(String)

    @Input
    Property<String> getVersion() {
        version
    }

    private final Property<String> versionCode = objects.property(String)

    @Optional
    @Input
    Property<String> getVersionCode() {
        versionCode
    }

    private final Property<String> toolsVersion = objects.property(String)

    @Optional
    @Input
    Property<String> getToolsVersion() {
        toolsVersion
    }

    private final Property<String> commitHash = objects.property(String)

    @Optional
    @Input
    Property<String> getCommitHash() {
        commitHash
    }

    private final Property<String> exportMethodName = objects.property(String)

    @Input
    Property<String> getExportMethodName() {
        exportMethodName
    }

    private final MapProperty<String, ?> customArguments = objects.mapProperty(String, Object)

    @Optional
    @Input
    MapProperty<String, ?> getCustomArguments() {
        customArguments
    }
}
