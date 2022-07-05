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
class SecurityLockKeychainIntegrationSpec extends InteractiveSecurityTaskIntegrationSpec<SecurityLockKeychain> {

    @Keychain(password = "123456")
    MacOsKeychain buildKeychain

    def "task locks configured keychain"() {
        given: "an unlocked keychain"
        appendToSubjectTask "keychain(file('${buildKeychain.location.path}'))"
        buildKeychain.unlock()

        expect:
        runTasksSuccessfully(subjectUnderTestName)
    }

    @Unroll("can set property #property with #invocation and type #type")
    def "can set property"() {
        expect:
        runPropertyQuery(get, set).matches(rawValue)

        where:
        property | invocation  | rawValue | type
        "all"    | providerSet | false    | "Boolean"
        "all"    | providerSet | false    | "Provider<Boolean>"
        "all"    | setter      | false    | "Boolean"
        "all"    | setter      | false    | "Provider<Boolean>"

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

@Requires({ os.macOs })
class SecurityLockKeychainIntegrationSpec2 extends MultiKeychainOperationIntegrationSpec<SecurityLockKeychain> {
}

