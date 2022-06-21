package wooga.gradle.macOS.security.tasks

import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.queries.TestValue
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetInvocation
import com.wooga.gradle.test.writers.PropertySetterWriter
import org.apache.tools.ant.types.PropertySet
import spock.lang.Requires
import spock.lang.Unroll

@Requires({ os.macOs && env['ATLAS_BUILD_UNITY_IOS_EXECUTE_KEYCHAIN_SPEC'] == 'YES' })
class SecuritySetKeychainSearchListIntegrationSpec extends KeychainSearchListSpec {
    String testTaskName = "keychainLookup"
    Class taskType = SecuritySetKeychainSearchList

    @Unroll
    def "can add #message to the lookup list"() {
        given: "a lookup list without the keychain added"
        keychains.each {
            assert !keychainSearchList.contains(it.location)
        }

        and: "the keychain configured"
        buildFile << """
        ${testTaskName} {
            action = "add"
            keychains(files('${keychains.collect { it.location.path }.join('\', \'')}'))
        }
        """.stripIndent()

        when:
        runTasksSuccessfully(testTaskName)

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
        buildFile << """
        ${testTaskName} {
            action = "remove"
            keychains(files('${keychains.collect { it.location.path }.join('\', \'')}'))
        }
        """.stripIndent()

        when:
        runTasksSuccessfully(testTaskName)

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
        buildFile << """
        ${testTaskName} {
            action = "${action}"
        }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(testTaskName)

        then:
        result.standardOutput.contains("Task :${testTaskName} NO-SOURCE")

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
        buildFile << """
        ${testTaskName} {
            action = "${action}"
            keychains(files('${keychains.collect { it.location.path }.join('\', \'')}'))
        }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(testTaskName)

        then:
        result.wasSkipped(testTaskName)

        where:
        action   | keychains                       | keychainsInSearchList                           | message
        "add"    | [buildKeychain, buildKeychain2] | [buildKeychain, buildKeychain2, buildKeychain3] | "all keychains already added"
        "remove" | [buildKeychain3]                | [buildKeychain, buildKeychain2]                 | "keychain to remove not in search list"
    }

    @Unroll("can set property #property with #method and type #type")
    def "can set property"() {
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
        property | method       | rawValue | type
        "action" | "action"     | "add"    | "String"
        "action" | "action"     | "remove" | "Action"
        "action" | "action"     | "add"    | "Provider<Action>"
        "action" | "action.set" | "remove" | "Action"
        "action" | "action.set" | "add"    | "Provider<Action>"
        "action" | "setAction"  | "remove" | "String"
        "action" | "setAction"  | "add"    | "Action"
        "action" | "setAction"  | "remove" | "Provider<Action>"

        value = wrapValueBasedOnType(rawValue, type) { type ->

            switch (type) {
                case SecuritySetKeychainSearchList.Action.simpleName:
                    return "${SecuritySetKeychainSearchList.Action.class.name}.valueOf('${rawValue.toString()}')"
                default:
                    return rawValue
            }
        }
        expectedValue = rawValue
    }

    @Unroll("sets value #rawValue with #method, type #type #message")
    def "method alters keychains property"() {
        given: "a set property"
        buildFile << """
            ${testTaskName}.keychains.setFrom(${wrapValueBasedOnType([baseValue], "List<File>")})
        """.stripIndent()

        and:
        if (appends) {
            rawValue.expectPrepend(baseValue)
        }

        when:
        def setter = new PropertySetterWriter(testTaskName, property)
            .set(rawValue, type)
            .toScript(method)

        def getter = new PropertyGetterTaskWriter(testTaskName + ".keychains.files", "")
        def query = runPropertyQuery(getter, setter)

        then:
        query.matches(rawValue)

        where:
        method                                         | rawValue                                               | type                       | appends
        PropertySetInvocation.customSetter("keychain") | TestValue.filePath("/some/path/1")                     | "File"                     | true
        PropertySetInvocation.customSetter("keychain") | TestValue.filePath("/some/path/2")                     | "Provider<File>"           | true
        PropertySetInvocation.method                   | TestValue.filePaths(["/some/path/3", "/some/path/4"])  | "Iterable<File>"           | true
        PropertySetInvocation.method                   | TestValue.filePaths(["/some/path/5", "/some/path/6"])  | "Provider<Iterable<File>>" | true
        PropertySetInvocation.setter                   | TestValue.filePaths(["/some/path/7", "/some/path/8"])  | "Iterable<File>"           | false
        PropertySetInvocation.setter                   | TestValue.filePaths(["/some/path/9", "/some/path/10"]) | "Provider<Iterable<File>>" | false

        property = "keychains"
        baseValue = osPath("/some/path/0")
        message = appends ? "appends to keychains collection" : "set keychains collection"
    }
}
