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

package wooga.gradle.unity.build

import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import spock.lang.Unroll

class UnityBuildPluginIntegrationSpec extends UnityIntegrationSpec {

    @Unroll
    def "can override default platforms/environments in extension with #propertyInstruction"() {
        given: "a default gradle project"

        assert runTasksWithFailure(taskName, "--dry-run")

        when: "change default platforms"
        buildFile << """
            ${propertyInstruction}
        """.stripIndent()

        then:
        runTasksSuccessfully(taskName, "--dry-run")

        where:
        taskName             | propertyInstruction
        "exportAlphaCi"      | "unityBuild.platforms = ['alpha']"
        "exportBetaCi"       | "unityBuild.platforms(['beta'])"
        "exportGammaCi"      | "unityBuild.platform('gamma')"
        "exportDeltaCi"      | "unityBuild.platforms('delta', 'epsilon')"
        "exportAndroidAlpha" | "unityBuild.environments = ['alpha']"
        "exportAndroidBeta"  | "unityBuild.environments(['beta'])"
        "exportAndroidGamma" | "unityBuild.environments('gamma')"
        "exportAndroidDelta" | "unityBuild.environments('delta', 'epsilon')"
        "exportZetaAlpha"    | "unityBuild.environments = ['alpha']\n unityBuild.platforms = ['zeta']"
        "exportEtaBeta"      | "unityBuild.environments(['beta'])\n unityBuild.platforms(['eta'])"
        "exportThetaGamma"   | "unityBuild.environments('gamma')\n unityBuild.platform('theta')"
        "exportIotaDelta"    | "unityBuild.environments('delta', 'epsilon')\n unityBuild.platforms('iota', 'kappa')"
    }

    @Unroll
    def "can override default platforms/environments in properties with #propertyInstruction"() {
        given: "a default gradle project"

        assert runTasksWithFailure(taskName, "--dry-run")
        when: "change default platforms"
        createFile("gradle.properties") << """
            ${propertyInstruction}
        """.stripIndent()

        then:
        runTasksSuccessfully(taskName, "--dry-run")

        where:
        taskName             | propertyInstruction
        "exportAlphaCi"      | "unityBuild.platforms=alpha,beta"
        "exportBetaCi"       | "unityBuild.platforms=alpha,beta"
        "exportAndroidAlpha" | "unityBuild.environments=alpha,beta"
        "exportAndroidBeta"  | "unityBuild.environments=alpha,beta"
        "exportZetaAlpha"    | "unityBuild.environments=alpha,beta\nunityBuild.platforms=zeta,kappa"
        "exportKappaBeta"    | "unityBuild.environments=alpha,beta\nunityBuild.platforms=zeta,kappa"
    }

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

    @Unroll
    def "can override default platforms/environments in environment with #propertyInstruction"() {
        given: "a default gradle project"

        assert runTasksWithFailure(taskName, "--dry-run")

        when: "change default platforms"
        propertyInstruction.each { k, v ->
            environmentVariables.set(k, v)
        }

        then:
        runTasksSuccessfully(taskName, "--dry-run")

        where:
        taskName             | propertyInstruction
        "exportAlphaCi"      | ['UNITY_BUILD_PLATFORMS': "alpha,beta"]
        "exportBetaCi"       | ['UNITY_BUILD_PLATFORMS': "alpha,beta"]
        "exportAndroidAlpha" | ['UNITY_BUILD_ENVIRONMENTS': "alpha,beta"]
        "exportAndroidBeta"  | ['UNITY_BUILD_ENVIRONMENTS': "alpha,beta"]
        "exportZetaAlpha"    | ['UNITY_BUILD_ENVIRONMENTS': "alpha,beta", 'UNITY_BUILD_PLATFORMS': "zeta,kappa"]
        "exportKappaBeta"    | ['UNITY_BUILD_ENVIRONMENTS': "alpha,beta", 'UNITY_BUILD_PLATFORMS': "zeta,kappa"]
    }

    @Unroll
    def ":#taskToRun calls Untity export method with correct arguments"() {
        given: "a default gradle project"

        when:
        def result = runTasksSuccessfully(taskToRun)

        then:
        result.standardOutput.contains("-executeMethod Wooga.UnityBuild.NewAutomatedBuild.Export")
        result.standardOutput.contains(expectedParameters)

        where:
        taskToRun                 | expectedParameters
        "exportAndroidCi"         | "-CustomArgs:platform=android;environment=ci"
        "exportAndroidStaging"    | "-CustomArgs:platform=android;environment=staging"
        "exportAndroidProduction" | "-CustomArgs:platform=android;environment=production"
        "exportIOSCi"             | "-CustomArgs:platform=iOS;environment=ci"
        "exportIOSStaging"        | "-CustomArgs:platform=iOS;environment=staging"
        "exportIOSProduction"     | "-CustomArgs:platform=iOS;environment=production"
        "exportWebGLCi"           | "-CustomArgs:platform=webGL;environment=ci"
        "exportWebGLStaging"      | "-CustomArgs:platform=webGL;environment=staging"
        "exportWebGLProduction"   | "-CustomArgs:platform=webGL;environment=production"
    }

    @Unroll
    def "can override exportMethodName in #location with #value"() {
        given: "execute on a default project"
        assert runTasksSuccessfully("exportAndroidCi").standardOutput.contains("-executeMethod Wooga.UnityBuild.NewAutomatedBuild.Export")

        def extensionKey = 'unityBuild.exportMethodName'
        def propertiesKey = 'unityBuild.exportMethodName'
        def envKey = 'UNITY_BUILD_EXPORT_METHOD_NAME'

        if (location == "environment") {
            envs.set(envKey, value)
        } else if (location == "properties") {
            createFile("gradle.properties") << "${propertiesKey}=${value}"
        } else {
            buildFile << "${extensionKey}('${value}')"
        }

        when:
        def result = runTasksSuccessfully("exportAndroidCi")

        then:
        result.standardOutput.contains("-executeMethod ${value}")

        where:
        value   | location
        "test1" | 'environment'
        "test2" | 'properties'
        "test3" | 'extension'
    }

    @Unroll
    def ":#taskToRun executes default export task"() {
        given: "a default gradle project"
        def expectedExportTask = "export${expectedDefaultHandlerTask}"
        def handleTaskName = "${taskToRun}${expectedDefaultHandlerTask}"

        when:
        def result = runTasks(taskToRun)

        then:
        result.wasExecuted(expectedExportTask)
        result.wasExecuted(handleTaskName)

        where:
        taskToRun  | expectedDefaultHandlerTask
        "assemble" | "AndroidCi"
        "check"    | "AndroidCi"
    }

    @Rule
    public final EnvironmentVariables envs = new EnvironmentVariables()

    @Unroll
    def ":#taskToRun executes override default #override in #location with #value task"() {
        given: "a default gradle project with adjusted default platform/environment settings"
        def extensionKey = override == 'platform' ? 'unityBuild.defaultPlatform' : 'unityBuild.defaultEnvironment'
        def propertiesKey = override == 'platform' ? 'unityBuild.platform' : 'unityBuild.environment'
        def envKey = override == 'platform' ? 'UNITY_BUILD_PLATFORM' : 'UNITY_BUILD_ENVIRONMENT'

        if (location == "environment") {
            envs.set(envKey, value)
        } else if (location == "properties") {
            createFile("gradle.properties") << "${propertiesKey}=${value}"
        } else {
            buildFile << "${extensionKey}('${value}')"
        }

        def expectedExportTask = "export${expectedDefaultHandlerTask}"
        def handleTaskName = "${taskToRun}${expectedDefaultHandlerTask}"

        when:
        def result = runTasks(taskToRun)

        then:
        result.wasExecuted(expectedExportTask)
        result.wasExecuted(handleTaskName)

        where:
        taskToRun  | override      | location      | value     | expectedDefaultHandlerTask
        "assemble" | "platform"    | "environment" | 'iOS'     | 'IOSCi'
        "assemble" | "platform"    | "properties"  | 'iOS'     | 'IOSCi'
        "assemble" | "platform"    | "extension"   | 'iOS'     | 'IOSCi'
        "check"    | "platform"    | "environment" | 'iOS'     | 'IOSCi'
        "check"    | "platform"    | "properties"  | 'iOS'     | 'IOSCi'
        "check"    | "platform"    | "extension"   | 'iOS'     | 'IOSCi'
        "assemble" | "environment" | "environment" | 'staging' | 'AndroidStaging'
        "assemble" | "environment" | "properties"  | 'staging' | 'AndroidStaging'
        "assemble" | "environment" | "extension"   | 'staging' | 'AndroidStaging'
        "check"    | "environment" | "environment" | 'staging' | 'AndroidStaging'
        "check"    | "environment" | "properties"  | 'staging' | 'AndroidStaging'
        "check"    | "environment" | "extension"   | 'staging' | 'AndroidStaging'
    }
}
