package wooga.gradle.macOS.security.tasks

import com.wooga.security.Domain
import com.wooga.security.MacOsKeychain
import com.wooga.security.MacOsKeychainSearchList
import com.wooga.spock.extensios.security.Keychain
import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.build.IntegrationSpec

abstract class KeychainSearchListSpec extends IntegrationSpec {

    abstract String getTestTaskName()

    abstract Class getTaskType()

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
        task ${testTaskName}(type: ${taskType.name}) {
        }
        """.stripIndent()
    }

    def cleanup() {
        keychainSearchList.reset()
    }


    @Unroll("can set property #property with #method and type #type")
    def "can set property keychainSearchListSpec"() {
        given: "a task to read back the value"
        buildFile << """
            task("readValue") {
                doLast {
                    println("property: " + ${testTaskName}.${property}.get())
                }
            }
        """.stripIndent()
        and: "a set property"

        buildFile << """
            ${testTaskName}.${method}($value)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        outputContains(result, "property: " + expectedValue.toString())

        where:
        property | method       | rawValue  | type
        "domain" | "domain"     | "user"    | "String"
        "domain" | "domain"     | "system"  | "Domain"
        "domain" | "domain"     | "common"  | "Provider<Domain>"
        "domain" | "domain.set" | "dynamic" | "Domain"
        "domain" | "domain.set" | "user"    | "Provider<Domain>"
        "domain" | "setDomain"  | "system"  | "String"
        "domain" | "setDomain"  | "common"  | "Domain"
        "domain" | "setDomain"  | "dynamic" | "Provider<Domain>"

        value = wrapValueBasedOnType(rawValue, type) { type ->

            switch (type) {
                case Domain.simpleName:
                    return "${Domain.class.name}.valueOf('${rawValue.toString()}')"
                default:
                    return rawValue
            }
        }
        expectedValue = rawValue
    }
}
