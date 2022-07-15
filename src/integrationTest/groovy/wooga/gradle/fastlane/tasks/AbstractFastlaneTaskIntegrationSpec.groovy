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

import com.wooga.gradle.PlatformUtils
import com.wooga.gradle.test.PropertyQueryTaskWriter
import spock.lang.Requires
import spock.lang.Unroll
import wooga.gradle.fastlane.FastlaneIntegrationSpec

abstract class AbstractFastlaneTaskIntegrationSpec extends FastlaneIntegrationSpec {

    abstract String getTestTaskName()

    abstract Class getTaskType()

    abstract String getWorkingFastlaneTaskConfig()

    def setup() {
        buildFile << workingFastlaneTaskConfig
    }

    @Unroll("can set property #property with #method and type #type")
    def "can set property #property with #method and type #type base"() {

        given: "a set property"
        buildFile << """
            ${testTaskName}.${invocation}
        """.stripIndent()

        // TODO: Refactor
        and: "a substitution"
        def expected = substitutePath(testValue, rawValue, type)

        when:
        def query = new PropertyQueryTaskWriter("${testTaskName}.${property}")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        // TODO: If you use the RegularFile provider, it starts at the build directory (wat)
        def actual = query.getValue(result)
        expected == actual

        where:
        property     | method           | rawValue                      | expectedValue | type
        "logFile"    | "logFile"        | osPath("/some/path/test1.log") | _             | "File"
        "logFile"    | "logFile"        | osPath("/some/path/test2.log") | _             | "Provider<RegularFile>"
        "logFile"    | "logFile.set"    | osPath("/some/path/test3.log") | _             | "File"
        "logFile"    | "logFile.set"    | osPath("/some/path/test4.log") | _             | "Provider<RegularFile>"
        "logFile"    | "setLogFile"     | osPath("/some/path/test5.log") | _             | "File"
        "logFile"    | "setLogFile"     | osPath("/some/path/test6.log") | _             | "Provider<RegularFile>"
        "apiKeyPath" | "apiKeyPath"     | osPath("/some/path/key1.json") | _             | "File"
        "apiKeyPath" | "apiKeyPath"     | osPath("/some/path/key2.json") | _             | "Provider<RegularFile>"
        "apiKeyPath" | "apiKeyPath.set" | osPath("/some/path/key3.json") | _             | "File"
        "apiKeyPath" | "apiKeyPath.set" | osPath("/some/path/key4.json") | _             | "Provider<RegularFile>"
        "apiKeyPath" | "setApiKeyPath"  | osPath("/some/path/key5.json") | _             | "File"
        "apiKeyPath" | "setApiKeyPath"  | osPath("/some/path/key6.json") | _             | "Provider<RegularFile>"

        // TODO: Is this meant to be here?
        value = wrapValueBasedOnType(rawValue, type)
        path = PlatformUtils.escapedPath(osPath(value))
        invocation = (method == _) ? "${property} = ${path}" : "${method}(${path})"
        testValue = (expectedValue == _) ? rawValue : expectedValue
    }

    @Requires({ PlatformUtils.mac })
    def "task writes log output"() {
        given: "a future log file"
        def logFile = new File(projectDir, "build/logs/${testTaskName}.log")
        assert !logFile.exists()

        and: "the logfile configured"
        buildFile << """${testTaskName}.logFile = ${wrapValueBasedOnType(logFile.path, File)}"""

        when:
        def result = runTasksSuccessfully(testTaskName)

        then:
        logFile.exists()
        !logFile.text.empty
    }

    @Requires({ PlatformUtils.mac })
    def "prints fastlane log to console and logfile"() {
        given: "a future log file"
        def logFile = new File(projectDir, "build/logs/${testTaskName}.log")
        assert !logFile.exists()

        and: "the logfile configured"
        buildFile << """${testTaskName}.logFile = ${wrapValueBasedOnType(logFile.path, File)}"""

        when:
        def result = runTasks(testTaskName)

        then:
        outputContains(result, logFile.text)
    }
}
