package wooga.gradle.build.unity.internal


import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

/**
 * The arguments for executing a build request via the Unified Build System
 */
class BuildEngineArgs {

    private static class BuildEngineArg {

        final Provider<String> argument
        final Provider<Object> value
        final Boolean isFlag

        BuildEngineArg(Provider<String> argument, Provider<Object> value) {
            this.argument = argument
            this.value = value
            this.isFlag = false
        }

        List<Provider<?>> toList() {
            if (value.present) {
                return [argument, value]
            }
            return []
        }
    }

    private static class BuildEngineFlag {
        final Provider<String> flag

        BuildEngineFlag(Provider<String> flag) {
            this.option = flag
        }

        List<Provider<String>> toList() {
            return [flag]
        }
    }

    private final ProviderFactory providers
    final Provider<String> method
    private Provider<Map<String, ?>> environment
    private List<Provider<?>> args

    BuildEngineArgs(ProviderFactory providers, Provider<String> method) {
        this.providers = providers
        this.method = method
        this.environment = providers.provider { new HashMap<String, ?>() }
        this.args = []
    }

    void addArg(Provider<String> arg, Provider<Object> value) {
        args << providers.provider { new BuildEngineArg(arg, value) }
    }

    void addArg(String arg, Provider<Object> value) {
        addArg(providers.provider({ arg }), value)
    }

    void addFlag(Provider<String> flag) {
        args << providers.provider({ new BuildEngineFlag(flag) })
    }

    void addFlag(String flag) {
        addFlag(providers.provider({ flag }))
    }

    void addArgs(Provider<List<?>> arguments) {
        this.args << arguments
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

    private List<String> fetchArguments(Object arg) {
        if (arg instanceof BuildEngineArg) {
            def buildEngineArg = arg as BuildEngineArg
            return buildEngineArg.toList()
                    .findAll { it.present }
                    .collect { argItem -> fetchArguments(argItem.get()) }
                    .flatten().collect { it.toString() }
        }

        if (arg instanceof BuildEngineFlag) {
            def buildEngineFlag = arg as BuildEngineFlag
            return buildEngineFlag.toList()
                    .findAll { it.present }
                    .collect { it.get() }
        }

        if (arg instanceof List) {
            def argList = arg as List
            return argList.collect { argItem -> fetchArguments(argItem) }.flatten().collect { it.toString() }
        }
        if (arg instanceof Map) {
            def argsMap = arg as Map
            return argsMap.collect { key, value ->
                [key.toString(), value.toString()]
            }.flatten().collect { it.toString() }
        } else {
            return [arg.toString()]
        }
    }

    Provider<List<String>> getArguments() {
        return providers.provider {
            def allArgs = []
            this.args.each {
                if (it.present) {
                    allArgs.addAll(fetchArguments(it.get()))
                }
            }
            return allArgs
        }
    }

}

class UnifiedBuildSystemOption {

}
