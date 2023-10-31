/*
 * Copyright 2018 Wooga GmbH
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

package wooga.gradle.build


import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.yaml.snakeyaml.Yaml
import spock.genesis.Gen
import spock.genesis.transform.Iterations
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.build.unity.UBSVersion
import wooga.gradle.build.unity.UnityBuildPlugin
import wooga.gradle.build.unity.UnityBuildPluginConventions
import wooga.gradle.unity.models.BuildTarget
import wooga.gradle.unity.models.UnityCommandLineOption

import static com.wooga.gradle.PlatformUtils.escapedPath

class UnityBuildPluginIntegrationSpec extends UnityIntegrationSpec {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

    @Shared
    File configsDir

    @Shared
    File configsDir2

    def setup() {
        //create the default location for app configs
        def assets = new File(projectDir, "Assets")
        configsDir = new File(assets, "UnifiedBuildSystem-Assets/AppConfigs")
        configsDir2 = new File(assets, "UnifiedBuildSystem-Assets/Configs")
        configsDir.mkdirs()
        configsDir2.mkdirs()

        ['ios_ci', 'android_ci', 'webGL_ci'].collectMany { [createFile("${it}.asset", configsDir), createFile("${it}.asset", configsDir2)] }.each {
            it << UNITY_ASSET_HEADER
            it << "\n"
            Yaml yaml = new Yaml()
            def buildTarget = it.name.split(/_/, 2).first().toLowerCase()
            def config = ['MonoBehaviour': ['bundleId': 'net.wooga.test', 'batchModeBuildTarget': buildTarget]]
            it << yaml.dump(config)
        }

        Yaml yaml = new Yaml()
        [configsDir, configsDir2].each {
            def config = createFile("custom.asset", it)
            config << UNITY_ASSET_HEADER
            config << "\n"
            config << yaml.dump(['MonoBehaviour': ['bundleId': 'net.wooga.test']])
        }
    }

    @Unroll
    def "#taskToRun calls Unity export method with buildType fetched from config"() {
        given: "a project with multiple configs"
        and: "a custom config without buildTarget"

        when:
        def result = runTasksSuccessfully(taskToRun)

        then:
        result.standardOutput.contains("-executeMethod ${UnityBuildPluginConventions.EXECUTE_METHOD_NAME.value}")
        result.standardOutput.contains(expectedParameters)

        where:
        taskToRun         | expectedParameters
        "exportAndroidCi" | "${UnityCommandLineOption.buildTarget.flag} ${BuildTarget.android}"
        "exportIosCi"     | "${UnityCommandLineOption.buildTarget.flag} ${BuildTarget.ios}"
        "exportWebGLCi"   | "${UnityCommandLineOption.buildTarget.flag} ${BuildTarget.webgl}"
    }

    @Unroll
    def "#taskToRun calls Unity export method corresponding to ubs version #ubsVersion without buildType when not contained in config"() {
        given: "a project with multiple configs"
        and: "a custom config without buildTarget"
        and: "a set ubs version"
        buildFile << "import wooga.gradle.build.unity.UBSVersion"
        buildFile << (ubsVersion ? """
            import wooga.gradle.build.unity.UBSVersion
            unityBuild.ubsCompatibilityVersion=UBSVersion.${ubsVersion.name()}
        """ : "")
        when:
        def result = runTasksSuccessfully(taskToRun)

        then:
        result.standardOutput.contains("-executeMethod ${expectedMethod}")
        !result.standardOutput.contains(expectedParameters)

        where:
        taskToRun      | expectedMethod                                      | expectedParameters                      | ubsVersion
        "exportCustom" | UnityBuildPluginConventions.EXECUTE_METHOD_NAME.value | "${UnityCommandLineOption.buildTarget}" | null
        "exportCustom" | UnityBuildPluginConventions.EXECUTE_METHOD_NAME.value | "${UnityCommandLineOption.buildTarget}" | UBSVersion.v160
    }

    String convertPropertyToEnvName(String property) {
        property.replaceAll(/([A-Z.])/, '_$1').replaceAll(/[.]/, '').toUpperCase()
    }

    @Unroll
    def "can override property #property in #location v2"() {

        when:
        def query = runPropertyQuery(getter, setter)

        then:
        query.matches(rawValue, type)

        where:
        property              | rawValue                                    | type     | location
        "exportMethodName"    | "test1"                                     | ""       | PropertyLocation.environment
        "exportMethodName"    | "test2"                                     | ""       | PropertyLocation.property
        "exportMethodName"    | "test3"                                     | "String" | PropertyLocation.script

        "toolsVersion"        | "1.2.3"                                     | ""       | PropertyLocation.environment
        "toolsVersion"        | "3.2.1"                                     | ""       | PropertyLocation.property
        "toolsVersion"        | "2.1.3"                                     | "String" | PropertyLocation.script

        "version"             | "1.2.3"                                     | ""       | PropertyLocation.environment
        "version"             | "3.2.1"                                     | ""       | PropertyLocation.property
        "version"             | "2.1.3"                                     | "String" | PropertyLocation.script

        "versionCode"         | "10203"                                     | ""       | PropertyLocation.environment
        "versionCode"         | "30201"                                     | ""       | PropertyLocation.property
        "versionCode"         | "20103"                                     | "String" | PropertyLocation.script

        "commitHash"          | "abcdefg"                                   | ""       | PropertyLocation.environment
        "commitHash"          | "gfedcba"                                   | ""       | PropertyLocation.property
        "commitHash"          | "1234567"                                   | "String" | PropertyLocation.script
        "outputDirectoryBase" | File.createTempDir("build", "export1").path | "File"   | PropertyLocation.script

        setter = new PropertySetterWriter(UnityBuildPlugin.EXTENSION_NAME, property)
            .set(rawValue, type)
            .to(location)

        getter = new PropertyGetterTaskWriter(setter)
    }

    @Unroll
    def "#taskToRun executes default export task"() {
        given: "a default gradle project"
        def expectedExportTask = "export${expectedDefaultHandlerTask}"
        def handleTaskName = "${taskToRun}${expectedDefaultHandlerTask}"

        buildFile << """
        unityBuild.defaultAppConfigName = 'android_ci'
        """.stripIndent()

        when:
        def result = runTasks(taskToRun)

        then:
        result.wasExecuted(expectedExportTask)
        result.wasExecuted(handleTaskName)

        where:
        taskToRun  | expectedDefaultHandlerTask
        "assemble" | "AndroidCi"
        "check"    | "AndroidCi"
        "publish"  | "AndroidCi"
    }

    @Rule
    public final EnvironmentVariables envs = new EnvironmentVariables()

    @Unroll
    def "#taskToRun executes #expectedHandlerTask when override default #override in #location with #value task"() {
        given: "a default gradle project with adjusted default platform/environment settings"
        def extensionKey = 'unityBuild.defaultAppConfigName'
        def propertiesKey = 'unityBuild.defaultAppConfigName'
        def envKey = 'UNITY_BUILD_DEFAULT_APP_CONFIG_NAME'

        envs.clear(envKey)

        if (location == "environment") {
            envs.set(envKey, value)
        } else if (location == "properties") {
            createFile("gradle.properties") << "${propertiesKey}=${value}"
        } else {
            buildFile << "${extensionKey} = '${value}'"
        }

        def expectedExportTask = "export${expectedDefaultHandlerTask}"
        def handleTaskName = "${expectedHandlerTask}"

        when:
        def result = runTasks(taskToRun)

        then:
        result.wasExecuted(expectedExportTask)
        result.wasExecuted(handleTaskName)

        where:
        taskToRun  | override               | location      | value        | expectedDefaultHandlerTask
        "assemble" | "defaultAppConfigName" | "environment" | 'ios_ci'     | 'IosCi'
        "assemble" | "defaultAppConfigName" | "properties"  | 'android_ci' | 'AndroidCi'
        "assemble" | "defaultAppConfigName" | "extension"   | 'webGL_ci'   | 'WebGLCi'
        "check"    | "defaultAppConfigName" | "environment" | 'ios_ci'     | 'IosCi'
        "check"    | "defaultAppConfigName" | "properties"  | 'android_ci' | 'AndroidCi'
        "check"    | "defaultAppConfigName" | "extension"   | 'webGL_ci'   | 'WebGLCi'
        "publish"  | "defaultAppConfigName" | "environment" | 'ios_ci'     | 'IosCi'
        "publish"  | "defaultAppConfigName" | "properties"  | 'android_ci' | 'AndroidCi'
        "publish"  | "defaultAppConfigName" | "extension"   | 'webGL_ci'   | 'WebGLCi'
        "export"   | "defaultAppConfigName" | "environment" | 'ios_ci'     | 'IosCi'
        "export"   | "defaultAppConfigName" | "properties"  | 'android_ci' | 'AndroidCi'
        "export"   | "defaultAppConfigName" | "extension"   | 'webGL_ci'   | 'WebGLCi'
        expectedHandlerTask = taskToRun + expectedDefaultHandlerTask
    }

    @Unroll
    def "skip export task action of task #taskToRun when skipExport is set in #location"() {
        given: "a default gradle project with adjusted default platform/environment settings"
        def extensionKey = 'unityBuild.skipExport'
        def propertiesKey = 'unityBuild.skipExport'
        def envKey = 'UNITY_BUILD_SKIP_EXPORT'

        envs.clear(envKey)

        if (location == "environment") {
            envs.set(envKey, value.toString())
        } else if (location == "properties") {
            createFile("gradle.properties") << "${propertiesKey}=${value}"
        } else {
            buildFile << "${extensionKey} = ${wrapValueBasedOnType(value, Boolean)}"
        }

        def expectedExportTask = "export${expectedDefaultHandlerTask}"

        when:
        def result = runTasks(taskToRun)

        then:
        result.wasExecuted(expectedExportTask)
        result.wasSkipped(expectedExportTask)
        result.wasExecuted(taskToRun)

        where:
        override     | location      | value | expectedDefaultHandlerTask | taskToRun
        "skipExport" | "environment" | true  | 'IosCi'                    | "assemble${expectedDefaultHandlerTask}"
        "skipExport" | "properties"  | true  | 'AndroidCi'                | "assemble${expectedDefaultHandlerTask}"
        "skipExport" | "extension"   | true  | 'WebGLCi'                  | "assemble${expectedDefaultHandlerTask}"
        "skipExport" | "environment" | true  | 'IosCi'                    | "check${expectedDefaultHandlerTask}"
        "skipExport" | "properties"  | true  | 'AndroidCi'                | "check${expectedDefaultHandlerTask}"
        "skipExport" | "extension"   | true  | 'WebGLCi'                  | "check${expectedDefaultHandlerTask}"
        "skipExport" | "environment" | true  | 'IosCi'                    | "publish${expectedDefaultHandlerTask}"
        "skipExport" | "properties"  | true  | 'AndroidCi'                | "publish${expectedDefaultHandlerTask}"
        "skipExport" | "extension"   | true  | 'WebGLCi'                  | "publish${expectedDefaultHandlerTask}"
    }

    @Unroll
    def "picks gradle version from config and executes exported project"() {
        given: "a default gradle project with adjusted default platform/environment settings"
        buildFile << "unityBuild.defaultConfigName = '${configName}'"

        def expectedExportTask = "export${expectedDefaultHandlerTask}"
        def handleTaskName = "${taskToRun}${expectedDefaultHandlerTask}"

        and: "an app config with configured gradle version"
        Yaml yaml = new Yaml()
        createFile("${configName}.asset", configsDir) << yaml.dump(['MonoBehaviour': ['bundleId': 'net.wooga.test', 'gradleVersion': version]])

        when:
        def result = runTasks(taskToRun)

        then:
        result.wasExecuted(expectedExportTask)
        result.wasExecuted(handleTaskName)

        def expectedVersion = version ?: "7.6.1"
        def error = "Could not execute build using connection to Gradle distribution 'https://services.gradle.org/distributions/gradle-${expectedVersion}-bin.zip'"
        // we expecting this task to fail because its not a real integration test
        // but gradle should have attempted to run the exported gradle project with the configured version
        // we simply check if the error contains the correct gradle version
        outputContains(result, error)

        where:
        taskToRun  | configName | version | expectedDefaultHandlerTask
        "assemble" | "android_ci"  | "5.0.0" | 'AndroidCi'
        "assemble" | "ios_ci"      | "4.4.0" | 'IosCi'
        "assemble" | "webGL_ci"    | null    | 'WebGLCi'
    }

    @Unroll
    def "default version #rawValue is converted to String from #type"() {
        given: "A custom project.version property"
        buildFile << """
            version = ${value}
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("exportAndroidCi")

        then:
        //the batchmode task will be called with version
        result.standardOutput.contains("version=$expectedValue;")

        where:
        rawValue | type       | expectedValue
        "1.0.0"  | "String"   | "1.0.0"
        "1.1.0"  | "Closure"  | "1.1.0"
        "1.1.1"  | "Callable" | "1.1.1"
        "2.0.0"  | "Object"   | "2.0.0"

        value = wrapValueBasedOnType(rawValue, type)
    }

    @Shared
    def characterPattern = ':_\\-<>|*\\\\? '

    @IgnoreIf({ os.windows })
    @Iterations(100)
    @Unroll
    def "generates task #expectedTaskName from config name #configName"() {
        given: "a project with custom app config directory"
        def assets = new File(projectDir, "Assets")
        def configsDir = new File(assets, "UnifiedBuildSystem-Assets/ConfigsCustom")
        configsDir.mkdirs()

        buildFile << """
        unityBuild.configsDirectory = file("${escapedPath(configsDir.path)}")
        """.stripIndent()

        and: "app config with delimiter in names"
        Yaml yaml = new Yaml()
        def config = ['MonoBehaviour': ['bundleId': 'net.wooga.test']]
        createFile("${configName}.asset", configsDir) << yaml.dump(config)

        expect:
        runTasksSuccessfully(expectedTaskName, '--dry-run')

        where:
        configName << Gen.these('test-config-file', 'test_config_file', 'test config file')
            .then(Gen.string(~/([$characterPattern]{1,5})test([$characterPattern]{1,5})config([$characterPattern]{1,5})file([$characterPattern]{1,5})/))
        expectedTaskName << Gen.any("assemble", "export", "check", "publish").map { "${it}TestConfigFile" }
    }


    def "sonarqube task run tests and sonar build"() {
        given: "applied sonarqube plugin"

        when:
        def result = runTasks("sonarqube", "--dry-run")

        then:
        def tasksLine = result.standardOutput.readLines().find { it.startsWith("Tasks to be executed:") }
        tasksLine.contains(":test")
        tasksLine.contains(":sonarBuildUnity")
        tasksLine.indexOf(":sonarqube") > tasksLine.indexOf(":test")
        tasksLine.indexOf(":sonarqube") > tasksLine.indexOf(":sonarBuildUnity")
    }

    def "sonarqube build task create solutions beforehand"() {
        given: "applied sonarqube plugin"

        when:
        def result = runTasks("sonarBuildUnity")

        then:
        result.wasExecuted(":generateSolution")
    }

    def "sonarqube build task runs after unity tests"() {
        given: "applied sonarqube plugin"

        when:
        def result = runTasks("sonarBuildUnity", "test")

        then:
        def tasksLine = result.standardOutput.readLines().find { it.startsWith("Tasks to be executed:") }
        tasksLine.indexOf(":sonarBuildUnity") > tasksLine.indexOf(":test")
    }
}
