/*
 * Copyright 2018 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.macOS.security.tasks

import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter
import com.wooga.security.MacOsKeychain
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Unroll

import static com.wooga.gradle.test.writers.PropertySetInvocation.*

@Requires({ os.macOs })
class SecurityCreateKeychainIntegrationSpec extends InteractiveSecurityTaskIntegrationSpec<SecurityCreateKeychain> {

    @Shared
    File buildKeychain

    def setup() {
        appendToSubjectTask """
            baseName = "build"
            extension = "keychain"
            destinationDir = file("build/sign/keychains")
            password = "${keychainPassword}"
        """.stripIndent()

        buildKeychain = new File(projectDir, 'build/sign/keychains/build.keychain')
    }

    def "creates a keychain"() {
        given: "a future keychain"
        assert !buildKeychain.exists()

        when:
        runTasksSuccessfully(subjectUnderTestName)

        then:
        buildKeychain.exists()
    }

    def "buildKeychain caches task outputs"() {
        given: "a gradle run with buildKeychain"
        runTasksSuccessfully(subjectUnderTestName)

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.wasUpToDate(subjectUnderTestName)
    }

    @Unroll
    def "createKeychain is not [UP-TO-DATE] when #reason"() {
        given: "a gradle run with buildKeychain"
        runTasksSuccessfully(subjectUnderTestName)

        when:
        buildKeychain.delete()
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        !result.wasUpToDate(subjectUnderTestName)
    }

    def "does not print password to stdout"() {
        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        !outputContains(result, "-p ${keychainPassword}")
        outputContains(result, "-p ****")
    }

    @Unroll
    def "can set keychain settings for created keychain"() {
        given: "custom lock settings"
        if (lockKeychainWhenSleep != _) {
            appendToSubjectTask("lockKeychainWhenSleep = ${lockKeychainWhenSleep}")
        }

        if (lockKeychainAfterTimeout != _) {
            appendToSubjectTask("lockKeychainAfterTimeout = ${lockKeychainAfterTimeout}")
        }

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        def keychain = new MacOsKeychain(buildKeychain, keychainPassword)
        if (lockKeychainAfterTimeout != _) {
            keychain.timeout == lockKeychainAfterTimeout
        }

        if (lockKeychainWhenSleep != _) {
            keychain.lockWhenSystemSleeps == lockKeychainWhenSleep
        }

        where:
        lockKeychainWhenSleep | lockKeychainAfterTimeout
        true                  | _
        false                 | _
        _                     | -1
        _                     | 1000
        true                  | 1000
        false                 | -1
    }

    @Unroll("can set property #property with #invocation and type #type")
    def "can set property"() {
        expect:
        runPropertyQuery(get, set).matches(rawValue)

        where:
        property                   | invocation  | rawValue        | type
        "fileName"                 | providerSet | "testName1"     | "String"
        "fileName"                 | providerSet | "testName2"     | "Provider<String>"
        "fileName"                 | setter      | "testName3"     | "String"
        "fileName"                 | setter      | "testName4"     | "Provider<String>"

        "baseName"                 | providerSet | "testBaseName1" | "String"
        "baseName"                 | providerSet | "testBaseName2" | "Provider<String>"
        "baseName"                 | setter      | "testBaseName3" | "String"
        "baseName"                 | setter      | "testBaseName4" | "Provider<String>"

        "extension"                | providerSet | "ext1"          | "String"
        "extension"                | providerSet | "ext2"          | "Provider<String>"
        "extension"                | setter      | "ext3"          | "String"
        "extension"                | setter      | "ext4"          | "Provider<String>"

        "password"                 | providerSet | "password1"     | "String"
        "password"                 | providerSet | "password2"     | "Provider<String>"
        "password"                 | setter      | "password3"     | "String"
        "password"                 | setter      | "password4"     | "Provider<String>"

        "destinationDir"           | providerSet | "/some/path/3"  | "File"
        "destinationDir"           | providerSet | "/some/path/4"  | "Provider<Directory>"
        "destinationDir"           | setter      | "/some/path/5"  | "File"
        "destinationDir"           | setter      | "/some/path/6"  | "Provider<Directory>"

        "lockKeychainWhenSleep"    | providerSet | true            | "Boolean"
        "lockKeychainWhenSleep"    | providerSet | true            | "Provider<Boolean>"
        "lockKeychainWhenSleep"    | setter      | true            | "Boolean"
        "lockKeychainWhenSleep"    | setter      | true            | "Provider<Boolean>"

        "lockKeychainAfterTimeout" | providerSet | 3               | "Integer"
        "lockKeychainAfterTimeout" | providerSet | 4               | "Provider<Integer>"
        "lockKeychainAfterTimeout" | setter      | 5               | "Provider<Integer>"
        "lockKeychainAfterTimeout" | setter      | 6               | "Integer"

        set = new PropertySetterWriter(subjectUnderTestName, property)
                .set(rawValue, type)
                .toScript(invocation)
                .serialize(wrapValueFallback)

        get = new PropertyGetterTaskWriter(set)

        value = wrapValueBasedOnType(rawValue, type)
        expectedValue = rawValue
    }
}
