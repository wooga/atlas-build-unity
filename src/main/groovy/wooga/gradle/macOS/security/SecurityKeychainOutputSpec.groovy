package wooga.gradle.macOS.security

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile

trait SecurityKeychainOutputSpec extends BaseSpec {
    private final Property<String> fileName = objects.property(String)

    @Internal
    Property<String> getFileName() {
        fileName
    }

    void setFileName(Provider<String> value) {
        fileName.set(value)
    }

    void setFileName(String value) {
        fileName.set(value)
    }

    private final Property<String> baseName = objects.property(String)

    @Internal
    Property<String> getBaseName() {
        baseName
    }

    void setBaseName(Provider<String> value) {
        baseName.set(value)
    }

    void setBaseName(String value) {
        baseName.set(value)
    }

    private final Property<String> extension = objects.property(String)

    @Internal
    Property<String> getExtension() {
        extension
    }

    void setExtension(Provider<String> value) {
        extension.set(value)
    }

    void setExtension(String value) {
        extension.set(value)
    }

    private final DirectoryProperty destinationDir = objects.directoryProperty()

    @Internal
    DirectoryProperty getDestinationDir() {
        destinationDir
    }

    void setDestinationDir(Provider<Directory> value) {
        destinationDir.set(value)
    }

    void setDestinationDir(File value) {
        destinationDir.set(value)
    }

    private final RegularFileProperty keychain = objects.fileProperty()

    @OutputFile
    RegularFileProperty getKeychain() {
        keychain
    }

    void setKeychain(Provider<RegularFile> value) {
        keychain.set(value)
    }

    void setKeychain(File value) {
        keychain.set(value)
    }
}
