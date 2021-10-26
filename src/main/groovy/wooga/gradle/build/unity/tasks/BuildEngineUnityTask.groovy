package wooga.gradle.build.unity.tasks


import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import wooga.gradle.build.unity.internal.BuildEngineArgs
import wooga.gradle.secrets.internal.Secrets
import wooga.gradle.unity.UnityTask

import javax.crypto.spec.SecretKeySpec


abstract class BuildEngineUnityTask extends UnityTask {

    private final Property<String> build
    private final Property<String> config
    private final RegularFileProperty configPath
    private final Property<String> exportMethodName
    private final Property<String> outputPath
    private final Property<String> logPath
    private final RegularFileProperty secretsFile
    private final Property<SecretKeySpec> secretsKey
    private final ListProperty<Object> customArguments

    BuildEngineUnityTask() {
        this.build = project.objects.property(String)
        this.config = project.objects.property(String)
        this.configPath = project.objects.fileProperty()
        this.exportMethodName = project.objects.property(String)
        this.outputPath = project.objects.property(String)
        this.logPath = project.objects.property(String)
        this.secretsFile = project.objects.fileProperty()
        this.secretsKey = project.objects.property(SecretKeySpec.class)
        this.customArguments = project.objects.listProperty(Object)
    }

    protected BuildEngineArgs defaultArgs() {
        Provider<Directory> outputDir = gradleDirectoryFrom(outputPath)
        Provider<Directory> logDir = gradleDirectoryFrom(logPath)

        def secrets = secretsFile.map { RegularFile secretsFile ->
            Secrets.decode(secretsFile.asFile.text)
        }
        def environmentSecrets = project.provider({
            generateSecretsEnvironment(secrets, secretsKey)
        }.memoize())



        def buildEngineArgs = new BuildEngineArgs(project.providers, exportMethodName)
        buildEngineArgs.with {
            addArg("--build", build)
            addArg("--configPath", configPath)
            addArg("--config", config)
            addArg("--outputPath", outputDir.map{out -> out.asFile.path})
            addArg("--logPath", logDir.map{out -> out.asFile.path})
            addRawArgs(customArguments)
            addEnvs(environmentSecrets)
        }
        return buildEngineArgs
    }

    protected void setupExecution(BuildEngineArgs unityArgs) {
        environment.putAll(unityArgs.environment)
        unityArgs.argsProviders.each { Provider<List<String>> argsProvider ->
            additionalArguments.addAll(argsProvider)
        }
        additionalArguments.add("-executeMethod")
        additionalArguments.add(unityArgs.method)
    }

    protected Secrets.EnvironmentSecrets generateSecretsEnvironment(Provider<Secrets> secrets,
                                                                    Property<SecretKeySpec> secretsKey) {
        if (secrets.present && secretsKey.present) {
            def s = secrets.get()
            def key = secretsKey.get()
            return s.encodeEnvironment(key)
        } else {
            return new Secrets.EnvironmentSecrets()
        }
    }

    protected Provider<Directory> gradleDirectoryFrom(Property<String> pathProperty) {
        return pathProperty.map { path ->
            project.layout.projectDirectory.dir(path)
        }
    }

    @Input
    Property<String> getBuild() {
        return build
    }

    @Input
    Property<String> getExportMethodName() {
        return exportMethodName
    }

    @Input
    Property<String> getOutputPath() {
        return outputPath
    }

    @Optional @Input
    Property<String> getLogPath() {
        return logPath
    }

    @Optional @InputFile
    RegularFileProperty getSecretsFile() {
        return secretsFile
    }

    @Optional @Input
    Property<SecretKeySpec> getSecretsKey() {
        return secretsKey
    }

    @Optional @Input
    ListProperty<Object> getCustomArguments() {
        return customArguments
    }

    @Optional @Input
    Property<String> getConfig() {
        return config
    }

    @Optional @InputFile
    RegularFileProperty getConfigPath() {
        return configPath
    }


    void setBuild(String build) {
        this.build.set(build)
    }

    void setExportMethodName(String unityMethodName) {
        this.exportMethodName.set(unityMethodName)
    }

    void setOutputPath(String outputPath) {
        this.outputPath.set(outputPath)
    }

    void setLogPath(String logPath) {
        this.logPath.set(logPath)
    }

    void setSecretsFile(File secretsFile) {
        this.secretsFile.set(secretsFile)
    }

    void setSecretsKey(SecretKeySpec secretsKey) {
        this.secretsKey.set(secretsKey)
    }

    void setCustomArguments(List<Object> customArguments) {
        this.customArguments.set(customArguments)
    }

    void setConfig(String config) {
        this.config.set(config)
    }

    void setConfigPath(File config) {
        this.configPath.set(config)
    }



}
