package wooga.gradle.build.unity.ios.tasks

import com.wooga.gradle.BaseSpec
import com.wooga.security.FindIdentityResult
import com.wooga.security.command.FindIdentity
import com.wooga.security.command.Import
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*

class ImportCodeSigningIdentities extends DefaultTask implements BaseSpec {

    @InputFiles
    @SkipWhenEmpty
    protected getInputFiles() {
        if (p12.isPresent()) {
            return project.files(p12, keychain)
        }
        project.files()
    }

    private final RegularFileProperty p12 = objects.fileProperty()

    @Internal
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

    private final RegularFileProperty keychain = objects.fileProperty()

    @InputFile
    RegularFileProperty getKeychain() {
        return keychain
    }

    void setKeychain(File value) {
        keychain.set(value)
    }

    void setKeychain(Provider<RegularFile> value) {
        keychain.set(value)
    }

    @OutputFile
    protected Provider<RegularFile> getOutputFile() {
        keychain
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

        onlyIf {
            /* We can only skip if the task does not
            configure the application access path. There is no
            way to check the current access rights for items in the keychain
            from the security cli tool. So we must treat the task as unskippable
            even if the identity might exist.
            */
            def ignoreInvalidSigningIdentity = ignoreInvalidSigningIdentity.getOrElse(false)
            def signingIdentities = signingIdentities.get()
            if (applicationAccessPaths.get().isEmpty() && !signingIdentities.isEmpty()) {
                return !signingIdentities.every({ hasSigningIdentity(it, !ignoreInvalidSigningIdentity) })
            }
            true
        }
    }

    Boolean hasSigningIdentity(String identity, Boolean validIdentities = true) {
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
        def keychain = keychain.get().asFile

        def importCertificates = new Import(p12.get().asFile, keychain)
                .withPassphrase(passphrase.get())
                .withType(Import.Type.Cert)
                .withFormat(Import.Format.Pkcs12)

        applicationAccessPaths.get().each {
            importCertificates.allowAccessFrom(it)
        }

        importCertificates.execute()

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
