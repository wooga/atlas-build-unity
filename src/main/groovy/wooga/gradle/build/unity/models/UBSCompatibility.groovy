package wooga.gradle.build.unity.models

import com.wooga.gradle.BaseSpec
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import wooga.gradle.build.unity.UBSVersion

trait UBSCompatibility extends BaseSpec {

    private final Property<UBSVersion> ubsCompatibilityVersion = objects.property(UBSVersion)

    /**
     * @return The version range of the Unified Build System to be compatible for.
     */
    @Internal
    Property<UBSVersion> getUbsCompatibilityVersion() {
        ubsCompatibilityVersion
    }
}
