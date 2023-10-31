package wooga.gradle.build.unity.internal


import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

/**
 * The arguments for executing a build request via the Unified Build System
 */
class BuildRequestArguments {

    private static class Entry {

        final Provider<String> argument
        final Provider<Object> value
        final Boolean isFlag

        Entry(Provider<String> argument, Provider<Object> value) {
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

    private static class Flag {
        final Provider<String> flag

        Flag(Provider<String> flag) {
            this.option = flag
        }

        List<Provider<String>> toList() {
            return [flag]
        }
    }

    /**
     * Used to generate the providers
     */
    private final ProviderFactory factory
    /**
     * The arguments list
     */
    private List<Provider<?>> providers

    BuildRequestArguments(ProviderFactory factory) {
        this.factory = factory
        this.providers = []
    }

    void addArg(Provider<String> arg, Provider<Object> value) {
        providers << factory.provider { new Entry(arg, value) }
    }

    void addArg(String arg, Provider<Object> value) {
        addArg(factory.provider({ arg }), value)
    }

    void addFlag(Provider<String> flag) {
        providers << factory.provider({ new Flag(flag) })
    }

    void addFlag(String flag) {
        addFlag(factory.provider({ flag }))
    }

    void addArgs(Provider<List<?>> arguments) {
        this.providers << arguments
    }

    private List<String> fetchArguments(Object arg) {
        if (arg instanceof Entry) {
            def buildEngineArg = arg as Entry
            return buildEngineArg.toList()
                    .findAll { it.present }
                    .collect { argItem -> fetchArguments(argItem.get()) }
                    .flatten().collect { it.toString() }
        }

        if (arg instanceof Flag) {
            def buildEngineFlag = arg as Flag
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
        return factory.provider {
            def allArgs = []
            this.providers.each {
                if (it.present) {
                    allArgs.addAll(fetchArguments(it.get()))
                }
            }
            return allArgs
        }
    }
}
