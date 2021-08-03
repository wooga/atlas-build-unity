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
        def customArgsParts = customArgsParts(result.standardOutput)
        hasKeyValue("--build", "Player", customArgsParts)
        hasKeyValue("--config", appConfigFile.path, customArgsParts)
        hasKeyValue("--version", version, customArgsParts)
    }

    @Unroll
    def "can configure optional #argName argument"() {
        given: "a custom export task without configuration"
        buildFile << """
            task("customExport", type: UnityBuildEnginePlayerTask) {
                appConfigFile = "${escapedPath(appConfigFile.path)}"
                version = "0.0.1"  
                ${propName} = "${argValue}"         
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")

        then:
        def customArgsParts = customArgsParts(result.standardOutput)
        hasKeyValue(argName, argValue, customArgsParts)

        where:
        propName        | argName         | argValue
        "build"         | "--build"       | "CustomBuild"
        "versionCode"   |"--versionCode"  | "codeiguess"
        "toolsVersion"  |"--toolsVersion" | "10.0.1"
        "commitHash"    |"--commitHash"   | "a345fc"
    }

}
