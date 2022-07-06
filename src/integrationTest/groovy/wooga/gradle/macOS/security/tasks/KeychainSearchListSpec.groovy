package wooga.gradle.macOS.security.tasks

import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter
import com.wooga.security.MacOsKeychain
import com.wooga.security.MacOsKeychainSearchList
import com.wooga.spock.extensios.security.Keychain
import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.macOS.security.SecurityTaskIntegrationSpec

import static com.wooga.gradle.test.writers.PropertySetInvocation.*

abstract class KeychainSearchListSpec<T extends AbstractSecurityKeychainSearchListTask> extends SecurityTaskIntegrationSpec<T> {
    MacOsKeychainSearchList keychainSearchList = new MacOsKeychainSearchList()

    @Shared
    @Keychain
    MacOsKeychain buildKeychain

    @Shared
    @Keychain
    MacOsKeychain buildKeychain2

    @Shared
    @Keychain
    MacOsKeychain buildKeychain3

    def setup() {
        keychainSearchList.reset()
        buildFile << """
        task ${subjectUnderTestName}(type: ${subjectUnderTestTypeName}) {
        }
        """.stripIndent()
    }

    def cleanup() {
        keychainSearchList.reset()
    }


    @Unroll("can set property #property with #invocation and type #type")
    def "can set property keychainSearchListSpec"() {
        expect:
        runPropertyQuery(get, set).matches(rawValue)

        where:
        property | invocation  | rawValue  | type
        "domain" | method      | "user"    | "String"
        "domain" | method      | "system"  | "Domain"
        "domain" | method      | "common"  | "Provider<Domain>"
        "domain" | providerSet | "dynamic" | "Domain"
        "domain" | providerSet | "user"    | "Provider<Domain>"
        "domain" | setter      | "system"  | "String"
        "domain" | setter      | "common"  | "Domain"
        "domain" | setter      | "dynamic" | "Provider<Domain>"

        set = new PropertySetterWriter(subjectUnderTestName, property)
                .set(rawValue, type)
                .toScript(invocation)
                .serialize(wrapValueFallback)

        get = new PropertyGetterTaskWriter(set)
    }
}
