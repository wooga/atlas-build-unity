package wooga.gradle.build.unity.internal

import org.gradle.api.provider.Provider

class BuildEngineRawArg {

    //argProvider can be a Provider<String/Map/List>. If none of these, defaults to toString value.
    private Provider<?> argProvider
    private boolean optional

    BuildEngineRawArg(Provider<?> argProvider, boolean optional=false) {
        this.optional = optional
        this.argProvider = argProvider
    }

    String resolveArgString() {
        return new BuildEngineArg(null, argProvider, this.optional).resolveArgString()
    }
}
