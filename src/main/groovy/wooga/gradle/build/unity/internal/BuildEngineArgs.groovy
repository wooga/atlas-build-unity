package wooga.gradle.build.unity.internal


import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

class BuildEngineArgs {


    private final ProviderFactory providers;
    final Provider<String> method;
    private Map<String, BuildEngineArg> args
    private List<BuildEngineRawArg> rawArgs
    private Provider<Map<String,?>> environment;

    BuildEngineArgs(ProviderFactory providers, Provider<String> method) {
        this.args = new HashMap<>()
        this.rawArgs = new ArrayList<>()
        this.providers = providers
        this.method = method
        this.environment = providers.provider{ new HashMap<String, ?>() };
    }

    void addArg(String key, Provider<?> rawValueProvider) {
        addArg(new BuildEngineArg(key, rawValueProvider))
    }

    void addOptArg(String key, Provider<?> rawValueProvider) {
        addArg(new BuildEngineArg(key, rawValueProvider, true))
    }

    void addArg(BuildEngineArg arg) {
        args.put(arg.key, arg)
    }

    void addRawArgs(Provider<List<?>> rawArgsProvider) {
       addRawArg(rawArgsProvider)
    }

    void addRawArg(Provider<?> rawValueProvider) {
        rawArgs.add(new BuildEngineRawArg(rawValueProvider))
    }

    void addEnvs(Provider<Map<String, ?>> envsProvider) {
        this.environment = this.environment.map {
            it.putAll(envsProvider.get())
            return it
        }
    }

    Provider<Map<String, ?>> getEnvironment() {
        return environment
    }

    Provider<String> getCustomArgsStr() {
        return providers.provider {
            def resolvedArgs = args.collect{ it.value.resolveArgString() }
            def resolvedRawArgs = rawArgs.collect {raw -> raw.resolveArgString() }
            resolvedArgs.addAll(resolvedRawArgs)

            return resolvedArgs.findAll {it != null}.join(" ") as String
        }
    }
}
