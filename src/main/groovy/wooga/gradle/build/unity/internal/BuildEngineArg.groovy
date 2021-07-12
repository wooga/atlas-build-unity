package wooga.gradle.build.unity.internal

import org.apache.commons.lang3.StringUtils
import org.gradle.api.internal.provider.MissingValueException
import org.gradle.api.provider.Provider


class BuildEngineArg {

    final String key
    final Provider<?> argProvider
    final boolean optional

    BuildEngineArg(Map<String, Provider<?>> rawArg, boolean optional=false) {
        this.key = rawArg.keySet().first()
        this.argProvider = rawArg.get(this.key).map{ it.toString() }
        this.optional=optional
    }

    BuildEngineArg(String key, Provider<?> valueProvider,boolean optional=false) {
        this.key = key
        this.argProvider = valueProvider.map {it.toString() }
        this.optional=optional
    }

    //argProvider can be a Provider<String/Map/List>. If none of these, defaults to toString value.
    BuildEngineArg(Provider<?> argProvider, boolean optional=false) {
        this.key = null
        this.argProvider = argProvider
        this.optional = optional
    }

    String resolveArgString() {
        if(optional && !argProvider.present) {
            return ""
        }
        return argToString(argProvider.orElse {
            throw new IllegalStateException("No value for the provider ${key? "associated with the key ${key}": ""}")
        }.get())
    }

    private String argToString(Object arg) {
        if(arg instanceof Map) {
            def argsMap = arg as Map
            return argsMap.collect { argPair ->
                argString(argPair.key.toString(), argPair.value.toString())
            }.join(" ")
        }
        if(arg instanceof List) {
            def argList = arg as List
            return argList.collect { argItem -> argToString(argItem) }.join(" ")
        }
        else {
            def strValue = arg.toString()
            return argString(key, strValue)
        }
    }

    private static String argString(String key, String value) {
        return StringUtils.isEmpty(key)? value : "${key} ${value}"
    }
}
