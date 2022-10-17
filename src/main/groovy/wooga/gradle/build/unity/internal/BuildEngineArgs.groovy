package wooga.gradle.build.unity.internal


import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

class BuildEngineArgs {

    private final ProviderFactory providers
    final Provider<String> method
    private Map<String, BuildEngineArg> args
    private List<BuildEngineRawArg> rawArgs
    private Provider<Map<String, ?>> environment

    BuildEngineArgs(ProviderFactory providers, Provider<String> method) {
        this.args = new HashMap<>()
        this.rawArgs = new ArrayList<>()
        this.providers = providers
        this.method = method
        this.environment = providers.provider { new HashMap<String, ?>() }
    }

    void addArg(String key, Provider<String> flag, Provider<Object> value) {
        args[key] = new BuildEngineArg(flag, value)
    }

    void addArg(String key, Provider<Object> value) {
        addArg(key, providers.provider({ key }), value)
    }

    void addRawArgs(Provider<List<?>> rawArgsProvider) {
        rawArgs.add(new BuildEngineRawArg(rawArgsProvider))
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

    Provider<List<String>> getArgsProviders() {
        return providers.provider {
            def allArgs = new ArrayList<String>()
            args.values().each {
                if (it.argStringProvider.present) {
                    allArgs.addAll(it.argStringProvider.get())
                }
            }
            rawArgs.each {
                if (it.argStringProvider.present) {
                    allArgs.addAll(it.argStringProvider.get())
                }
            }
            return allArgs
        }
    }
}
