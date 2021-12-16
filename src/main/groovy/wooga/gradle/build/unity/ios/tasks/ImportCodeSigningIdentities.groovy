package wooga.gradle.build.unity.ios.tasks

import com.wooga.security.FindIdentityResult
import com.wooga.security.command.FindIdentity
import com.wooga.security.command.Import
import com.wooga.security.command.LockKeychain
import com.wooga.security.command.UnlockKeychain
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import wooga.gradle.macOS.security.SecurityKeychainOutputSpec

class ImportCodeSigningIdentities extends DefaultTask implements SecurityKeychainOutputSpec {

    @InputFiles
    @SkipWhenEmpty
    protected getInputFiles() {
        if (p12.isPresent()) {
            return project.files(p12, inputKeychain)
        }
        project.files()
    }

    private final RegularFileProperty p12 = objects.fileProperty()

    @InputFile
    @Optional
    RegularFileProperty getP12() {
        p12
    }

    void setP12(File value) {
        p12.set(value)
    }

    void setP12(Provider<RegularFile> value) {
        p12.set(value)
    }

    private final Property<String> passphrase = objects.property(String)

    @Input
    Property<String> getPassphrase() {
        return passphrase
    }

    void setPassphrase(String value) {
        passphrase.set(value)
    }

    void setPassphrase(Provider<String> value) {
        passphrase.set(value)
    }

    private final Property<Boolean> ignoreInvalidSigningIdentity = objects.property(Boolean)

    @Optional
    @Input
    Property<Boolean> getIgnoreInvalidSigningIdentity() {
        return ignoreInvalidSigningIdentity
    }

    void setIgnoreInvalidSigningIdentity(Boolean value) {
        ignoreInvalidSigningIdentity.set(value)
    }

    void setIgnoreInvalidSigningIdentity(Provider<Boolean> value) {
        ignoreInvalidSigningIdentity.set(value)
    }

    private final RegularFileProperty inputKeychain = objects.fileProperty()

    @InputFile
    RegularFileProperty getInputKeychain() {
        inputKeychain
    }

    void setInputKeychain(Provider<RegularFile> value) {
        inputKeychain.set(value)
    }

    void setInputKeychain(File value) {
        inputKeychain.set(value)
    }

    private final Property<String> password = objects.property(String)

    @Input
    @Optional
    Property<String> getPassword() {
        password
    }

    void setPassword(Provider<String> value) {
        password.set(value)
    }

    void setPassword(String value) {
        password.set(value)
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

    private final ListProperty<String> applicationAccessPaths = objects.listProperty(String)

    @Input
    ListProperty<String> getApplicationAccessPaths() {
        applicationAccessPaths
    }

    void setApplicationAccessPaths(Provider<List<String>> value) {
        applicationAccessPaths.set(value)
    }

    void setApplicationAccessPaths(List<String> value) {
        applicationAccessPaths.set(value)
    }

    void applicationAccessPath(String value) {
        applicationAccessPaths.add(value)
    }

    void applicationAccessPath(Provider<String> value) {
        applicationAccessPaths.add(value)
    }

    ImportCodeSigningIdentities() {
        outputs.upToDateWhen {
            def ignoreInvalidSigningIdentity = ignoreInvalidSigningIdentity.getOrElse(false)
            def signingIdentities = signingIdentities.get()
            if (!signingIdentities.isEmpty()) {
                return signingIdentities.every { hasSigningIdentity(it, !ignoreInvalidSigningIdentity) }
            }
            false
        }

        fileName.convention(baseName.map({
            if (extension.present) {
                return it + "." + extension.get()
            }
            it
        }))

        keychain.convention(destinationDir.file(fileName))
        inputKeychain.convention(keychain)
    }

    Boolean hasSigningIdentity(String identity, Boolean validIdentities = true) {
        if (!keychain.get().asFile.exists()) {
            return false
        }

        def command = new FindIdentity()
                .withKeychain(keychain.get().asFile)
                .withPolicy(FindIdentity.Policy.Codesigning)
                .validIdentities(validIdentities)
        def foundIdentity = command.execute()
        if (foundIdentity == null || foundIdentity.isEmpty()) {
            return false
        }

        FindIdentityResult identityResult = FindIdentityResult.fromOutPut(foundIdentity)
        identityResult.hasIdentity(identity, validIdentities)
    }

    @TaskAction
    protected void importCodeSigningIdentity() {
        def inputKeychain = inputKeychain.get().asFile
        def keychain = keychain.get().asFile

        if (inputKeychain.canonicalPath != keychain.canonicalPath) {
            FileUtils.copyFile(inputKeychain, keychain)
        }

        if (p12.isPresent()) {
            if (password.present) {
                new UnlockKeychain().withKeychain(keychain).withPassword(password.get()).execute()
            }

            def importCertificates = new Import(p12.get().asFile, keychain)
                    .withPassphrase(passphrase.get())
                    .withType(Import.Type.Cert)
                    .withFormat(Import.Format.Pkcs12)

            applicationAccessPaths.get().each {
                importCertificates.allowAccessFrom(it)
            }

            importCertificates.execute()

            if (password.present) {
                new LockKeychain().withKeychain(keychain)
            }

            if (signingIdentities.present) {
                signingIdentities.get().each { String signingIdentity ->
                    if (!hasSigningIdentity(signingIdentity, false)) {
                        throw new GradleException("Unable to find code sign identity '${signingIdentity}' in keychain ${keychain.path}")
                    } else {
                        if (hasSigningIdentity(signingIdentity)) {
                            logger.info("Signing Identity '${signingIdentity}' successfull imported into keychain ${keychain.path}")
                        } else if (!ignoreInvalidSigningIdentity.get()) {
                            throw new GradleException("Unable to find valid code sign identity '${signingIdentity}' in keychain ${keychain.path}")
                        } else {
                            logger.warn("Signing Identity '${signingIdentity}' found but invalid")
                        }
                    }
                }
            } else {
                logger.info("No CodeSigningIdentity specified. Skip verification")
            }
        }
    }
}
