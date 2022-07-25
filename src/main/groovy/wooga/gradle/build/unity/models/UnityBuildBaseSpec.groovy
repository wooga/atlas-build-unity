package wooga.gradle.build.unity.models

import com.wooga.gradle.BaseSpec
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

trait UnityBuildBaseSpec implements BaseSpec {

    private final Property<String> exportMethodName = objects.property(String)

    @Input
    Property<String> getExportMethodName() {
        exportMethodName
    }

    void setExportMethodName(String unityMethodName) {
        this.exportMethodName.set(unityMethodName)
    }
}
