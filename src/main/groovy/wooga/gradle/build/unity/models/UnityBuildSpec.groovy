package wooga.gradle.build.unity.models

import com.wooga.gradle.BaseSpec
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import wooga.gradle.unity.models.UnityCommandLineOptionType


trait UnityBuildSpec extends UnityBuildBaseSpec {
}

enum BuildRequestOption {

    build("--build", true),

    private final String flag
    private final String environmentKey
    private final Boolean map
    private final UnityCommandLineOptionType type

    /**
     * @return The type of this option
     */
    UnityCommandLineOptionType getType() {
        type
    }

    /**
     * @return Whether this option has arguments
     */
    Boolean getHasArguments() {
        type == UnityCommandLineOptionType.Argument
    }

    /**
     * @return The name of the option as seen in the shell; as -$NAME
     */
    String getFlag() {
        flag
    }

    /**
     * @return The name of the environment key, such as UNITY_FOOBAR
     */
    String getEnvironmentKey() {
        environmentKey
    }

    /**
     * @return Whether this option should be automatically mapped to environment/gradle properties
     */
    Boolean getMap() {
        map
    }

    /**
     * Options which require an argument
     */
    static List<BuildRequestOption> argumentFlags = values().findAll { it -> it.hasArguments }
    /**
     * Options which require no argument, act as switches when present
     */
    static List<BuildRequestOption> flags = values().findAll { it -> !it.hasArguments }

    BuildRequestOption(String flag, Boolean hasArguments, Boolean map = true) {
        this.flag = flag
        this.type = hasArguments
            ? UnityCommandLineOptionType.Argument
            : UnityCommandLineOptionType.Flag
        this.environmentKey = null
        this.map = map
    }

}
