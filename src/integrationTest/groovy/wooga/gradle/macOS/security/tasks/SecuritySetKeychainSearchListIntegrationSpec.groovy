package wooga.gradle.macOS.security.tasks


import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter
import spock.lang.Requires
import spock.lang.Unroll

import static com.wooga.gradle.test.writers.PropertySetInvocation.*

@Requires({ os.macOs && env['ATLAS_BUILD_UNITY_IOS_EXECUTE_KEYCHAIN_SPEC'] == 'YES' })
class SecuritySetKeychainSearchListIntegrationSpec extends KeychainSearchListSpec<SecuritySetKeychainSearchList> {
    @Unroll
    def "can add #message to the lookup list"() {
        given: "a lookup list without the keychain added"
        keychains.each {
            assert !keychainSearchList.contains(it.location)
        }

        and: "the keychain configured"
        appendToSubjectTask """
            action = "add"
            keychains(files('${keychains.collect { it.location.path }.join('\', \'')}'))
        """.stripIndent()

        when:
        runTasksSuccessfully(subjectUnderTestName)

        then:
        keychains.every {
            keychainSearchList.contains(it.location)
        }

        where:
        keychains                                       | message
        [buildKeychain]                                 | "a single keychain"
        [buildKeychain, buildKeychain2, buildKeychain3] | "multiple keychains"
    }

    @Unroll
    def "can remove #message to the lookup list"() {
        given: "a lookup list without the keychain added"
        keychainSearchList.addAll(keychains.collect { it.location })
        keychains.each {
            assert keychainSearchList.contains(it.location)
        }

        and: "the keychain configured"
        appendToSubjectTask("""
            action = "remove"
            keychains(files('${keychains.collect { it.location.path }.join('\', \'')}'))
        """.stripIndent())

        when:
        runTasksSuccessfully(subjectUnderTestName)

        then:
        !keychains.every {
            keychainSearchList.contains(it.location)
        }

        where:
        keychains                                       | message
        [buildKeychain]                                 | "a single keychain"
        [buildKeychain, buildKeychain2, buildKeychain3] | "multiple keychains"
    }

    @Unroll
    def "skips with no source when action is #action and no keychains are configured"() {
        given:
        appendToSubjectTask("""
            action = "${action}"
        """.stripIndent())

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.standardOutput.contains("Task :${subjectUnderTestName} NO-SOURCE")

        where:
        action << ["add", "remove"]
    }

    @Unroll
    def "skipped when #message"() {
        given: "keychains in the search list"
        keychainSearchList.addAll(keychainsInSearchList.collect { it.location })
        keychainsInSearchList.each {
            assert keychainSearchList.contains(it.location)
        }

        and: "test task configured with action and keychains"
        appendToSubjectTask("""
            action = "${action}"
            keychains(files('${keychains.collect { it.location.path }.join('\', \'')}'))
        """.stripIndent())

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.wasSkipped(subjectUnderTestName)

        where:
        action   | keychains                       | keychainsInSearchList                           | message
        "add"    | [buildKeychain, buildKeychain2] | [buildKeychain, buildKeychain2, buildKeychain3] | "all keychains already added"
        "remove" | [buildKeychain3]                | [buildKeychain, buildKeychain2]                 | "keychain to remove not in search list"
    }

    @Unroll("can set property #property with #methodName and type #type")
    def "can set property"() {
        expect:
        runPropertyQuery(get, set).matches(rawValue)

        where:
        property | invocation  | rawValue | type
        "action" | providerSet | "remove" | "Action"
        "action" | providerSet | "add"    | "Provider<Action>"
        "action" | setter      | "remove" | "String"
        "action" | setter      | "add"    | "Action"
        "action" | setter      | "remove" | "Provider<Action>"

        set = new PropertySetterWriter(subjectUnderTestName, property)
                .set(rawValue, type)
                .toScript(invocation)
                .serialize(wrapValueFallback)

        get = new PropertyGetterTaskWriter(set)

        value = wrapValueBasedOnType(rawValue, type)
        expectedValue = rawValue
    }
}

@Requires({ os.macOs })
class SecuritySetKeychainSearchListIntegrationSpec2 extends MultiKeychainOperationIntegrationSpec<SecuritySetKeychainSearchList> {
}
