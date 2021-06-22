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

package wooga.gradle.xcodebuild.tasks

import spock.lang.Unroll
import wooga.gradle.xcodebuild.ConsoleSettings
import wooga.gradle.xcodebuild.XcodeBuildIntegrationSpec
import wooga.gradle.xcodebuild.config.BuildSettings

abstract class AbstractXcodeTaskIntegrationSpec extends XcodeBuildIntegrationSpec {

    abstract String getTestTaskName()

    abstract Class getTaskType()

    abstract String getWorkingXcodebuildTaskConfig()

    @Unroll("can set property #property with #method and type #type")
    def "can set property #property with #method and type #type base"() {
        given: "a custom xcodebuild task"
        buildFile << """
            task("${testTaskName}", type: ${taskType.name})
        """.stripIndent()

        and: "a task to read back the value"
        buildFile << """
            task("readValue") {
                doLast {
                    println("property: " + ${testTaskName}.${property}.get())
                }
            }
        """.stripIndent()

        and: "a set property"
        buildFile << """
            ${testTaskName}.${invocation}
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        outputContains(result, "property: " + testValue.toString())

        where:
        property          | method                | rawValue                                                    | expectedValue                                                          | type
        "logFile"         | "logFile"             | "/some/path/test1.log"                                      | _                                                                      | "File"
        "logFile"         | "logFile"             | "/some/path/test2.log"                                      | _                                                                      | "Provider<RegularFile>"
        "logFile"         | "logFile.set"         | "/some/path/test3.log"                                      | _                                                                      | "File"
        "logFile"         | "logFile.set"         | "/some/path/test4.log"                                      | _                                                                      | "Provider<RegularFile>"
        "logFile"         | "setLogFile"          | "/some/path/test5.log"                                      | _                                                                      | "File"
        "logFile"         | "setLogFile"          | "/some/path/test6.log"                                      | _                                                                      | "Provider<RegularFile>"
        "consoleSettings" | "consoleSettings.set" | "plain"                                                     | "ConsoleSettings{prettyPrint=true, useUnicode=false, colorize=never}"  | "ConsoleSettings"
        "consoleSettings" | "consoleSettings.set" | "rich"                                                      | "ConsoleSettings{prettyPrint=true, useUnicode=true, colorize=always}"  | "Provider<ConsoleSettings>"
        "consoleSettings" | "consoleSettings"     | "verbose"                                                   | "ConsoleSettings{prettyPrint=false, useUnicode=false, colorize=never}" | "ConsoleSettings"
        "consoleSettings" | "consoleSettings"     | "auto"                                                      | "ConsoleSettings{prettyPrint=true, useUnicode=true, colorize=auto}"    | "Provider<ConsoleSettings>"
        "buildSettings"   | "buildSettings"       | '[SOME_SETTING=some/value]'                                 | _                                                                      | "BuildSettings"
        "buildSettings"   | "buildSettings"       | '[MORE_SETTINGS=some/other/value, SOME_SETTING=some/value]' | _                                                                      | "Provider<BuildSettings>"
        "buildSettings"   | "buildSettings.set"   | '[SOME_SETTING=some/value]'                                 | _                                                                      | "BuildSettings"
        "buildSettings"   | "buildSettings.set"   | '[MORE_SETTINGS=some/other/value, SOME_SETTING=some/value]' | _                                                                      | "Provider<BuildSettings>"
        "buildSettings"   | "setBuildSettings"    | '[SOME_SETTING=some/value]'                                 | _                                                                      | "BuildSettings"
        "buildSettings"   | "setBuildSettings"    | '[MORE_SETTINGS=some/other/value, SOME_SETTING=some/value]' | _                                                                      | "Provider<BuildSettings>"

        value = wrapValueBasedOnType(rawValue, type, { type ->
            switch (type) {
                case ConsoleSettings.class.simpleName:
                    return "${ConsoleSettings.class.name}.fromGradleOutput(org.gradle.api.logging.configuration.ConsoleOutput.${rawValue.toString().capitalize()})"

                case BuildSettings.class.simpleName:
                    return "new ${BuildSettings.class.name}()" + rawValue.replaceAll(/(\[|\])/, '').split(',').collect({
                        List<String> parts = it.split("=")
                        ".put('${parts[0].trim()}', '${parts[1].trim()}')"
                    }).join("")
                default:
                    return rawValue
            }
        })
        invocation = (method == _) ? "${property} = ${value}" : "${method}(${value})"
        testValue = (expectedValue == _) ? rawValue : expectedValue
    }

    @Unroll("can configure console settings with #useConfigureBlockMessage #invocation and type #type")
    def "can configure console settings"() {
        given: "a custom xcodebuild task"
        buildFile << """
            task("${testTaskName}", type: ${taskType.name})
        """.stripIndent()

        and: "a task to read back the value"
        buildFile << """
            task("readValue") {
                doLast {
                    println("property: " + ${testTaskName}.consoleSettings.get().${property})
                }
            }
        """.stripIndent()

        and: "a set property"
        if (useConfigureBlock) {
            buildFile << """
            ${testTaskName}.consoleSettings {
                ${invocation}
            }
            """.stripIndent()
        } else {
            buildFile << "${testTaskName}.consoleSettings.get().${invocation}"
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
        useConfigureBlockMessage = useConfigureBlock ? "configuration closure and" : ""
        invocation = (method == _) ? "${property} = ${value}" : "${method}(${value})"
        expectedValue = rawValue
    }

    def "task #testTaskName writes log output"() {
        given:
        buildFile << workingXcodebuildTaskConfig

        and: "a future log file"
        def logFile = new File(projectDir, "build/logs/${testTaskName}.log")
        assert !logFile.exists()

        when:
        runTasks(testTaskName)

        then:
        logFile.exists()
        !logFile.text.empty
    }

    abstract String getExpectedPrettyLogOutput()

    abstract String getExpectedPrettyUnicodeLogOutput()

    abstract String getExpectedPrettyColoredUnicodeLogOutput()

    @Unroll("prints #logType xcodebuild log to console when #reason")
    def "prints xcodebuild log to console"() {
        given: "export task with pretty print enabled"
        buildFile << workingXcodebuildTaskConfig
        buildFile << """
        ${testTaskName} {
            consoleSettings {
                prettyPrint = ${usePrettyPrint}
                useUnicode = ${useUniCode}
                colorize = "${colorize}"
            }
        }
        """.stripIndent()

        and: "a future log file"
        def logFile = new File(projectDir, "build/logs/${testTaskName}.log")
        assert !logFile.exists()

        when:
        def result = runTasks(testTaskName)

        then:
        def expectedPrintOutput = ""
        if (usePrettyPrint && useUniCode && colorize == ConsoleSettings.ColorOption.always) {
            expectedPrintOutput = expectedPrettyColoredUnicodeLogOutput
        } else if (usePrettyPrint && useUniCode && colorize == ConsoleSettings.ColorOption.never) {
            expectedPrintOutput = expectedPrettyUnicodeLogOutput
        } else if (usePrettyPrint && !useUniCode) {
            expectedPrintOutput = expectedPrettyLogOutput
        }

        outputContains(result, expectedPrintOutput)
        outputContains(result, logFile.text) == !usePrettyPrint

        where:
        usePrettyPrint | useUniCode | colorize                           | logType                 | reason
        true           | true       | ConsoleSettings.ColorOption.always | "short colored unicode" | "pretty print and unicode and colorize is enabled"
        true           | true       | ConsoleSettings.ColorOption.never  | "short unicode"         | "pretty print and unicode is enabled"
        true           | false      | ConsoleSettings.ColorOption.never  | "short ascii"           | "pretty print is enabled"
        false          | true       | ConsoleSettings.ColorOption.never  | "full ascii"            | "pretty print is disabled"
        false          | false      | ConsoleSettings.ColorOption.never  | "full ascii"            | "pretty print and unicode is disabled"
    }

    def "can provide additional build arguments"() {
        given:
        buildFile << workingXcodebuildTaskConfig

        and: "some custom arguments"
        buildFile << """
        ${testTaskName}.buildArgument("-quiet")
        ${testTaskName}.buildArguments("-enableAddressSanitizer", "YES")
        ${testTaskName}.buildArguments("-enableThreadSanitizer", "NO")
        """.stripIndent()

        when:
        def result = runTasks(testTaskName)

        then:
        outputContains(result, "-quiet")
        outputContains(result, "-enableAddressSanitizer YES")
        outputContains(result, "-enableThreadSanitizer NO")
    }
}
