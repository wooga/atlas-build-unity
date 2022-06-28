/*
 * Copyright 2018-2020 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.macOS.security.tasks

import com.wooga.security.MacOsKeychain
import com.wooga.spock.extensios.security.Keychain
import spock.lang.Requires
import spock.lang.Unroll
import wooga.gradle.build.IntegrationSpec

@Requires({ os.macOs })
class SecurityLockKeychainIntegrationSpec extends IntegrationSpec {
    String testTaskName = "lockKeychain"

    Class taskType = SecurityLockKeychain

    @Keychain(password = "123456")
    MacOsKeychain buildKeychain

    def setup() {
        buildFile << """
        task ${testTaskName}(type: ${taskType.name}) {
        }
        """.stripIndent()
    }

    def "task locks configured keychain"() {
        given: "an unlocked keychain"
        buildFile << """
        ${testTaskName} {
            keychain(file('${buildKeychain.location.path}'))
        }
        """.stripIndent()
        buildKeychain.unlock()

        expect:
        runTasksSuccessfully(testTaskName)
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
        property    | method          | rawValue         | type
        "all"       | "all"           | false            | "Provider<Boolean>"
        "all"       | "all.set"       | false            | "Boolean"
        "all"       | "all.set"       | false            | "Provider<Boolean>"
        "all"       | "setAll"        | false            | "Boolean"
        "all"       | "setAll"        | false            | "Provider<Boolean>"
        value = wrapValueBasedOnType(rawValue, type)
        expectedValue = rawValue
    }

    @Unroll("method #method #message")
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
        method         | rawValue                          | type                   | appends
        "keychain"     | "/some/path/1"                    | "File"                 | true
        "keychain"     | "/some/path/2"                    | "Provider<File>"       | true
        "keychains"    | ["/some/path/3", "/some/path/4"]  | "List<File>"           | true
        "keychains"    | ["/some/path/5", "/some/path/6"]  | "Provider<List<File>>" | true
        "setKeychains" | ["/some/path/7", "/some/path/8"]  | "List<File>"           | false
        "setKeychains" | ["/some/path/9", "/some/path/10"] | "Provider<List<File>>" | false

        baseValue = ["/some/path/0"]
        value = wrapValueBasedOnType(rawValue, type)
        baseValueWrapped = wrapValueBasedOnType(baseValue, "List<File>")
        expectedValue = appends ? [baseValue, [rawValue]].flatten() : [rawValue].flatten()
        message = appends ? "appends to keychains collection" : "set keychains collection"
    }

    def "task is never Up-to-date"() {
        given: "a keychain"
        buildFile << """
        ${testTaskName} {
            keychain(file('${buildKeychain.location.path}'))
        }
        """.stripIndent()

        and: "a run of the task"
        runTasksSuccessfully(testTaskName)

        when:
        def result = runTasksSuccessfully(testTaskName)

        then:
        !result.wasUpToDate(testTaskName)
    }
}
