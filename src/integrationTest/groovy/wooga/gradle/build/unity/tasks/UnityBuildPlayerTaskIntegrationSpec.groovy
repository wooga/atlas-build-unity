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

package wooga.gradle.build.unity.tasks

import spock.lang.Unroll
import wooga.gradle.build.UnityIntegrationSpec

class UnityBuildPlayerTaskIntegrationSpec extends UnityIntegrationSpec {

    def setup() {
        buildFile << """
            task("exportCustom", type: wooga.gradle.build.unity.tasks.UnityBuildPlayerTask)
        """.stripIndent()
    }

    def "uses default settings when not configured"() {
        given: "a custom export task without configuration"

        when:
        def result = runTasksSuccessfully("exportCustom")

        then:
        result.standardOutput.contains("-executeMethod Wooga.UnifiedBuildSystem.Build.Export")
        result.standardOutput.contains("platform=android")
        result.standardOutput.contains("environment=ci")
        !result.standardOutput.contains("toolsVersion=")
    }

    @Unroll
    def "can override optional property #property with #methodName with value: #value"() {
        given: "a export task with toolsVersion configuration"
        buildFile << """
            exportCustom.${methodName}('${value}')
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("exportCustom")

        then:

        result.standardOutput.contains("toolsVersion=${value}")
        //rest of the values are default
        result.standardOutput.contains("-executeMethod Wooga.UnifiedBuildSystem.Build.Export")
        result.standardOutput.contains("platform=android")
        result.standardOutput.contains("environment=ci")

        where:
        property       | value   | useSetter
        "toolsVersion" | "1.2.3" | true
        "toolsVersion" | "3.2.1" | false
        methodName = (useSetter) ? "set${property.capitalize()}" : property
    }

    @Unroll
    def "can override #property with #methodName with value: #vsalue"() {
        given: "a export task with custom configuration"
        buildFile << """
            exportCustom.${methodName}('${value}')
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("exportCustom")

        then:

        result.standardOutput.contains("-executeMethod ${expectedExportMethod}")
        result.standardOutput.contains("platform=${expectedPlatform}")
        result.standardOutput.contains("-buildTarget ${expectedPlatform}")
        result.standardOutput.contains("environment=${expectedEnvironment}")

        where:
        property           | value          | useSetter | expectedExportMethod                    | expectedEnvironment | expectedPlatform
        "exportMethodName" | "method1"      | true      | "method1"                               | 'ci'                | 'android'
        "exportMethodName" | "method2"      | false     | "method2"                               | 'ci'                | 'android'
        "buildEnvironment" | "environment1" | true      | 'Wooga.UnifiedBuildSystem.Build.Export' | "environment1"      | 'android'
        "buildEnvironment" | "environment2" | false     | 'Wooga.UnifiedBuildSystem.Build.Export' | "environment2"      | 'android'
        "buildPlatform"    | "platform1"    | true      | 'Wooga.UnifiedBuildSystem.Build.Export' | 'ci'                | "platform1"
        "buildPlatform"    | "platform2"    | false     | 'Wooga.UnifiedBuildSystem.Build.Export' | 'ci'                | "platform2"
        methodName = (useSetter) ? "set${property.capitalize()}" : property
    }
}
