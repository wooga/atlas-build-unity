package wooga.gradle.build.unity.models

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import wooga.gradle.build.unity.UBSVersion
import wooga.gradle.build.unity.models.UnityBuildBaseSpec

trait UnityBuildEngineSpec extends UnityBuildBaseSpec {

    private final Property<String> build = objects.property(String)

    /**
     * @return The name of the build request type or its alias
     */
    @Input
    Property<String> getBuild() {
        return build
    }

    void setBuild(String build) {
        this.build.set(build)
    }

    private final DirectoryProperty outputDirectory = objects.directoryProperty()

    @Internal //TODO: rethink this to get advantage from gradle UP-TO-DATE checks.
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

    private final Property<String> config = objects.property(String)

    /**
     * @return The name of the configuration file to use. This should be used when no path is specified in {@code configPath}.
     */
    @Optional
    @Input
    Property<String> getConfig() {
        return config
    }

    void setConfig(String config) {
        this.config.set(config)
    }

    private final RegularFileProperty configPath = objects.fileProperty()

    /**
     * @return The path to the configuration file for the build. This should be used when no name is specified in {@code config}
     */
    @Optional
    @InputFile
    RegularFileProperty getConfigPath() {
        return configPath
    }

    void setConfigPath(File config) {
        this.configPath.set(config)
    }

    private final ConfigurableFileCollection inputFiles = objects.fileCollection()

    @SkipWhenEmpty
    @InputFiles
    ConfigurableFileCollection getInputFiles() {
        inputFiles
    }
}
