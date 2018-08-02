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
        result.standardOutput.contains("version=unspecified")
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
    def "can override #property with #methodName with value: #value"() {
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
        result.standardOutput.contains("version=${expectedVersion}")

        where:
        property           | value          | useSetter | expectedExportMethod                    | expectedEnvironment | expectedPlatform | expectedVersion
        "exportMethodName" | "method1"      | true      | "method1"                               | 'ci'                | 'android'        | 'unspecified'
        "exportMethodName" | "method2"      | false     | "method2"                               | 'ci'                | 'android'        | 'unspecified'
        "buildEnvironment" | "environment1" | true      | 'Wooga.UnifiedBuildSystem.Build.Export' | "environment1"      | 'android'        | 'unspecified'
        "buildEnvironment" | "environment2" | false     | 'Wooga.UnifiedBuildSystem.Build.Export' | "environment2"      | 'android'        | 'unspecified'
        "buildPlatform"    | "platform1"    | true      | 'Wooga.UnifiedBuildSystem.Build.Export' | 'ci'                | "platform1"      | 'unspecified'
        "buildPlatform"    | "platform2"    | false     | 'Wooga.UnifiedBuildSystem.Build.Export' | 'ci'                | "platform2"      | 'unspecified'
        "version"          | "2.4.5"        | true      | 'Wooga.UnifiedBuildSystem.Build.Export' | 'ci'                | "android"        | '2.4.5'
        "version"          | "3.5.6"        | false     | 'Wooga.UnifiedBuildSystem.Build.Export' | 'ci'                | "android"        | '3.5.6'
        methodName = (useSetter) ? "set${property.capitalize()}" : property
    }

    @Unroll
    def "up-to-date check returns false when input paramter: #property changes"() {
        given: "up to date task"
        runTasksSuccessfully("exportCustom")
        def result = runTasksSuccessfully("exportCustom")
        assert result.wasUpToDate('exportCustom')

        when: "update an input property"
        buildFile << """
            exportCustom.${methodName}('${value}')
        """.stripIndent()

        result = runTasksSuccessfully("exportCustom")

        then:
        !result.wasUpToDate('exportCustom')

        where:
        property           | value          | useSetter
        "exportMethodName" | "method1"      | true
        "exportMethodName" | "method2"      | false
        "buildEnvironment" | "environment1" | true
        "buildEnvironment" | "environment2" | false
        "buildPlatform"    | "platform1"    | true
        "buildPlatform"    | "platform2"    | false
        "version"          | "1.0.1"        | true
        "version"          | "1.1.0"        | false
        methodName = (useSetter) ? "set${property.capitalize()}" : property
    }
}
