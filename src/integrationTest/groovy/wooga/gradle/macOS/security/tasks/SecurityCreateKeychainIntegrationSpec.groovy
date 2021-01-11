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

import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.build.IntegrationSpec
import wooga.gradle.build.unity.ios.IOSBuildPlugin
import wooga.gradle.fastlane.tasks.PilotUpload

@Requires({ os.macOs })
class SecurityCreateKeychainIntegrationSpec extends IntegrationSpec {

    String testTaskName = "createKeychain"

    Class taskType = SecurityCreateKeychain

    String keychainPassword = "123456"

    @Shared
    File buildKeychain

    def setup() {
        buildFile << """
        task ${testTaskName}(type: ${taskType.name}) {
            baseName = "build"
            extension = "keychain"
            destinationDir = file("build/sign/keychains")
            password = "${keychainPassword}"
        }
        """.stripIndent()

        buildKeychain = new File(projectDir, 'build/sign/keychains/build.keychain')
    }

    def "creates a keychain"() {
        given: "a future keychain"
        assert !buildKeychain.exists()

        when:
        runTasksSuccessfully(testTaskName)

        then:
        buildKeychain.exists()
    }

    def "buildKeychain caches task outputs"() {
        given: "a gradle run with buildKeychain"
        runTasksSuccessfully(testTaskName)

        when:
        def result = runTasksSuccessfully(testTaskName)

        then:
        result.wasUpToDate(testTaskName)
    }

    @Unroll
    def "createKeychain is not [UP-TO-DATE] when #reason"() {
        given: "a gradle run with buildKeychain"
        runTasksSuccessfully(testTaskName)

        when:
        buildKeychain.delete()
        def result = runTasksSuccessfully(testTaskName)

        then:
        !result.wasUpToDate(testTaskName)
    }

    def "does not print password to stdout"() {
        when:
        def result = runTasksSuccessfully(testTaskName)

        then:
        !outputContains(result, "-p ${keychainPassword}")
        outputContains(result, "-p ****")
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
        property                   | method                         | rawValue        | type
        "fileName"                 | "fileName"                     | "testName1"     | "String"
        "fileName"                 | "fileName"                     | "testName2"     | "Provider<String>"
        "fileName"                 | "fileName.set"                 | "testName1"     | "String"
        "fileName"                 | "fileName.set"                 | "testName2"     | "Provider<String>"
        "fileName"                 | "setFileName"                  | "testName3"     | "String"
        "fileName"                 | "setFileName"                  | "testName4"     | "Provider<String>"

        "baseName"                 | "baseName"                     | "testBaseName1" | "String"
        "baseName"                 | "baseName"                     | "testBaseName2" | "Provider<String>"
        "baseName"                 | "baseName.set"                 | "testBaseName1" | "String"
        "baseName"                 | "baseName.set"                 | "testBaseName2" | "Provider<String>"
        "baseName"                 | "setBaseName"                  | "testBaseName3" | "String"
        "baseName"                 | "setBaseName"                  | "testBaseName4" | "Provider<String>"

        "extension"                | "extension"                    | "ext2"          | "Provider<String>"
        "extension"                | "extension.set"                | "ext1"          | "String"
        "extension"                | "extension.set"                | "ext2"          | "Provider<String>"
        "extension"                | "setExtension"                 | "ext3"          | "String"
        "extension"                | "setExtension"                 | "ext4"          | "Provider<String>"

        "password"                 | "password"                     | "password2"     | "Provider<String>"
        "password"                 | "password.set"                 | "password1"     | "String"
        "password"                 | "password.set"                 | "password2"     | "Provider<String>"
        "password"                 | "setPassword"                  | "password3"     | "String"
        "password"                 | "setPassword"                  | "password4"     | "Provider<String>"

        "destinationDir"           | "destinationDir"               | "/some/path/1"  | "File"
        "destinationDir"           | "destinationDir"               | "/some/path/2"  | "Provider<Directory>"
        "destinationDir"           | "destinationDir.set"           | "/some/path/3"  | "File"
        "destinationDir"           | "destinationDir.set"           | "/some/path/4"  | "Provider<Directory>"
        "destinationDir"           | "setDestinationDir"            | "/some/path/5"  | "File"
        "destinationDir"           | "setDestinationDir"            | "/some/path/6"  | "Provider<Directory>"

        "lockKeychainWhenSleep"    | "lockKeychainWhenSleep"        | true            | "Boolean"
        "lockKeychainWhenSleep"    | "lockKeychainWhenSleep"        | true            | "Provider<Boolean>"
        "lockKeychainWhenSleep"    | "lockKeychainWhenSleep.set"    | true            | "Boolean"
        "lockKeychainWhenSleep"    | "lockKeychainWhenSleep.set"    | true            | "Provider<Boolean>"
        "lockKeychainWhenSleep"    | "setLockKeychainWhenSleep"     | true            | "Boolean"
        "lockKeychainWhenSleep"    | "setLockKeychainWhenSleep"     | true            | "Provider<Boolean>"

        "lockKeychainAfterTimeout" | "lockKeychainAfterTimeout"     | 1               | "Integer"
        "lockKeychainAfterTimeout" | "lockKeychainAfterTimeout"     | 2               | "Provider<Integer>"
        "lockKeychainAfterTimeout" | "lockKeychainAfterTimeout.set" | 3               | "Integer"
        "lockKeychainAfterTimeout" | "lockKeychainAfterTimeout.set" | 4               | "Provider<Integer>"
        "lockKeychainAfterTimeout" | "setLockKeychainAfterTimeout"  | 5               | "Provider<Integer>"
        "lockKeychainAfterTimeout" | "setLockKeychainAfterTimeout"  | 6               | "Integer"
        value = wrapValueBasedOnType(rawValue, type)
        expectedValue = rawValue
    }
}
