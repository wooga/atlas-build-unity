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

import static com.wooga.gradle.test.queries.TestValue.filePath
import static com.wooga.gradle.test.queries.TestValue.filePath
import static com.wooga.gradle.test.queries.TestValue.filePaths
import static com.wooga.gradle.test.queries.TestValue.filePaths
import static com.wooga.gradle.test.queries.TestValue.filePaths
import static com.wooga.gradle.test.queries.TestValue.filePaths
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
        "all"    | method      | false    | "Provider<Boolean>"
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

    @Unroll("method #invocation #message")
    def "method alters keychains property"() {
        given: "a set property"
        appendToSubjectTask("keychains = ${wrapValueBasedOnType(baseValue, "List<File>")}")

        and:
        if (appends) {
            rawValue.expectPrepend(baseValue)
        }

        expect:
        runPropertyQuery(get, set).matches(rawValue)

        where:
        invocation               | rawValue                                     | type                       | appends
        customSetter("keychain") | filePath("/some/path/1")                     | "File"                     | true
        customSetter("keychain") | filePath("/some/path/2")                     | "Provider<File>"           | true
        method                   | filePaths(["/some/path/3", "/some/path/4"])  | "Iterable<File>"           | true
        method                   | filePaths(["/some/path/5", "/some/path/6"])  | "Provider<Iterable<File>>" | true
        setter                   | filePaths(["/some/path/7", "/some/path/8"])  | "Iterable<File>"           | false
        setter                   | filePaths(["/some/path/9", "/some/path/10"]) | "Provider<Iterable<File>>" | false

        property = "keychains"
        baseValue = osPath("/some/path/0")
        message = appends ? "appends to keychains collection" : "set keychains collection"
        set = new PropertySetterWriter(subjectUnderTestName, property)
                .set(rawValue, type)
                .toScript(invocation)
        get = new PropertyGetterTaskWriter(subjectUnderTestName + ".keychains.files", "")
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
