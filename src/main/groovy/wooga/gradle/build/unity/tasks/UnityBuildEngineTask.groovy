package wooga.gradle.build.unity.tasks

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.*
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import wooga.gradle.build.unity.internal.BuildEngineArgs
import wooga.gradle.secrets.SecretSpec
import wooga.gradle.secrets.internal.Secrets
import wooga.gradle.unity.UnityTask

import javax.crypto.spec.SecretKeySpec

trait UnityBuildEngineSpec extends BaseSpec {

    private final Property<String> build = objects.property(String)

    @Input
    Property<String> getBuild() {
        return build
    }

    void setBuild(String build) {
        this.build.set(build)
    }

    private final DirectoryProperty outputDirectory = objects.directoryProperty()

    @OutputDirectory
    Provider<Directory> getOutputDirectory() {
        return outputDirectory
    }

    void setOutputDirectory(File outputPath) {
        this.outputDirectory.set(outputPath)
    }

    private final Property<String> logPath = objects.property(String)

    @Optional
    @Input
    Property<String> getLogPath() {
        return logPath
    }

    void setLogPath(String logPath) {
        this.logPath.set(logPath)
    }

    private final RegularFileProperty secretsFile = objects.fileProperty()

    @Optional
    @InputFile
    RegularFileProperty getSecretsFile() {
        return secretsFile
    }

    void setSecretsFile(File secretsFile) {
        this.secretsFile.set(secretsFile)
    }

    private final ListProperty<Object> customArguments = objects.listProperty(Object)

    @Optional
    @Input
    ListProperty<Object> getCustomArguments() {
        return customArguments
    }

    void setCustomArguments(List<Object> customArguments) {
        this.customArguments.set(customArguments)
    }

    private final Property<String> config = objects.property(String)

    @Optional
    @Input
    Property<String> getConfig() {
        return config
    }

    void setConfig(String config) {
        this.config.set(config)
    }

    private final RegularFileProperty configPath = objects.fileProperty()

    @Optional
    @InputFile
    RegularFileProperty getConfigPath() {
        return configPath
    }

    void setConfigPath(File config) {
        this.configPath.set(config)
    }

    private final Property<String> exportMethodName = objects.property(String)

    @Input
    Property<String> getExportMethodName() {
        return exportMethodName
    }

    void setExportMethodName(String unityMethodName) {
        this.exportMethodName.set(unityMethodName)
    }

    private final ConfigurableFileCollection inputFiles = objects.fileCollection()

    @SkipWhenEmpty
    @InputFiles
    ConfigurableFileCollection getInputFiles() {
        inputFiles
    }
}

abstract class UnityBuildEngineTask extends UnityTask implements SecretSpec, UnityBuildEngineSpec {

    UnityBuildEngineTask() {
    }

    protected BuildEngineArgs defaultArgs() {
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
            addArg("--outputPath", outputDirectory.map { out -> out.asFile.path })
            addArg("--logPath", logDir.map { out -> out.asFile.path })
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


}
