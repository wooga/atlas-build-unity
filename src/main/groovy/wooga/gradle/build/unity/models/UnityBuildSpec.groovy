package wooga.gradle.build.unity.models

import com.wooga.gradle.BaseSpec
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional


trait UnityBuildSpec extends UnityBuildBaseSpec {

    // TODO:
//    private final MapProperty<String, ?> customArguments = objects.mapProperty(String, Object)
//
//    @Optional
//    @Input
//    MapProperty<String, ?> getCustomArguments() {
//        customArguments
//    }
}

enum BuildRequestOption {

}
