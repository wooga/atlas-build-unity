package wooga.gradle.build.unity.internal

import org.apache.commons.lang3.StringUtils
import org.gradle.api.provider.Provider

class BuildEngineArg {

    final Provider<String> flag
    final Provider<Object> value

    BuildEngineArg(Provider<String> flag, Provider<Object> value) {
        this.flag = flag
        this.value = value
    }

    Provider<String[]> getArgStringProvider() {
        flag.map({
            if (value.present) {
                return argString(it, value.get().toString())
            }
            null
        })
    }

    private static String[] argString(String key, String value) {
        return StringUtils.isEmpty(key) ? [value] : [key, value]
    }
}
