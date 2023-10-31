package wooga.gradle.build.unity.models

import com.wooga.gradle.BaseSpec
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

trait UnityBuildBaseSpec implements UBSCompatibility, BaseSpec {

    private final Property<String> exportMethodName = objects.property(String)

    /**
     * @return The method to have Unity execute
     */
    @Input
    Property<String> getExportMethodName() {
        exportMethodName
    }

    void setExportMethodName(String unityMethodName) {
        this.exportMethodName.set(unityMethodName)
    }
}
