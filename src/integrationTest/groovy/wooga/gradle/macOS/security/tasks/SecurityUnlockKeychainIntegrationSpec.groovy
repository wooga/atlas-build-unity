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

import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter
import com.wooga.security.MacOsKeychain
import com.wooga.spock.extensios.security.Keychain
import spock.lang.Requires
import spock.lang.Unroll

import static com.wooga.gradle.test.writers.PropertySetInvocation.*

@Requires({ os.macOs })
class SecurityUnlockKeychainIntegrationSpec extends InteractiveSecurityTaskIntegrationSpec<SecurityUnlockKeychain> {

    @Keychain(password = "123456")
    MacOsKeychain buildKeychain

    def setup() {
        appendToSubjectTask """
            keychain = file('${buildKeychain.location.path}')
            password = "${buildKeychain.password}"
        """.stripIndent()
    }

    def "task unlocks configured keychain"() {
        given: "a locked keychain"
        buildKeychain.lock()

        expect:
        runTasksSuccessfully(subjectUnderTestName)
    }

    @Unroll("can set property #property with #method and type #type")
    def "can set property"() {
        expect:
        runPropertyQuery(get, set).matches(rawValue)

        where:
        property   | invocation  | rawValue       | type
        "password" | method      | "password2"    | "Provider<String>"
        "password" | providerSet | "password1"    | "String"
        "password" | providerSet | "password2"    | "Provider<String>"
        "password" | setter      | "password3"    | "String"
        "password" | setter      | "password4"    | "Provider<String>"

        "keychain" | method      | "/some/path/1" | "File"
        "keychain" | method      | "/some/path/2" | "Provider<RegularFile>"
        "keychain" | providerSet | "/some/path/3" | "File"
        "keychain" | providerSet | "/some/path/4" | "Provider<RegularFile>"
        "keychain" | setter      | "/some/path/5" | "File"
        "keychain" | setter      | "/some/path/6" | "Provider<RegularFile>"
        set = new PropertySetterWriter(subjectUnderTestName, property)
                .set(rawValue, type)
                .toScript(invocation)
                .serialize(wrapValueFallback)

        get = new PropertyGetterTaskWriter(set)
    }

    def "task is never Up-to-date"() {
        given: "a keychain"
        appendToSubjectTask("keychain(file('${buildKeychain.location.path}'))")

        and: "a run of the task"
        runTasksSuccessfully(subjectUnderTestName)

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        !result.wasUpToDate(subjectUnderTestName)
    }
}
