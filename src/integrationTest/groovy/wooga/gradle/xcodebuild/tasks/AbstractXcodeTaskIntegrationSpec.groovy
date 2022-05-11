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


import com.wooga.gradle.test.TaskIntegrationSpec
import com.wooga.gradle.test.queries.TestValue
import com.wooga.gradle.test.run.result.GradleRunResult
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetInvocation
import com.wooga.gradle.test.writers.PropertySetterWriter
import spock.lang.Requires
import spock.lang.Unroll
import wooga.gradle.xcodebuild.ConsoleSettings
import wooga.gradle.xcodebuild.XcodeBuildIntegrationSpec

abstract class AbstractXcodeTaskIntegrationSpec<T extends AbstractXcodeTask> extends XcodeBuildIntegrationSpec
    implements TaskIntegrationSpec<T> {

    abstract String getWorkingXcodebuildTaskConfig()

    @Unroll("can set property #property with #method and type #type")
    def "can set property #property with #method and type #type base"() {

        given:
        addMockTask(true)

        when:
        def query = runPropertyQuery(getter, setter)

        then:
        query.matches(value)

        where:
        property          | method                            | value                                                                                                   | type
        "logFile"         | PropertySetInvocation.method      | osPath("/some/path/test1.log")                                                                          | "File"
        "logFile"         | PropertySetInvocation.method      | osPath("/some/path/test2.log")                                                                          | "Provider<RegularFile>"
        "logFile"         | PropertySetInvocation.providerSet | osPath("/some/path/test3.log")                                                                          | "File"
        "logFile"         | PropertySetInvocation.providerSet | osPath("/some/path/test4.log")                                                                          | "Provider<RegularFile>"
        "logFile"         | PropertySetInvocation.setter      | osPath("/some/path/test5.log")                                                                          | "File"
        "logFile"         | PropertySetInvocation.setter      | osPath("/some/path/test6.log")                                                                          | "Provider<RegularFile>"
        "consoleSettings" | PropertySetInvocation.providerSet | TestValue.set("plain").expect("ConsoleSettings{prettyPrint=true, useUnicode=false, colorize=never}")    | "ConsoleSettings"
        "consoleSettings" | PropertySetInvocation.providerSet | TestValue.set("rich").expect("ConsoleSettings{prettyPrint=true, useUnicode=true, colorize=always}")     | "Provider<ConsoleSettings>"
        "consoleSettings" | PropertySetInvocation.method      | TestValue.set("verbose").expect("ConsoleSettings{prettyPrint=false, useUnicode=false, colorize=never}") | "ConsoleSettings"
        "consoleSettings" | PropertySetInvocation.method      | TestValue.set("auto").expect("ConsoleSettings{prettyPrint=true, useUnicode=true, colorize=auto}")       | "Provider<ConsoleSettings>"
        "buildSettings"   | PropertySetInvocation.method      | '[SOME_SETTING=some/value]'                                                                             | "BuildSettings"
        "buildSettings"   | PropertySetInvocation.method      | '[MORE_SETTINGS=some/other/value, SOME_SETTING=some/value]'                                             | "Provider<BuildSettings>"
        "buildSettings"   | PropertySetInvocation.providerSet | '[SOME_SETTING=some/value]'                                                                             | "BuildSettings"
        "buildSettings"   | PropertySetInvocation.providerSet | '[MORE_SETTINGS=some/other/value, SOME_SETTING=some/value]'                                             | "Provider<BuildSettings>"
        "buildSettings"   | PropertySetInvocation.setter      | '[SOME_SETTING=some/value]'                                                                             | "BuildSettings"
        "buildSettings"   | PropertySetInvocation.setter      | '[MORE_SETTINGS=some/other/value, SOME_SETTING=some/value]'                                             | "Provider<BuildSettings>"

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(value, type)
            .use(method)
            .serialize(wrapValueFallback)

        getter = new PropertyGetterTaskWriter(setter)
    }

    @Unroll("can configure console settings with #useConfigureBlockMessage #invocation and type #type")
    def "can configure console settings"() {
        given: "a custom xcodebuild task"
        addMockTask(true)

        and: "a task to read back the value"
        buildFile << """
            task("readValue") {
                doLast {
                    println("property: " + ${subjectUnderTestName}.consoleSettings.get().${property})
                }
            }
        """.stripIndent()

        and: "a set property"
        if (useConfigureBlock) {
            buildFile << """
            ${subjectUnderTestName}.consoleSettings {
                ${invocation}
            }
            """.stripIndent()
        } else {
            buildFile << "${subjectUnderTestName}.consoleSettings.get().${invocation}"
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

    @Requires({ os.macOs })
    def "task #testTaskName writes log output"() {
        given:
        buildFile << workingXcodebuildTaskConfig

        and: "a future log file"
        def logFile = new File(projectDir, "build/logs/${subjectUnderTestName}.log")
        assert !logFile.exists()

        when:
        runTasks(subjectUnderTestName)

        then:
        logFile.exists()
        !logFile.text.empty
    }

    abstract String getExpectedPrettyLogOutput()

    abstract String getExpectedPrettyUnicodeLogOutput()

    abstract String getExpectedPrettyColoredUnicodeLogOutput()

    @Requires({ os.macOs })
    @Unroll("prints #logType xcodebuild log to console when #reason")
    def "prints xcodebuild log to console"() {
        given: "export task with pretty print enabled"
        buildFile << workingXcodebuildTaskConfig
        buildFile << """
        ${subjectUnderTestName} {
            consoleSettings {
                prettyPrint = ${usePrettyPrint}
                useUnicode = ${useUniCode}
                colorize = "${colorize}"
            }
        }
        """.stripIndent()

        and: "a future log file"
        def logFile = new File(projectDir, "build/logs/${subjectUnderTestName}.log")
        assert !logFile.exists()

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        def expectedPrintOutput = ""
        if (usePrettyPrint && useUniCode && colorize == ConsoleSettings.ColorOption.always) {
            expectedPrintOutput = expectedPrettyColoredUnicodeLogOutput
        } else if (usePrettyPrint && useUniCode && colorize == ConsoleSettings.ColorOption.never) {
            expectedPrintOutput = expectedPrettyUnicodeLogOutput
        } else if (usePrettyPrint && !useUniCode) {
            expectedPrintOutput = expectedPrettyLogOutput
        }

        def log = new GradleRunResult(result).getAt(subjectUnderTestName).getTaskLog()
        log.contains(expectedPrintOutput)

        where:
        usePrettyPrint | useUniCode | colorize                           | logType                 | reason
        true           | true       | ConsoleSettings.ColorOption.always | "short colored unicode" | "pretty print and unicode and colorize is enabled"
        true           | true       | ConsoleSettings.ColorOption.never  | "short unicode"         | "pretty print and unicode is enabled"
        true           | false      | ConsoleSettings.ColorOption.never  | "short ascii"           | "pretty print is enabled"
        false          | true       | ConsoleSettings.ColorOption.never  | "full ascii"            | "pretty print is disabled"
        false          | false      | ConsoleSettings.ColorOption.never  | "full ascii"            | "pretty print and unicode is disabled"
    }

    @Requires({ os.macOs })
    def "can provide additional build arguments"() {
        given:
        buildFile << workingXcodebuildTaskConfig

        and: "some custom arguments"
        buildFile << """
        ${subjectUnderTestName}.argument("-quiet")
        ${subjectUnderTestName}.arguments("-enableAddressSanitizer", "YES")
        ${subjectUnderTestName}.arguments("-enableThreadSanitizer", "NO")
        """.stripIndent()

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        outputContains(result, "-quiet")
        outputContains(result, "-enableAddressSanitizer YES")
        outputContains(result, "-enableThreadSanitizer NO")
    }
}
