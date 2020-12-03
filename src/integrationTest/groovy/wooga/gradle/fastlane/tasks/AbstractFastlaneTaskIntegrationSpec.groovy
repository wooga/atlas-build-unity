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

package wooga.gradle.fastlane.tasks

import spock.lang.Unroll
import wooga.gradle.fastlane.FastlaneSpec
import wooga.gradle.xcodebuild.ConsoleSettings
import wooga.gradle.xcodebuild.config.BuildSettings

abstract class AbstractFastlaneTaskIntegrationSpec extends FastlaneSpec {

    abstract String getTestTaskName()

    abstract Class getTaskType()

    abstract String getWorkingFastlaneTaskConfig()

    def setup() {
        buildFile << workingFastlaneTaskConfig
    }

    @Unroll("can set property #property with #method and type #type")
    def "can set property #property with #method and type #type base"() {
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
            ${testTaskName}.${invocation}
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        outputContains(result, "property: " + testValue.toString())

        where:
        property  | method        | rawValue               | expectedValue | type
        "logFile" | "logFile"     | "/some/path/test1.log" | _             | "File"
        "logFile" | "logFile"     | "/some/path/test2.log" | _             | "Provider<RegularFile>"
        "logFile" | "logFile.set" | "/some/path/test3.log" | _             | "File"
        "logFile" | "logFile.set" | "/some/path/test4.log" | _             | "Provider<RegularFile>"
        "logFile" | "setLogFile"  | "/some/path/test5.log" | _             | "File"
        "logFile" | "setLogFile"  | "/some/path/test6.log" | _             | "Provider<RegularFile>"


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

    @Unroll
    def "can configure arguments with #method #message"() {
        given: "a custom archive task"
        buildFile << """
            ${testTaskName} {
                arguments(["--test", "value"])
            }
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
            ${testTaskName}.${method}($value)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        outputContains(result, "property: " + expectedValue.toString())

        where:
        method                    | rawValue         | type                      | append | expectedValue
        "argument"                | "--foo"          | "String"                  | true   | ["--test", "value", "--foo"]
        "arguments"               | ["--foo", "bar"] | "List<String>"            | true   | ["--test", "value", "--foo", "bar"]
        "arguments"               | ["--foo", "bar"] | "String[]"                | true   | ["--test", "value", "--foo", "bar"]
        "setAdditionalArguments"  | ["--foo", "bar"] | "List<String>"            | false  | ["--foo", "bar"]
        "setAdditionalArguments"  | ["--foo", "bar"] | "Provider<List<String>>"  | false  | ["--foo", "bar"]
        "additionalArguments.set" | ["--foo", "bar"] | "List<String>"            | false  | ["--foo", "bar"]
        "additionalArguments.set" | ["--foo", "bar"] | "Provider<List<String>>>" | false  | ["--foo", "bar"]

        property = "additionalArguments"
        value = wrapValueBasedOnType(rawValue, type)
        message = (append) ? "which appends arguments" : "which replaces arguments"
    }

    def "task writes log output"() {
        given: "a future log file"
        def logFile = new File(projectDir, "build/logs/${testTaskName}.log")
        assert !logFile.exists()

        and: "the logfile configured"
        buildFile << """${testTaskName}.logFile = file("${logFile.path}")"""

        when:
        runTasks(testTaskName)

        then:
        logFile.exists()
        !logFile.text.empty
    }

    def "prints fastlane log to console and logfile"() {
        given: "a future log file"
        def logFile = new File(projectDir, "build/logs/${testTaskName}.log")
        assert !logFile.exists()

        and: "the logfile configured"
        buildFile << """${testTaskName}.logFile = file("${logFile.path}")"""

        when:
        def result = runTasks(testTaskName)

        then:
        outputContains(result, logFile.text)
    }
}
