package wooga.gradle.build.unity.tasks

import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.build.UnityIntegrationSpec

class UnityBuildEnginePlayerTaskIntegrationSpec extends UnityIntegrationSpec {

    @Shared
    File appConfigFile;

    def setup() {
        buildFile << "import wooga.gradle.build.unity.tasks.UnityBuildEnginePlayerTask\n".stripIndent()
        appConfigFile = createAppConfig("Assets/CustomConfigs")
    }

    def "uses default settings when configuring only with mandatory variables"() {
        given: "a custom export task without configuration"
        def version = "0.0.1"
        buildFile << """
            task("customExport", type: UnityBuildEnginePlayerTask) {
                appConfigFile = file("${escapedPath(appConfigFile.path)}")
                version = "${version}"               
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")
        then:
        def customArgsString = substringAt(result.standardOutput, "-CustomArgs")
        customArgsString.contains("--build Player")
        customArgsString.contains("--appConfig ${appConfigFile.path}")
        customArgsString.contains("--version ${version}")
    }

    @Unroll
    def "can configure optional #argName argument to be passed trough -CustomArgs"() {
        given: "a custom export task without configuration"
        buildFile << """
            task("customExport", type: UnityBuildEnginePlayerTask) {
                appConfigFile = file("${escapedPath(appConfigFile.path)}")
                version = "0.0.1"  
                ${propName} = "${argValue}"         
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")
        then:
        def customArgsString = substringAt(result.standardOutput, "-CustomArgs")
        customArgsString.contains("${argName} ${argValue}")
        where:
        propName        | argName         | argValue
        "build"         | "--build"       | "CustomBuild"
        "versionCode"   |"--versionCode"  | "codeiguess"
        "toolsVersion"  |"--toolsVersion" | "10.0.1"
        "commitHash"    |"--commitHash"   | "a345fc"
    }

}
