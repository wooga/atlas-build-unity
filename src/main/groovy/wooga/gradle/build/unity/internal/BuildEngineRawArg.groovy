package wooga.gradle.build.unity.internal

import org.apache.commons.lang3.StringUtils
import org.gradle.api.provider.Provider

class BuildEngineRawArg {

    //argProvider can be a Provider<String/Map/List>. If none of these, defaults to toString value.
    private Provider<?> argProvider

    BuildEngineRawArg(Provider<?> argProvider) {
        this.argProvider = argProvider
    }

    Provider<String[]> getArgStringProvider() {
        return argProvider.map { Object rawArg ->
            argToString(rawArg)
        }
    }

    private String[] argToString(Object arg) {
        if(arg instanceof List) {
            def argList = arg as List
            return argList.collect { argItem -> argToString(argItem) }.flatten()
        }
        if(arg instanceof Map) {
            def argsMap = arg as Map
            return argsMap.collect { argPair ->
                argString(argPair.key.toString(), argPair.value.toString())
            }.flatten()
        }
        else {
            return [arg.toString()]
        }
    }

    private static String[] argString(String key, String value) {
        return StringUtils.isEmpty(key)? [value] : [key, value]
    }
}
