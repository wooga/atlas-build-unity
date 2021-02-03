package wooga.gradle.macOS.security.tasks


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

    @Unroll("method :#method with type #type #message")
    def "method alters keychains property"() {
        given: "a task to read back the value"
        buildFile << """
            task("readValue") {
                doLast {
                    println("property: " + ${testTaskName}.keychains.files)
                }
            }
        """.stripIndent()
        and: "a set property"
        buildFile << """
            ${testTaskName}.keychains.setFrom($baseValueWrapped)
            ${testTaskName}.${method}($value)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        outputContains(result, "property: " + expectedValue.toString())

        where:
        method         | rawValue                          | type                       | appends
        "keychain"     | "/some/path/1"                    | "File"                     | true
        "keychain"     | "/some/path/2"                    | "Provider<File>"           | true
        "keychains"    | ["/some/path/3", "/some/path/4"]  | "Iterable<File>"           | true
        "keychains"    | ["/some/path/5", "/some/path/6"]  | "Provider<Iterable<File>>" | true
        "setKeychains" | ["/some/path/7", "/some/path/8"]  | "Iterable<File>"           | false
        "setKeychains" | ["/some/path/9", "/some/path/10"] | "Provider<Iterable<File>>" | false

        baseValue = ["/some/path/0"]
        value = wrapValueBasedOnType(rawValue, type)
        baseValueWrapped = wrapValueBasedOnType(baseValue, "List<File>")
        expectedValue = appends ? [baseValue, [rawValue]].flatten() : [rawValue].flatten()
        message = appends ? "appends to keychains collection" : "set keychains collection"
    }
}
