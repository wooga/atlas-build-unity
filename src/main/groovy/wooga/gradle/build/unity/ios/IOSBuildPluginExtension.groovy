package wooga.gradle.build.unity.ios

import com.wooga.gradle.BaseSpec
import org.gradle.api.Action
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input

trait IOSBuildPluginExtension extends BaseSpec {

    @Deprecated
    abstract PasswordCredentials getFastlaneCredentials()

    @Deprecated
    abstract void setFastlaneCredentials(PasswordCredentials cred)

    @Deprecated
    abstract IOSBuildPluginExtension fastlaneCredentials(Closure configuration)

    @Deprecated
    abstract IOSBuildPluginExtension fastlaneCredentials(Action<PasswordCredentials> action)

    @Deprecated
    abstract IOSBuildPluginExtension fastlaneCredentials(PasswordCredentials cred)

    private final Property<String> keychainPassword = objects.property(String)

    Property<String> getKeychainPassword() {
        keychainPassword
    }

    void setKeychainPassword(Provider<String> value) {
        keychainPassword.set(value)
    }

    void setKeychainPassword(String value) {
        keychainPassword.set(value)
    }

    private final ListProperty<String> signingIdentities = objects.listProperty(String)

    @Input
    ListProperty<String> getSigningIdentities() {
        signingIdentities
    }

    void setSigningIdentities(Provider<List<String>> value) {
        signingIdentities.set(value)
    }

    void setSigningIdentities(List<String> value) {
        signingIdentities.set(value)
    }

    void setSigningIdentity(String value) {
        signingIdentities.empty().add(value)
    }

    void setSigningIdentity(Provider<String> value) {
        signingIdentities.empty().add(value)
    }

    void signingIdentity(String value) {
        signingIdentities.add(value)
    }

    void signingIdentity(Provider<String> value) {
        signingIdentities.add(value)
    }

    private final RegularFileProperty codeSigningIdentityFile = objects.fileProperty()

    Property<RegularFile> getCodeSigningIdentityFile() {
        codeSigningIdentityFile
    }

    void setCodeSigningIdentityFile(Provider<RegularFile> value) {
        codeSigningIdentityFile.set(value)
    }

    void setCodeSigningIdentityFile(File value) {
        codeSigningIdentityFile.set(value)
    }

    private final Property<String> codeSigningIdentityFilePassphrase = objects.property(String)

    Property<String> getCodeSigningIdentityFilePassphrase() {
        codeSigningIdentityFilePassphrase
    }

    void setCodeSigningIdentityFilePassphrase(Provider<String> value) {
        codeSigningIdentityFilePassphrase.set(value)
    }

    void setCodeSigningIdentityFilePassphrase(String value) {
        codeSigningIdentityFilePassphrase.set(value)
    }

    private final Property<String> appIdentifier = objects.property(String)

    Property<String> getAppIdentifier() {
        appIdentifier
    }

    void setAppIdentifier(Provider<String> value) {
        appIdentifier.set(value)
    }

    void setAppIdentifier(String value) {
        appIdentifier.set(value)
    }

    private final Property<String> teamId = objects.property(String)

    Property<String> getTeamId() {
        teamId
    }

    void setTeamId(Provider<String> value) {
        teamId.set(value)
    }

    void setTeamId(String value) {
        teamId.set(value)
    }

    private final Property<String> scheme = objects.property(String)

    Property<String> getScheme() {
        scheme
    }

    void setScheme(Provider<String> value) {
        scheme.set(value)
    }

    void setScheme(String value) {
        scheme.set(value)
    }

    private final Property<String> configuration = objects.property(String)

    Property<String> getConfiguration() {
        configuration
    }

    void setConfiguration(Provider<String> value) {
        configuration.set(value)
    }

    void setConfiguration(String value) {
        configuration.set(value)
    }

    private final Property<String> provisioningName = objects.property(String)

    Property<String> getProvisioningName() {
        provisioningName
    }

    void setProvisioningName(Provider<String> value) {
        provisioningName.set(value)
    }

    void setProvisioningName(String value) {
        provisioningName.set(value)
    }

    private final Property<Boolean> adhoc = objects.property(Boolean)

    Property<Boolean> getAdhoc() {
        adhoc
    }

    void setAdhoc(Provider<Boolean> value) {
        adhoc.set(value)
    }

    void setAdhoc(Boolean value) {
        adhoc.set(value)
    }

    private final Property<Boolean> publishToTestFlight = objects.property(Boolean)

    Property<Boolean> getPublishToTestFlight() {
        publishToTestFlight
    }

    void setPublishToTestFlight(Provider<Boolean> value) {
        publishToTestFlight.set(value)
    }

    void setPublishToTestFlight(Boolean value) {
        publishToTestFlight.set(value)
    }

    private final RegularFileProperty exportOptionsPlist = objects.fileProperty()

    @Input
    RegularFileProperty getExportOptionsPlist() {
        exportOptionsPlist
    }

    void setExportOptionsPlist(Provider<RegularFile> value) {
        exportOptionsPlist.set(value)
    }

    void setExportOptionsPlist(File value) {
        exportOptionsPlist.set(value)
    }

}
