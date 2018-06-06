/*
 * Copyright 2017 the original author or authors.
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

package wooga.gradle.unity.build.tasks

import spock.lang.Unroll
import wooga.gradle.unity.build.UnityIntegrationSpec

class UnityBuildPlayerTaskIntegrationSpec extends UnityIntegrationSpec {

    def setup() {
        buildFile << """
            task("exportCustom", type: wooga.gradle.unity.build.tasks.UnityBuildPlayerTask)
        """.stripIndent()
    }

    def "uses default settings when not configured"() {
        given: "a custom export task without configuration"

        when:
        def result = runTasksSuccessfully("exportCustom")

        then:
        result.standardOutput.contains("-executeMethod Wooga.UnityBuild.NewAutomatedBuild.Export")
        result.standardOutput.contains("platform=android")
        result.standardOutput.contains("environment=ci")
    }

    @Unroll
    def "can override #property with #methodName with #value"() {
        given: "execute on a default project"
        buildFile << """
            exportCustom.${methodName}('${value}')
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("exportCustom")

        then:

        result.standardOutput.contains("-executeMethod ${expectedExportMethod}")
        result.standardOutput.contains("platform=${expectedPlatform}")
        result.standardOutput.contains("environment=${expectedEnvironment}")

        where:
        property           | value          | useSetter | expectedExportMethod                        | expectedEnvironment | expectedPlatform
        "exportMethodName" | "method1"      | true      | "method1"                                   | 'ci'                | 'android'
        "exportMethodName" | "method2"      | false     | "method2"                                   | 'ci'                | 'android'
        "buildEnvironment" | "environment1" | true      | 'Wooga.UnityBuild.NewAutomatedBuild.Export' | "environment1"      | 'android'
        "buildEnvironment" | "environment2" | false     | 'Wooga.UnityBuild.NewAutomatedBuild.Export' | "environment2"      | 'android'
        "buildPlatform"    | "platform1"    | true      | 'Wooga.UnityBuild.NewAutomatedBuild.Export' | 'ci'                | "platform1"
        "buildPlatform"    | "platform2"    | false     | 'Wooga.UnityBuild.NewAutomatedBuild.Export' | 'ci'                | "platform2"
        methodName = (useSetter) ? "set${property.capitalize()}" : property
    }
}
