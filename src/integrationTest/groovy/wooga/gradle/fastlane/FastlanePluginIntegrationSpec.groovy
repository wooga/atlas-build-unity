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

import com.wooga.gradle.PlatformUtils
import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.PropertyQueryTaskWriter
import spock.lang.Requires
import spock.lang.Unroll
import wooga.gradle.fastlane.tasks.PilotUpload
import wooga.gradle.fastlane.tasks.SighRenew

import static com.wooga.gradle.PlatformUtils.escapedPath
import static com.wooga.gradle.test.PropertyUtils.envNameFromProperty

class FastlanePluginIntegrationSpec extends FastlaneIntegrationSpec {

    @Unroll("extension property #property returns '#testValue' if #reason")
    def "extension property returns value"() {

        given: "a gradle.properties"
        def propertiesFile = createFile("gradle.properties")

        switch (location) {
            case PropertyLocation.script:
                buildFile << "${extensionName}.${invocation}"
                break
            case PropertyLocation.property:
                propertiesFile << "${extensionName}.${property} = ${escapedValue}"
                break
            case PropertyLocation.environment:
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
        def query = new PropertyQueryTaskWriter("${extensionName}.${property}")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        if (type != _) {
            testValue = substitutePath(testValue, testValue, type)
        }
        query.matches(result, testValue)

        where:
        property              | method                    | rawValue                      | expectedValue | type                    | location                     | additionalInfo
        "username"            | _                         | "someUser1"                   | _             | _                       | PropertyLocation.environment | ""
        "username"            | _                         | "someUser2"                   | _             | _                       | PropertyLocation.property    | ""
        "username"            | _                         | "someUser3"                   | _             | "String"                | PropertyLocation.script      | ""
        "username"            | _                         | "someUser4"                   | _             | "Provider<String>"      | PropertyLocation.script      | ""
        "username"            | "username.set"            | "someUser5"                   | _             | "String"                | PropertyLocation.script      | ""
        "username"            | "username.set"            | "someUser6"                   | _             | "Provider<String>"      | PropertyLocation.script      | ""
        "username"            | "username"                | "someUser7"                   | _             | "String"                | PropertyLocation.script      | ""
        "username"            | "username"                | "someUser8"                   | _             | "Provider<String>"      | PropertyLocation.script      | ""
        "username"            | _                         | _                             | null          | _                       | PropertyLocation.none        | ""


        "password"            | _                         | "somePassword1"               | _             | _                       | PropertyLocation.environment | ""
        "password"            | _                         | "somePassword2"               | _             | _                       | PropertyLocation.property    | ""
        "password"            | _                         | "somePassword3"               | _             | "String"                | PropertyLocation.script      | ""
        "password"            | _                         | "somePassword4"               | _             | "Provider<String>"      | PropertyLocation.script      | ""
        "password"            | "password.set"            | "somePassword5"               | _             | "String"                | PropertyLocation.script      | ""
        "password"            | "password.set"            | "somePassword6"               | _             | "Provider<String>"      | PropertyLocation.script      | ""
        "password"            | "password"                | "somePassword7"               | _             | "String"                | PropertyLocation.script      | ""
        "password"            | "password"                | "somePassword8"               | _             | "Provider<String>"      | PropertyLocation.script      | ""
        "password"            | _                         | _                             | null          | _                       | PropertyLocation.none        | ""

        "apiKeyPath"          | _                         | osPath("/path/to/key1.json")  | _             | _                       | PropertyLocation.environment | ""
        "apiKeyPath"          | _                         | osPath("/path/to/key2.json")  | _             | _                       | PropertyLocation.property    | ""
        "apiKeyPath"          | _                         | osPath("/path/to/key3.json")  | _             | "File"                  | PropertyLocation.script      | ""
        "apiKeyPath"          | _                         | osPath("/path/to/key4.json")  | _             | "Provider<RegularFile>" | PropertyLocation.script      | ""
        "apiKeyPath"          | "apiKeyPath.set"          | osPath("/path/to/key5.json")  | _             | "File"                  | PropertyLocation.script      | ""
        "apiKeyPath"          | "apiKeyPath.set"          | osPath("/path/to/key6.json")  | _             | "Provider<RegularFile>" | PropertyLocation.script      | ""
        "apiKeyPath"          | "apiKeyPath"              | osPath("/path/to/key7.json")  | _             | "File"                  | PropertyLocation.script      | ""
        "apiKeyPath"          | "apiKeyPath"              | osPath("/path/to/key8.json")  | _             | "Provider<RegularFile>" | PropertyLocation.script      | ""
        "apiKeyPath"          | _                         | _                             | null          | _                       | PropertyLocation.none        | ""

        "skip2faUpgrade"      | _                         | true                          | _             | _                       | PropertyLocation.environment | ""
        "skip2faUpgrade"      | _                         | true                          | _             | _                       | PropertyLocation.property    | ""
        "skip2faUpgrade"      | _                         | true                          | _             | "Boolean"               | PropertyLocation.script      | ""
        "skip2faUpgrade"      | _                         | true                          | _             | "Provider<Boolean>"     | PropertyLocation.script      | ""
        "skip2faUpgrade"      | "skip2faUpgrade.set"      | true                          | _             | "Boolean"               | PropertyLocation.script      | ""
        "skip2faUpgrade"      | "skip2faUpgrade.set"      | true                          | _             | "Provider<Boolean>"     | PropertyLocation.script      | ""
        "skip2faUpgrade"      | "skip2faUpgrade"          | true                          | _             | "Boolean"               | PropertyLocation.script      | ""
        "skip2faUpgrade"      | "skip2faUpgrade"          | true                          | _             | "Provider<Boolean>"     | PropertyLocation.script      | ""
        "skip2faUpgrade"      | _                         | _                             | false         | _                       | PropertyLocation.none        | ""

        extensionName = "fastlane"
        value = (type != _) ? wrapValueBasedOnType(rawValue, type) : rawValue
        providedValue = (location == PropertyLocation.script) ? type : value
        testValue = (expectedValue == _) ? rawValue : expectedValue
        reason = location.reason() + ((location == PropertyLocation.none) ? "" : "  with '$providedValue' ") + additionalInfo
        escapedValue = (value instanceof String) ? escapedPath(value) : value
        invocation = (method != _) ? "${method}(${escapedValue})" : "${property} = ${escapedValue}"
    }

    //TODO: These test should be able to run on all platforms but some path comparisons are quite tricky to test without some adjustments
    @Requires({ PlatformUtils.mac })
    @Unroll("extension property #property returns '#testValue' if #reason")
    def "extension property returns value2"() {

        given: "a gradle.properties"
        def propertiesFile = createFile("gradle.properties")

        switch (location) {
            case PropertyLocation.script:
                buildFile << "${extensionName}.${invocation}"
                break
            case PropertyLocation.property:
                propertiesFile << "${extensionName}.${property} = ${escapedValue}"
                break
            case PropertyLocation.environment:
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
        def query = new PropertyQueryTaskWriter("${extensionName}.${property}")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        if (type != _) {
            testValue = substitutePath(testValue, testValue, type)
        }
        query.matches(result, testValue)

        where:
        property              | method                    | rawValue                      | expectedValue | type                    | location                     | additionalInfo
        "executableName"      | _                         | "fastlane_2"                  | _             | _                       | PropertyLocation.environment | ""
        "executableName"      | _                         | "fastlane_3"                  | _             | _                       | PropertyLocation.property    | ""
        "executableName"      | "executableName.set"      | "fastlane_3"                  | _             | "String"                | PropertyLocation.script      | ""
        "executableName"      | "executableName.set"      | "fastlane_4"                  | _             | "Provider<String>"      | PropertyLocation.script      | ""
        "executableName"      | "setExecutableName"       | "fastlane_5"                  | _             | "String"                | PropertyLocation.script      | ""
        "executableName"      | "setExecutableName"       | "fastlane_6"                  | _             | "Provider<String>"      | PropertyLocation.script      | ""
        "executableName"      | _                         | "fastlane_6"                  | "fastlane"    | _                       | PropertyLocation.none        | ""

        "executableDirectory" | _                         | osPath("/path/to/fastlane_2") | _             | _                       | PropertyLocation.environment | ""
        "executableDirectory" | _                         | osPath("/path/to/fastlane_3") | _             | _                       | PropertyLocation.property    | ""
        "executableDirectory" | "executableDirectory.set" | osPath("/path/to/fastlane_3") | _             | "File"                  | PropertyLocation.script      | ""
        "executableDirectory" | "executableDirectory.set" | osPath("/path/to/fastlane_4") | _             | "Provider<Directory>"   | PropertyLocation.script      | ""
        "executableDirectory" | "setExecutableDirectory"  | osPath("/path/to/fastlane_5") | _             | "File"                  | PropertyLocation.script      | ""
        "executableDirectory" | "setExecutableDirectory"  | osPath("/path/to/fastlane_6") | _             | "Provider<Directory>"   | PropertyLocation.script      | ""
        "executableDirectory" | _                         | osPath("/path/to/fastlane_6") | null          | _                       | PropertyLocation.none        | ""

        "executable"          | "setExecutable"           | osPath("/path/to/fastlane_5") | _             | "String"                | PropertyLocation.script      | ""
        "executable"          | "setExecutable"           | osPath("/path/to/fastlane_6") | _             | "Provider<String>"      | PropertyLocation.script      | ""
        "executable"          | _                         | osPath("/path/to/fastlane_6") | "fastlane"    | _                       | PropertyLocation.none        | ""

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
            ${extensionName}.${invocation}
        """.stripIndent()

        and: "the test value with replace placeholders"
        if (testValue instanceof String) {
            testValue = testValue.replaceAll("#projectDir#", escapedPath(projectDir.path))
            testValue = testValue.replaceAll("#taskName#", taskName)
        }

        when: ""
        def query = new PropertyQueryTaskWriter("${taskName}.${property}")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, testValue)

        where:
        property              | extensionProperty     | tasktype    | rawValue                     | expectedValue | type      | useProviderApi
        "username"            | "username"            | SighRenew   | "userName1"                  | _             | "String"  | true
        "username"            | "username"            | PilotUpload | "userName2"                  | _             | "String"  | true

        "executableName"      | "executableName"      | SighRenew   | "fastlane_1"                 | _             | "String"  | true
        "executableName"      | "executableName"      | PilotUpload | "fastlane_1"                 | _             | "String"  | true

        "executableDirectory" | "executableDirectory" | SighRenew   | osPath("/path/to/")          | _             | "File"    | true
        "executableDirectory" | "executableDirectory" | PilotUpload | osPath("/path/to/")          | _             | "File"    | true

        "password"            | "password"            | SighRenew   | "password1"                  | _             | "String"  | true
        "password"            | "password"            | PilotUpload | "password2"                  | _             | "String"  | true

        "apiKeyPath"          | "apiKeyPath"          | SighRenew   | osPath("/path/to/key1.json") | _             | "File"    | true
        "apiKeyPath"          | "apiKeyPath"          | PilotUpload | osPath("/path/to/key2.json") | _             | "File"    | true

        "skip2faUpgrade"      | "skip2faUpgrade"      | SighRenew   | true                         | _             | "Boolean" | true
        "skip2faUpgrade"      | "skip2faUpgrade"      | PilotUpload | true                         | _             | "Boolean" | true

        extensionName = "fastlane"
        taskName = "fastlaneTask"
        value = (type != _) ? wrapValueBasedOnType(rawValue, type) : rawValue
        testValue = (expectedValue == _) ? rawValue : expectedValue
        escapedValue = (value instanceof String) ? escapedPath(value) : value
        invocation = "${extensionProperty}.set(${escapedValue})"
        providerInvocation = (useProviderApi) ? ".getOrNull()" : ""
    }
}
