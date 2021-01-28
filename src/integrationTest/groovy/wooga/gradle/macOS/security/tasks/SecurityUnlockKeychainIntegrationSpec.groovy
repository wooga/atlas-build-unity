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
class SecurityUnlockKeychainIntegrationSpec extends IntegrationSpec {
    String testTaskName = "unlockKeychain"

    Class taskType = SecurityUnlockKeychain

    @Keychain(password = "123456")
    MacOsKeychain buildKeychain

    def setup() {
        buildFile << """
        task ${testTaskName}(type: ${taskType.name}) {
            keychain = file('${buildKeychain.location.path}')
            password = "${buildKeychain.password}"
        }
        """.stripIndent()
    }

    def "task unlocks configured keychain"() {
        given: "a locked keychain"
        buildKeychain.lock()

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
        property   | method         | rawValue       | type
        "password" | "password"     | "password2"    | "Provider<String>"
        "password" | "password.set" | "password1"    | "String"
        "password" | "password.set" | "password2"    | "Provider<String>"
        "password" | "setPassword"  | "password3"    | "String"
        "password" | "setPassword"  | "password4"    | "Provider<String>"

        "keychain" | "keychain"     | "/some/path/1" | "File"
        "keychain" | "keychain"     | "/some/path/2" | "Provider<RegularFile>"
        "keychain" | "keychain.set" | "/some/path/3" | "File"
        "keychain" | "keychain.set" | "/some/path/4" | "Provider<RegularFile>"
        "keychain" | "setKeychain"  | "/some/path/5" | "File"
        "keychain" | "setKeychain"  | "/some/path/6" | "Provider<RegularFile>"
        value = wrapValueBasedOnType(rawValue, type)
        expectedValue = rawValue
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
