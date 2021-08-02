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


abstract class AbstractUnityBuildEngineTask extends UnityTask {

    private static final String CUSTOM_ARGS_DECL = "-CustomArgs";

    private final Property<String> build
    private final Property<String> config
    private final Property<String> exportMethodName
    private final Property<String> outputPath
    private final RegularFileProperty secretsFile
    private final Property<SecretKeySpec> secretsKey
    private final ListProperty<Object> customArguments

    AbstractUnityBuildEngineTask() {
        this.build = project.objects.property(String)
        this.config = project.objects.property(String)
        this.exportMethodName = project.objects.property(String)
        this.outputPath = project.objects.property(String)
        this.secretsFile = project.objects.fileProperty()
        this.secretsKey = project.objects.property(SecretKeySpec.class)
        this.customArguments = project.objects.listProperty(Object)
    }

    BuildEngineArgs defaultArgs() {
        Provider<Directory> outputDir = outputPath.map { path ->
            project.layout.projectDirectory.dir(path)
        }
        def secrets = secretsFile.map { RegularFile secretsFile ->
            Secrets.decode(secretsFile.asFile.text)
        }
        def environmentSecrets = project.provider({
            generateSecretsEnvironment(secrets, secretsKey)
        }.memoize())

        def buildEngineArgs = new BuildEngineArgs(project.providers, exportMethodName)
        buildEngineArgs.with {
            addArg("--build", build)
            addOptArg("--config", config)
            addArg("--outputPath", outputDir.map{out -> out.asFile.path})
            addRawArgs(customArguments)
            addEnvs(environmentSecrets)
        }
        return buildEngineArgs
    }

    def setupExecution(BuildEngineArgs unityArgs) {
        environment.putAll(unityArgs.environment)
        additionalArguments.add(unityArgs.customArgsStr.map {
            args -> "${CUSTOM_ARGS_DECL}: ${args}"
        })
        additionalArguments.add("-executeMethod")
        additionalArguments.add(unityArgs.method)
    }

    def generateSecretsEnvironment(Provider<Secrets> secrets, Property<SecretKeySpec> secretsKey) {
        if (secrets.present && secretsKey.present) {
            def s = secrets.get()
            def key = secretsKey.get()
            return s.encodeEnvironment(key)
        } else {
            return new Secrets.EnvironmentSecrets()
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

    void setBuild(String build) {
        this.build.set(build)
    }

    void setExportMethodName(String unityMethodName) {
        this.exportMethodName.set(unityMethodName)
    }

    void setOutputPath(String outputPath) {
        this.outputPath.set(outputPath)
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

    void setConfig(File config) {
        this.config.set(config.absolutePath)
    }
}
