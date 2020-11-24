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

package wooga.gradle.xcodebuild

import net.wooga.test.xcode.XcodeTestProject
import org.junit.ClassRule
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.xcodebuild.tasks.ArchiveDebugSymbols
import wooga.gradle.xcodebuild.tasks.ExportArchive
import wooga.gradle.xcodebuild.tasks.XcodeArchive

@Requires({ os.macOs })
class XcodeBuildPluginIntegrationSpec extends XcodeBuildIntegrationSpec {

    @Shared
    @ClassRule
    XcodeTestProject xcodeProject = new XcodeTestProject()

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
        property          | method                | rawValue                   | expectedValue                                                          | type                        | location                  | additionalInfo
        "logsDir"         | _                     | "custom/logs"              | "#projectDir#/build/custom/logs"                                       | _                           | PropertyLocation.env      | " as relative path"
        "logsDir"         | _                     | "custom/logs"              | "#projectDir#/build/custom/logs"                                       | _                           | PropertyLocation.property | " as relative path"
        "logsDir"         | _                     | "build/custom/logs"        | "#projectDir#/build/custom/logs"                                       | "File"                      | PropertyLocation.script   | " as relative path"
        "logsDir"         | _                     | "build/custom/logs"        | "#projectDir#/build/custom/logs"                                       | "Provider<Directory>"       | PropertyLocation.script   | " as relative path"
        "logsDir"         | "logsDir.set"         | "build/custom/logs"        | "#projectDir#/build/custom/logs"                                       | "File"                      | PropertyLocation.script   | " as relative path"
        "logsDir"         | "logsDir.set"         | "build/custom/logs"        | "#projectDir#/build/custom/logs"                                       | "Provider<Directory>"       | PropertyLocation.script   | " as relative path"
        "logsDir"         | "logsDir"             | "build/custom/logs"        | "#projectDir#/build/custom/logs"                                       | "File"                      | PropertyLocation.script   | " as relative path"
        "logsDir"         | "logsDir"             | "build/custom/logs"        | "#projectDir#/build/custom/logs"                                       | "Provider<Directory>"       | PropertyLocation.script   | " as relative path"
        "logsDir"         | _                     | _                          | "#projectDir#/build/logs"                                              | _                           | PropertyLocation.none     | ""

        "logsDir"         | _                     | "/custom/logs"             | _                                                                      | _                           | PropertyLocation.env      | " as absolute path"
        "logsDir"         | _                     | "/custom/logs"             | _                                                                      | _                           | PropertyLocation.property | " as absolute path"
        "logsDir"         | _                     | "/custom/logs"             | _                                                                      | "File"                      | PropertyLocation.script   | " as absolute path"
        "logsDir"         | _                     | "/custom/logs"             | _                                                                      | "Provider<Directory>"       | PropertyLocation.script   | " as absolute path"
        "logsDir"         | "logsDir.set"         | "/custom/logs"             | _                                                                      | "File"                      | PropertyLocation.script   | " as absolute path"
        "logsDir"         | "logsDir.set"         | "/custom/logs"             | _                                                                      | "Provider<Directory>"       | PropertyLocation.script   | " as absolute path"
        "logsDir"         | "logsDir"             | "/custom/logs"             | _                                                                      | "File"                      | PropertyLocation.script   | " as absolute path"
        "logsDir"         | "logsDir"             | "/custom/logs"             | _                                                                      | "Provider<Directory>"       | PropertyLocation.script   | " as absolute path"

        "derivedDataPath" | _                     | "custom/derivedData"       | "#projectDir#/build/custom/derivedData"                                | _                           | PropertyLocation.env      | " as relative path"
        "derivedDataPath" | _                     | "custom/derivedData"       | "#projectDir#/build/custom/derivedData"                                | _                           | PropertyLocation.property | " as relative path"
        "derivedDataPath" | _                     | "build/custom/derivedData" | "#projectDir#/build/custom/derivedData"                                | "File"                      | PropertyLocation.script   | " as relative path"
        "derivedDataPath" | _                     | "build/custom/derivedData" | "#projectDir#/build/custom/derivedData"                                | "Provider<Directory>"       | PropertyLocation.script   | " as relative path"
        "derivedDataPath" | "derivedDataPath.set" | "build/custom/derivedData" | "#projectDir#/build/custom/derivedData"                                | "File"                      | PropertyLocation.script   | " as relative path"
        "derivedDataPath" | "derivedDataPath.set" | "build/custom/derivedData" | "#projectDir#/build/custom/derivedData"                                | "Provider<Directory>"       | PropertyLocation.script   | " as relative path"
        "derivedDataPath" | "derivedDataPath"     | "build/custom/derivedData" | "#projectDir#/build/custom/derivedData"                                | "File"                      | PropertyLocation.script   | " as relative path"
        "derivedDataPath" | "derivedDataPath"     | "build/custom/derivedData" | "#projectDir#/build/custom/derivedData"                                | "Provider<Directory>"       | PropertyLocation.script   | " as relative path"
        "derivedDataPath" | _                     | _                          | "#projectDir#/build/derivedData"                                       | _                           | PropertyLocation.none     | ""

        "derivedDataPath" | _                     | "/custom/derivedData"      | _                                                                      | _                           | PropertyLocation.env      | " as absolute path"
        "derivedDataPath" | _                     | "/custom/derivedData"      | _                                                                      | _                           | PropertyLocation.property | " as absolute path"
        "derivedDataPath" | _                     | "/custom/derivedData"      | _                                                                      | "File"                      | PropertyLocation.script   | " as absolute path"
        "derivedDataPath" | _                     | "/custom/derivedData"      | _                                                                      | "Provider<Directory>"       | PropertyLocation.script   | " as absolute path"
        "derivedDataPath" | "derivedDataPath.set" | "/custom/derivedData"      | _                                                                      | "File"                      | PropertyLocation.script   | " as absolute path"
        "derivedDataPath" | "derivedDataPath.set" | "/custom/derivedData"      | _                                                                      | "Provider<Directory>"       | PropertyLocation.script   | " as absolute path"
        "derivedDataPath" | "derivedDataPath"     | "/custom/derivedData"      | _                                                                      | "File"                      | PropertyLocation.script   | " as absolute path"
        "derivedDataPath" | "derivedDataPath"     | "/custom/derivedData"      | _                                                                      | "Provider<Directory>"       | PropertyLocation.script   | " as absolute path"

        "xarchivesDir"    | _                     | "custom/archives"          | "#projectDir#/build/custom/archives"                                   | _                           | PropertyLocation.env      | " as relative path"
        "xarchivesDir"    | _                     | "custom/archives"          | "#projectDir#/build/custom/archives"                                   | _                           | PropertyLocation.property | " as relative path"
        "xarchivesDir"    | _                     | "build/custom/archives"    | "#projectDir#/build/custom/archives"                                   | "File"                      | PropertyLocation.script   | " as relative path"
        "xarchivesDir"    | _                     | "build/custom/archives"    | "#projectDir#/build/custom/archives"                                   | "Provider<Directory>"       | PropertyLocation.script   | " as relative path"
        "xarchivesDir"    | "xarchivesDir.set"    | "build/custom/archives"    | "#projectDir#/build/custom/archives"                                   | "File"                      | PropertyLocation.script   | " as relative path"
        "xarchivesDir"    | "xarchivesDir.set"    | "build/custom/archives"    | "#projectDir#/build/custom/archives"                                   | "Provider<Directory>"       | PropertyLocation.script   | " as relative path"
        "xarchivesDir"    | "xarchivesDir"        | "build/custom/archives"    | "#projectDir#/build/custom/archives"                                   | "File"                      | PropertyLocation.script   | " as relative path"
        "xarchivesDir"    | "xarchivesDir"        | "build/custom/archives"    | "#projectDir#/build/custom/archives"                                   | "Provider<Directory>"       | PropertyLocation.script   | " as relative path"
        "xarchivesDir"    | _                     | _                          | "#projectDir#/build/archives"                                          | _                           | PropertyLocation.none     | ""

        "xarchivesDir"    | _                     | "/custom/archives"         | _                                                                      | _                           | PropertyLocation.env      | " as absolute path"
        "xarchivesDir"    | _                     | "/custom/archives"         | _                                                                      | _                           | PropertyLocation.property | " as absolute path"
        "xarchivesDir"    | _                     | "/custom/archives"         | _                                                                      | "File"                      | PropertyLocation.script   | " as absolute path"
        "xarchivesDir"    | _                     | "/custom/archives"         | _                                                                      | "Provider<Directory>"       | PropertyLocation.script   | " as absolute path"
        "xarchivesDir"    | "xarchivesDir.set"    | "/custom/archives"         | _                                                                      | "File"                      | PropertyLocation.script   | " as absolute path"
        "xarchivesDir"    | "xarchivesDir.set"    | "/custom/archives"         | _                                                                      | "Provider<Directory>"       | PropertyLocation.script   | " as absolute path"
        "xarchivesDir"    | "xarchivesDir"        | "/custom/archives"         | _                                                                      | "File"                      | PropertyLocation.script   | " as absolute path"
        "xarchivesDir"    | "xarchivesDir"        | "/custom/archives"         | _                                                                      | "Provider<Directory>"       | PropertyLocation.script   | " as absolute path"

        "debugSymbolsDir" | _                     | "custom/symbols"           | "#projectDir#/build/custom/symbols"                                    | _                           | PropertyLocation.env      | " as relative path"
        "debugSymbolsDir" | _                     | "custom/symbols"           | "#projectDir#/build/custom/symbols"                                    | _                           | PropertyLocation.property | " as relative path"
        "debugSymbolsDir" | _                     | "build/custom/symbols"     | "#projectDir#/build/custom/symbols"                                    | "File"                      | PropertyLocation.script   | " as relative path"
        "debugSymbolsDir" | _                     | "build/custom/symbols"     | "#projectDir#/build/custom/symbols"                                    | "Provider<Directory>"       | PropertyLocation.script   | " as relative path"
        "debugSymbolsDir" | "debugSymbolsDir.set" | "build/custom/symbols"     | "#projectDir#/build/custom/symbols"                                    | "File"                      | PropertyLocation.script   | " as relative path"
        "debugSymbolsDir" | "debugSymbolsDir.set" | "build/custom/symbols"     | "#projectDir#/build/custom/symbols"                                    | "Provider<Directory>"       | PropertyLocation.script   | " as relative path"
        "debugSymbolsDir" | "debugSymbolsDir"     | "build/custom/symbols"     | "#projectDir#/build/custom/symbols"                                    | "File"                      | PropertyLocation.script   | " as relative path"
        "debugSymbolsDir" | "debugSymbolsDir"     | "build/custom/symbols"     | "#projectDir#/build/custom/symbols"                                    | "Provider<Directory>"       | PropertyLocation.script   | " as relative path"
        "debugSymbolsDir" | _                     | _                          | "#projectDir#/build/symbols"                                           | _                           | PropertyLocation.none     | ""

        "debugSymbolsDir" | _                     | "/custom/symbols"          | _                                                                      | _                           | PropertyLocation.env      | " as absolute path"
        "debugSymbolsDir" | _                     | "/custom/symbols"          | _                                                                      | _                           | PropertyLocation.property | " as absolute path"
        "debugSymbolsDir" | _                     | "/custom/symbols"          | _                                                                      | "File"                      | PropertyLocation.script   | " as absolute path"
        "debugSymbolsDir" | _                     | "/custom/symbols"          | _                                                                      | "Provider<Directory>"       | PropertyLocation.script   | " as absolute path"
        "debugSymbolsDir" | "debugSymbolsDir.set" | "/custom/symbols"          | _                                                                      | "File"                      | PropertyLocation.script   | " as absolute path"
        "debugSymbolsDir" | "debugSymbolsDir.set" | "/custom/symbols"          | _                                                                      | "Provider<Directory>"       | PropertyLocation.script   | " as absolute path"
        "debugSymbolsDir" | "debugSymbolsDir"     | "/custom/symbols"          | _                                                                      | "File"                      | PropertyLocation.script   | " as absolute path"
        "debugSymbolsDir" | "debugSymbolsDir"     | "/custom/symbols"          | _                                                                      | "Provider<Directory>"       | PropertyLocation.script   | " as absolute path"

        "consoleSettings" | "consoleSettings.set" | "plain"                    | "ConsoleSettings{prettyPrint=true, useUnicode=false, colorize=never}"  | "ConsoleSettings"           | PropertyLocation.script   | ""
        "consoleSettings" | "consoleSettings.set" | "rich"                     | "ConsoleSettings{prettyPrint=true, useUnicode=true, colorize=always}"  | "Provider<ConsoleSettings>" | PropertyLocation.script   | ""
        "consoleSettings" | "consoleSettings"     | "verbose"                  | "ConsoleSettings{prettyPrint=false, useUnicode=false, colorize=never}" | "ConsoleSettings"           | PropertyLocation.script   | ""
        "consoleSettings" | "consoleSettings"     | "auto"                     | "ConsoleSettings{prettyPrint=true, useUnicode=true, colorize=auto}"    | "Provider<ConsoleSettings>" | PropertyLocation.script   | ""

        extensionName = "xcodebuild"
        value = (type != _) ? wrapValueBasedOnType(rawValue, type, { type ->
            switch (type) {
                case ConsoleSettings.class.simpleName:
                    return "${ConsoleSettings.class.name}.fromGradleOutput(org.gradle.api.logging.configuration.ConsoleOutput.${rawValue.toString().capitalize()})"
                default:
                    return rawValue
            }
        }) : rawValue
        providedValue = (location == PropertyLocation.script) ? type : value
        testValue = (expectedValue == _) ? rawValue : expectedValue
        reason = location.reason() + ((location == PropertyLocation.none) ? "" : "  with '$providedValue' ") + additionalInfo
        escapedValue = (value instanceof String) ? escapedPath(value) : value
        invocation = (method != _) ? "${method}(${escapedValue})" : "${property} = ${escapedValue}"
    }

    @Unroll("can configure console settings with #useConfigureBlockMessage #invocation and type #type")
    def "can configure console settings"() {
        given: "a task to read back the value"
        buildFile << """
            task("readValue") {
                doLast {
                    println("property: " + ${extensionName}.consoleSettings.get().${property})
                }
            }
        """.stripIndent()

        and: "a set property"
        and: "a set property"
        if (useConfigureBlock) {
            buildFile << """
            ${extensionName}.consoleSettings {
                ${invocation}
            }
            """.stripIndent()
        } else {
            buildFile << "${extensionName}.consoleSettings.get().${invocation}"
        }

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        outputContains(result, "property: " + expectedValue.toString())

        where:
        property      | method           | rawValue                           | type          | useConfigureBlock
        "prettyPrint" | "setPrettyPrint" | true                               | "Boolean"     | false
        "prettyPrint" | "setPrettyPrint" | false                              | "Boolean"     | false
        "useUnicode"  | "setUseUnicode"  | true                               | "Boolean"     | false
        "useUnicode"  | "setUseUnicode"  | false                              | "Boolean"     | false
        "colorize"    | "setColorize"    | ConsoleSettings.ColorOption.always | "ColorOption" | false
        "colorize"    | "setColorize"    | ConsoleSettings.ColorOption.never  | "ColorOption" | false
        "colorize"    | "setColorize"    | ConsoleSettings.ColorOption.auto   | "ColorOption" | false
        "colorize"    | _                | "always"                           | "String"      | false
        "colorize"    | _                | "never"                            | "String"      | false
        "colorize"    | _                | "auto"                             | "String"      | false

        "prettyPrint" | "setPrettyPrint" | true                               | "Boolean"     | true
        "prettyPrint" | "setPrettyPrint" | false                              | "Boolean"     | true
        "useUnicode"  | "setUseUnicode"  | true                               | "Boolean"     | true
        "useUnicode"  | "setUseUnicode"  | false                              | "Boolean"     | true
        "colorize"    | "setColorize"    | ConsoleSettings.ColorOption.always | "ColorOption" | true
        "colorize"    | "setColorize"    | ConsoleSettings.ColorOption.never  | "ColorOption" | true
        "colorize"    | "setColorize"    | ConsoleSettings.ColorOption.auto   | "ColorOption" | true
        "colorize"    | _                | "always"                           | "String"      | true
        "colorize"    | _                | "never"                            | "String"      | true
        "colorize"    | _                | "auto"                             | "String"      | true

        value = wrapValueBasedOnType(rawValue, type) { type ->
            switch (type) {
                case ConsoleSettings.ColorOption.simpleName:
                    return ConsoleSettings.ColorOption.name + ".${rawValue.toString()}"
                default:
                    return rawValue
            }
        }
        extensionName = "xcodebuild"
        useConfigureBlockMessage = useConfigureBlock ? "configuration closure and" : ""
        invocation = (method == _) ? "${property} = ${value}" : "${method}(${value})"
        expectedValue = rawValue
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
        property          | extensionProperty | tasktype            | rawValue                   | expectedValue                                      | type   | useProviderApi
        "logFile"         | "logsDir"         | XcodeArchive        | "build/custom/logs"        | "#projectDir#/build/custom/logs/#taskName#.log"    | "File" | true
        "logFile"         | "logsDir"         | ExportArchive       | "build/custom/logs"        | "#projectDir#/build/custom/logs/#taskName#.log"    | "File" | true
        "derivedDataPath" | "derivedDataPath" | XcodeArchive        | "build/custom/derivedData" | "#projectDir#/build/custom/derivedData/#taskName#" | "File" | true
        "destinationDir"  | "xarchivesDir"    | XcodeArchive        | "build/custom/archives"    | "#projectDir#/build/custom/archives"               | "File" | true
        "destinationDir"  | "xarchivesDir"    | ExportArchive       | "build/custom/archives"    | "#projectDir#/build/custom/archives"               | "File" | true
        "destinationDir"  | "debugSymbolsDir" | ArchiveDebugSymbols | "build/custom/symbols"     | "#projectDir#/build/custom/symbols"                | "File" | false


        extensionName = "xcodebuild"
        taskName = "xcodebuildTask"
        value = (type != _) ? wrapValueBasedOnType(rawValue, type) : rawValue
        testValue = (expectedValue == _) ? rawValue : expectedValue
        escapedValue = (value instanceof String) ? escapedPath(value) : value
        invocation = "${extensionProperty}.set(${escapedValue})"
        providerInvocation = (useProviderApi) ? ".getOrNull()" : ""
    }

    @Unroll("gradle console #console sets default value for consoleSettings.#consoleSettingProperty: #expectedValue")
    def "consoleSettings fallback to gradle console settings"() {
        given: "a properties file with console selected"
        def propertiesFile = createFile("gradle.properties")
        propertiesFile << "org.gradle.console=${console}"

        and: "a task to read out the value"
        buildFile << """
            task(readValue) {
                doLast {
                    def consoleSettings = ${extensionName}.consoleSettings.get()
                    println("${extensionName}.consoleSettings.${consoleSettingProperty}: " + consoleSettings.${consoleSettingProperty})
                }
            }
        """

        when:
        def result = runTasksSuccessfully('readValue')

        then:
        outputContains(result, "${extensionName}.consoleSettings.${consoleSettingProperty}: ${expectedValue}")

        where:
        console   | consoleSettingProperty | expectedValue
        "rich"    | "prettyPrint"          | true
        "rich"    | "useUnicode"           | true
        "rich"    | "colorize"             | ConsoleSettings.ColorOption.always

        "plain"   | "prettyPrint"          | true
        "plain"   | "useUnicode"           | false
        "plain"   | "colorize"             | ConsoleSettings.ColorOption.never

        "verbose" | "prettyPrint"          | false
        "verbose" | "useUnicode"           | false
        "verbose" | "colorize"             | ConsoleSettings.ColorOption.never

        "auto"    | "prettyPrint"          | true
        "auto"    | "useUnicode"           | true
        "auto"    | "colorize"             | ConsoleSettings.ColorOption.auto

        extensionName = "xcodebuild"
    }
}
