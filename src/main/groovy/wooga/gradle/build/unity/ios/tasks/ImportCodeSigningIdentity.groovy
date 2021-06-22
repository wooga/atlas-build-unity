package wooga.gradle.build.unity.ios.tasks

import com.wooga.security.FindIdentityResult
import com.wooga.security.command.FindIdentity
import com.wooga.security.command.Import
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

class ImportCodeSigningIdentity extends DefaultTask {
    @InputFile
    final RegularFileProperty p12

    void setP12(File value) {
        p12.set(value)
    }

    void setP12(Provider<RegularFile> value) {
        p12.set(value)
    }

    ImportCodeSigningIdentity p12(File value) {
        setP12(value)
        this
    }

    ImportCodeSigningIdentity p12(Provider<RegularFile> value) {
        setP12(value)
        this
    }

    @Input
    final Property<String> passphrase

    void setPassphrase(String value) {
        passphrase.set(value)
    }

    void setPassphrase(Provider<String> value) {
        passphrase.set(value)
    }

    ImportCodeSigningIdentity passphrase(String value) {
        setPassphrase(value)
        this
    }

    ImportCodeSigningIdentity passphrase(Provider<String> value) {
        setPassphrase(value)
        this
    }

    @Input
    final Property<Boolean> ignoreInvalidSigningIdentity

    void setIgnoreInvalidSigningIdentity(Boolean value) {
        ignoreInvalidSigningIdentity.set(value)
    }

    void setIgnoreInvalidSigningIdentity(Provider<Boolean> value) {
        ignoreInvalidSigningIdentity.set(value)
    }

    ImportCodeSigningIdentity ignoreInvalidSigningIdentity(Boolean value) {
        setIgnoreInvalidSigningIdentity(value)
        this
    }

    ImportCodeSigningIdentity ignoreInvalidSigningIdentity(Provider<Boolean> value) {
        setIgnoreInvalidSigningIdentity(value)
        this
    }

    @InputFile
    final RegularFileProperty keychain

    void setKeychain(File value) {
        keychain.set(value)
    }

    void setKeychain(Provider<RegularFile> value) {
        keychain.set(value)
    }

    ImportCodeSigningIdentity keychain(File value) {
        setKeychain(value)
        this
    }

    ImportCodeSigningIdentity keychain(Provider<RegularFile> value) {
        setKeychain(value)
        this
    }

    @Input
    final Property<String> signingIdentity

    void setSigningIdentity(String value) {
        signingIdentity.set(value)
    }

    void setSigningIdentity(Provider<String> value) {
        signingIdentity.set(value)
    }

    ImportCodeSigningIdentity signingIdentity(String value) {
        setSigningIdentity(value)
        this
    }

    ImportCodeSigningIdentity signingIdentity(Provider<String> value) {
        setSigningIdentity(value)
        this
    }

    ImportCodeSigningIdentity() {
        keychain = project.objects.fileProperty()
        p12 = project.objects.fileProperty()
        passphrase = project.objects.property(String)
        signingIdentity = project.objects.property(String)
        ignoreInvalidSigningIdentity = project.objects.property(Boolean)

        onlyIf(new Spec<Task>() {
            @Override
            boolean isSatisfiedBy(Task task) {
                return !hasSigningIdentity(signingIdentity.get(), !ignoreInvalidSigningIdentity.get())
            }
        })
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
        def signingIdentity = signingIdentity.get()
        def keychain = keychain.get().asFile

        def importCertificates = new Import(p12.get().asFile, keychain)
                .withPassphrase(passphrase.get())
                .withType(Import.Type.Cert)
                .withFormat(Import.Format.Pkcs12)
        importCertificates.execute()

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
}
