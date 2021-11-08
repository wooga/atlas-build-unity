package wooga.gradle.build.unity.internal

import org.apache.commons.lang3.StringUtils
import org.gradle.api.provider.Provider


class BuildEngineArg {

    final String key
    final Provider<?> argProvider

    BuildEngineArg(String key, Provider<?> valueProvider) {
        this.key = key
        this.argProvider = valueProvider
    }

    Provider<String[]> getArgStringProvider() {
        return argProvider.map { arg -> argString(key, arg.toString()) }
    }

    private static String[] argString(String key, String value) {
        return StringUtils.isEmpty(key)? [value] : [key, value]
    }
}
