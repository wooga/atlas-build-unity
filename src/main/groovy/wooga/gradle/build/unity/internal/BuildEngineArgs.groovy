package wooga.gradle.build.unity.internal


import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

class BuildEngineArgs {


    private final ProviderFactory providers;

    final Provider<String> method;
    private List<BuildEngineArg> args = new ArrayList<>();
    private Provider<Map<String,?>> environment;

    BuildEngineArgs(ProviderFactory providers, Provider<String> method) {
        this.args = new ArrayList<>();
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

    void addArg(Provider<?> rawValueProvider) {
        addArg(new BuildEngineArg(rawValueProvider))
    }

    void addArgs(Provider<List<?>> rawArgsProvider) {
       addArg(rawArgsProvider)
    }

    void addArg(BuildEngineArg arg) {
        args.add(arg)
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
            def resolvedArgs = args.collect{ it.resolveArgString() }.findAll {it != null}
            return resolvedArgs.join(" ") as String
        }
    }
}
