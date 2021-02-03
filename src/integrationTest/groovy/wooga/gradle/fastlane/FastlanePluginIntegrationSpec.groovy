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

package wooga.gradle.fastlane

import spock.lang.Requires
import spock.lang.Unroll
import wooga.gradle.fastlane.tasks.PilotUpload
import wooga.gradle.fastlane.tasks.SighRenew

@Requires({ os.macOs })
class FastlanePluginIntegrationSpec extends FastlaneIntegrationSpec {

    @Unroll()
    def "extension property :#property returns '#testValue' if #reason"() {
        given:
        buildFile << """
            task(custom) {
                doLast {
                    def value = ${extensionName}.${property}.getOrNull()
                    println("${extensionName}.${property}: " + value)
                }
            }
        """

        and: "a gradle.properties"
        def propertiesFile = createFile("gradle.properties")

        switch (location) {
            case PropertyLocation.script:
                buildFile << "${extensionName}.${invocation}"
                break
            case PropertyLocation.property:
                propertiesFile << "${extensionName}.${property} = ${escapedValue}"
                break
            case PropertyLocation.env:
                environmentVariables.set(envNameFromProperty(extensionName, property), "${value}")
                break
            default:
                break
        }

        and: "the test value with replace placeholders"
        if (testValue instanceof String) {
            testValue = testValue.replaceAll("#projectDir#", escapedPath(projectDir.path))
        }

        when: ""
        def result = runTasksSuccessfully("custom")

        then:
        result.standardOutput.contains("${extensionName}.${property}: ${testValue}")

        where:
        property     | method           | rawValue             | expectedValue | type                     | location                  | additionalInfo
        "username"   | _                | "someUser1"          | _             | _                        | PropertyLocation.env      | ""
        "username"   | _                | "someUser2"          | _             | _                        | PropertyLocation.property | ""
        "username"   | _                | "someUser3"          | _             | "String"                 | PropertyLocation.script   | ""
        "username"   | _                | "someUser4"          | _             | "Provider<String>"       | PropertyLocation.script   | ""
        "username"   | "username.set"   | "someUser5"          | _             | "String"                 | PropertyLocation.script   | ""
        "username"   | "username.set"   | "someUser6"          | _             | "Provider<String>"       | PropertyLocation.script   | ""
        "username"   | "username"       | "someUser7"          | _             | "String"                 | PropertyLocation.script   | ""
        "username"   | "username"       | "someUser8"          | _             | "Provider<String>"       | PropertyLocation.script   | ""
        "username"   | _                | _                    | null          | _                        | PropertyLocation.none     | ""


        "password"   | _                | "somePassword1"      | _             | _                        | PropertyLocation.env      | ""
        "password"   | _                | "somePassword2"      | _             | _                        | PropertyLocation.property | ""
        "password"   | _                | "somePassword3"      | _             | "String"                 | PropertyLocation.script   | ""
        "password"   | _                | "somePassword4"      | _             | "Provider<String>"       | PropertyLocation.script   | ""
        "password"   | "password.set"   | "somePassword5"      | _             | "String"                 | PropertyLocation.script   | ""
        "password"   | "password.set"   | "somePassword6"      | _             | "Provider<String>"       | PropertyLocation.script   | ""
        "password"   | "password"       | "somePassword7"      | _             | "String"                 | PropertyLocation.script   | ""
        "password"   | "password"       | "somePassword8"      | _             | "Provider<String>"       | PropertyLocation.script   | ""
        "password"   | _                | _                    | null          | _                        | PropertyLocation.none     | ""

        "apiKeyPath" | _                | "/path/to/key1.json" | _             | _                        | PropertyLocation.env      | ""
        "apiKeyPath" | _                | "/path/to/key2.json" | _             | _                        | PropertyLocation.property | ""
        "apiKeyPath" | _                | "/path/to/key3.json" | _             | "File"                   | PropertyLocation.script   | ""
        "apiKeyPath" | _                | "/path/to/key4.json" | _             | "Provider<RegularFile>"  | PropertyLocation.script   | ""
        "apiKeyPath" | "apiKeyPath.set" | "/path/to/key5.json" | _             | "File"                   | PropertyLocation.script   | ""
        "apiKeyPath" | "apiKeyPath.set" | "/path/to/key6.json" | _             | "Provider<RegularFile>"  | PropertyLocation.script   | ""
        "apiKeyPath" | "apiKeyPath"     | "/path/to/key7.json" | _             | "File"                   | PropertyLocation.script   | ""
        "apiKeyPath" | "apiKeyPath"     | "/path/to/key8.json" | _             | "Provider<RegularFile>" | PropertyLocation.script   | ""
        "apiKeyPath" | _                | _                    | null          | _                        | PropertyLocation.none     | ""
        extensionName = "fastlane"
        value = (type != _) ? wrapValueBasedOnType(rawValue, type) : rawValue
        providedValue = (location == PropertyLocation.script) ? type : value
        testValue = (expectedValue == _) ? rawValue : expectedValue
        reason = location.reason() + ((location == PropertyLocation.none) ? "" : "  with '$providedValue' ") + additionalInfo
        escapedValue = (value instanceof String) ? escapedPath(value) : value
        invocation = (method != _) ? "${method}(${escapedValue})" : "${property} = ${escapedValue}"
    }

    @Unroll("property #property of type #tasktype.simpleName is bound to property #extensionProperty of extension #extensionName")
    def "task property is connected with extension"() {
        given:
        buildFile << """
            task ${taskName}(type: ${tasktype.name})

            task(custom) {
                doLast {
                    def value = ${taskName}.${property}${providerInvocation}
                    println("${taskName}.${property}: " + value)
                }
            }
            
            ${extensionName}.${invocation}
        """.stripIndent()

        and: "the test value with replace placeholders"
        if (testValue instanceof String) {
            testValue = testValue.replaceAll("#projectDir#", escapedPath(projectDir.path))
            testValue = testValue.replaceAll("#taskName#", taskName)
        }

        when: ""
        def result = runTasksSuccessfully("custom")

        then:
        result.standardOutput.contains("${taskName}.${property}: ${testValue}")

        where:
        property     | extensionProperty | tasktype    | rawValue             | expectedValue | type     | useProviderApi
        "username"   | "username"        | SighRenew   | "userName1"          | _             | "String" | true
        "username"   | "username"        | PilotUpload | "userName2"          | _             | "String" | true

        "password"   | "password"        | SighRenew   | "password1"          | _             | "String" | true
        "password"   | "password"        | PilotUpload | "password2"          | _             | "String" | true

        "apiKeyPath" | "apiKeyPath"      | SighRenew   | "/path/to/key1.json" | _             | "File"   | true
        "apiKeyPath" | "apiKeyPath"      | PilotUpload | "/path/to/key2.json" | _             | "File"   | true

        extensionName = "fastlane"
        taskName = "fastlaneTask"
        value = (type != _) ? wrapValueBasedOnType(rawValue, type) : rawValue
        testValue = (expectedValue == _) ? rawValue : expectedValue
        escapedValue = (value instanceof String) ? escapedPath(value) : value
        invocation = "${extensionProperty}.set(${escapedValue})"
        providerInvocation = (useProviderApi) ? ".getOrNull()" : ""
    }
}
