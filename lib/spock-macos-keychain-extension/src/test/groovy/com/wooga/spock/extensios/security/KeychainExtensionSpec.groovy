package com.wooga.spock.extensios.security

import com.wooga.security.MacOsKeychain
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification

@Requires({ os.macOs })
class KeychainExtensionSpec extends Specification {

    @Keychain
    MacOsKeychain keychain

    @Shared
    @Keychain
    MacOsKeychain sharedKeychain

    @Shared
    Iterable<MacOsKeychain> testKeychains = []

    def "creates a macOS keychain"() {
        expect:
        keychain.exists()
        sharedKeychain.exists()
    }

    @Keychain
    def "can annotate feature method with a keychain"(MacOsKeychain keychain) {
        expect:
        keychain.exists()
    }

    @Keychain
    def "can annotate a feature method with a keychain with iteration"(Boolean theValue, MacOsKeychain keychain) {
        expect:
        keychain.exists()
        !testKeychains.contains(keychain)

        cleanup:
        testKeychains << keychain

        where:
        theValue | _
        false    | _
        true     | _

        and:
        keychain = null
    }

    @Keychain(fileName = "custom.keychain", password = "78910", timeout = 2000, lockWhenSystemSleeps = true, unlockKeychain = true)
    def "can set initializer settings for created keychain"(MacOsKeychain keychain) {
        expect:
        keychain.location.name == "custom.keychain"
        keychain.password == "78910"
        keychain.timeout == 2000
        keychain.lockWhenSystemSleeps
    }


    def cleanup() {
        testKeychains << keychain
    }

    def cleanupSpec() {
        assert testKeychains.every({ !it.exists() })
    }
}
