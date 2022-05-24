package wooga.gradle.fastlane.models

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.OutputFiles

trait SighRenewSpec extends BaseSpec {

    private final Property<String> fileName = objects.property(String)

    @Internal
    Property<String> getFileName() {
        fileName
    }

    void setFileName(String value) {
        fileName.set(value)
    }

    void setFileName(Provider<String> value) {
        fileName.set(value)
    }

    private final Property<String> provisioningName = objects.property(String)

    @Internal
    Property<String> getProvisioningName() {
        provisioningName
    }

    void setProvisioningName(String value) {
        provisioningName.set(value)
    }

    void setProvisioningName(Provider<String> value) {
        provisioningName.set(value)
    }

    private final Property<Boolean> adhoc = objects.property(Boolean)

    @Internal
    Property<Boolean> getAdhoc() {
        adhoc
    }

    void setAdhoc(Boolean value) {
        adhoc.set(value)
    }

    void setAdhoc(Provider<Boolean> value) {
        adhoc.set(value)
    }

    private final DirectoryProperty destinationDir = objects.directoryProperty()

    @Internal
    DirectoryProperty getDestinationDir() {
        destinationDir
    }

    void setDestinationDir(File value) {
        destinationDir.set(value)
    }

    void setDestinationDir(Provider<Directory> value) {
        destinationDir.set(value)
    }

    private final Property<Boolean> readOnly = objects.property(Boolean)

    @Internal
    Property<Boolean> getReadOnly() {
        readOnly
    }

    void setReadOnly(Boolean value) {
        readOnly.set(value)
    }

    void setReadOnly(Provider<Boolean> value) {
        readOnly.set(value)
    }

    private final Property<Boolean> ignoreProfilesWithDifferentName = objects.property(Boolean)

    @Internal
    Property<Boolean> getIgnoreProfilesWithDifferentName() {
        ignoreProfilesWithDifferentName
    }

    void setIgnoreProfilesWithDifferentName(Boolean value) {
        ignoreProfilesWithDifferentName.set(value)
    }

    void setIgnoreProfilesWithDifferentName(Provider<Boolean> value) {
        ignoreProfilesWithDifferentName.set(value)
    }
}
