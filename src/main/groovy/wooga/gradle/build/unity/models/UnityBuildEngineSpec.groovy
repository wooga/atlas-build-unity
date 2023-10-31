package wooga.gradle.build.unity.models

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*

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

    /**
     * @return The directory where the builds are output to
     */
    @Internal //TODO: rethink this to get advantage from gradle UP-TO-DATE checks.
    Provider<Directory> getOutputDirectory() {
        return outputDirectory
    }

    void setOutputDirectory(File outputPath) {
        this.outputDirectory.set(outputPath)
    }

    private final Property<String> logPath = objects.property(String)

    /**
     * @return The directory path where log files will be written to
     */
    @Optional
    @Input
    Property<String> getLogPath() {
        return logPath
    }

    void setLogPath(String logPath) {
        this.logPath.set(logPath)
    }

    private final RegularFileProperty secretsFile = objects.fileProperty()

    /**
     * @return The secret file used for resolution during the build process
     */
    @Optional
    @InputFile
    RegularFileProperty getSecretsFile() {
        return secretsFile
    }

    void setSecretsFile(File secretsFile) {
        this.secretsFile.set(secretsFile)
    }

    private final Property<String> configName = objects.property(String)

    /**
     * @return The name of the configuration file to use. This should be used when no path is specified in {@code configFile}.
     */
    @Optional
    @Input
    Property<String> getConfigName() {
        return configName
    }

    void setConfigName(String config) {
        this.configName.set(config)
    }

    private final RegularFileProperty configFile = objects.fileProperty()

    /**
     * @return The configuration file for the build. This should be used when no name is specified in {@code config}
     */
    @Optional
    @InputFile
    RegularFileProperty getConfigFile() {
        return configFile
    }

    void setConfigFile(File config) {
        this.configFile.set(config)
    }

    // TODO: Elaborate
    /**
     * @return The files used by the build
     */
    @SkipWhenEmpty
    @IgnoreEmptyDirectories
    @InputFiles
    ConfigurableFileCollection getInputFiles() {
        inputFiles
    }

    private final ConfigurableFileCollection inputFiles = objects.fileCollection()
}
