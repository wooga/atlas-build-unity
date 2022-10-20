package wooga.gradle.build.unity.tasks

import org.apache.commons.io.FilenameUtils
import org.gradle.api.GradleException
import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.build.UnityIntegrationSpec
import wooga.gradle.build.unity.UBSVersion

class UnityBuildPlayerIntegrationSpec extends UnityIntegrationSpec {

    @Shared
    File appConfigFile;

    def setup() {
        appConfigFile = createAppConfig("Assets/CustomConfigs")
    }

    @Unroll("uses default settings when configuring only with mandatory variables with #configProperty")
    def "uses default settings when configuring only with mandatory variables"() {
        given: "a custom export task without configuration"
        def version = "0.0.1"
        buildFile << """
            task("customExport", type: ${UnityBuildPlayer.class.name}) {
                ${configProperty} = ${wrapValueBasedOnType(configValue, configValueType)}
                version = "${version}"    
            }           
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")

        then:
        def appConfigName = FilenameUtils.removeExtension(new File(configValue).name)
        def actualConfigValue = configValueType == File ? new File(projectDir, configValue).absolutePath : configValue
        def customArgsParts = unityArgs(result.standardOutput)
        hasKeyValue("--build", "Player", customArgsParts)
        hasKeyValue("--${argName}".toString(), actualConfigValue, customArgsParts)
        hasKeyValue("--version", version, customArgsParts)
        hasKeyValue("--outputPath",
                new File(projectDir, "build/export/${appConfigName}/project").path, customArgsParts)
        if (argName == "configPath") {
            hasKeyValue("-buildTarget", "android", customArgsParts)
        }

        where:
        configProperty  | argName      | configValue                         | configValueType
        "config"        | "config"     | "configName"                        | String
        "configPath"    | "configPath" | "Assets/CustomConfigs/custom.asset" | File
        "appConfigFile" | "configPath" | "Assets/CustomConfigs/custom.asset" | File
    }

    @Unroll
    def "can configure optional #argName argument"() {
        given: "a custom export task without configuration"
        buildFile << """
            task("customExport", type: ${UnityBuildPlayer.class.name}) {
                config = "configPath"
                version = "0.0.1"  
                ${propName} = "${argValue}"         
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")

        then:
        def customArgsParts = unityArgs(result.standardOutput)
        hasKeyValue(argName, argValue, customArgsParts)

        where:
        propName       | argName          | argValue
        "build"        | "--build"        | "CustomBuild"
        "versionCode"  | "--versionCode"  | "codeiguess"
        "toolsVersion" | "--toolsVersion" | "10.0.1"
        "commitHash"   | "--commitHash"   | "a345fc"
    }

    def "throws exception when no config/configPath/appConfigFile property is given"() {
        given: "a custom export task without configuration"
        buildFile << """
            task("customExport", type: ${UnityBuildPlayer.class.name}) {
                version = "0.0.1"  
                outputDirectory = file("any")
            }
        """.stripIndent()

        when:
        runTasksSuccessfully("customExport")

        then:
        def e = thrown(GradleException)
        rootCause(e) instanceof IllegalArgumentException
    }

    @Unroll
    def "property #property will be mapped to cli parameter #expectedParamter when UBSCompatibility is #compatibility"() {
        buildFile << """
            task("customExport", type: ${UnityBuildPlayer.class.name}) {
                ubsCompatibilityVersion = ${wrapValueBasedOnType(compatibility, "UBSVersion", wrapValueFallback)}
                appConfigFile = ${wrapValueBasedOnType("Assets/CustomConfigs/custom.asset", "File")}
                ${property} = ${wrapValueBasedOnType(expectedValue, "String")}
            }           
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")

        then:
        result.standardOutput.contains("${expectedParamter} ${expectedValue}")

        where:
        property      | expectedParamter       | expectedValue | compatibility
        "version"     | "--version"            | "1.0.0"       | UBSVersion.v100
        "version"     | "--version"            | "2.0.0"       | UBSVersion.v120
        "version"     | "--build-version"      | "3.0.0"       | UBSVersion.v160
        "versionCode" | "--versionCode"       | "1"           | UBSVersion.v100
        "versionCode" | "--versionCode"       | "2"           | UBSVersion.v120
        "versionCode" | "--build-version-code" | "3"           | UBSVersion.v160
    }
}
