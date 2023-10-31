package wooga.gradle.build.unity.tasks

import org.apache.commons.io.FilenameUtils
import org.gradle.api.GradleException
import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.build.unity.models.UBSVersion
import wooga.gradle.build.unity.models.BuildRequestOption

/**
 * Tests of the task for the default build request, {@link PlayerBuildUnityTask}
 */
class PlayerBuildUnityTaskIntegrationSpec extends BuildUnityTaskIntegrationSpec<PlayerBuildUnityTask> {

    @Shared
    File configFile;

    def setup() {
        configFile = createConfig("Assets/CustomConfigs")
    }

    @Unroll("uses default settings when configuring only with mandatory variables with #configProperty")
    def "uses default settings when configuring only with mandatory variables"() {
        given: "a custom export task without configuration"
        def version = "0.0.1"
        addSubjectTask(false, """
                ${configProperty} = ${wrapValueBasedOnType(configValue, configValueType)}
                version = "${version}"    
        """)

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        def configName = FilenameUtils.removeExtension(new File(configValue).name)
        def actualConfigValue = configValueType == File ? new File(projectDir, configValue).absolutePath : configValue

        def customArgsParts = unityArgs(result.standardOutput)
        hasKeyValue("--build", "Player", customArgsParts)
        hasKeyValue("--${argName}".toString(), actualConfigValue, customArgsParts)
        hasKeyValue("--build-version", version, customArgsParts)
        hasKeyValue("--outputPath",
            new File(projectDir, "build/export/${configName}/project").path, customArgsParts)
        if (argName == "configPath") {
            hasKeyValue("-buildTarget", "android", customArgsParts)
        }

        where:
        configProperty | argName      | configValue                         | configValueType
        "config"       | "config"     | "configName"                        | String
        "configPath"   | "configPath" | "Assets/CustomConfigs/custom.asset" | File
    }

    @Unroll
    def "can configure optional #argName argument"() {
        given: "a custom export task without configuration"
        addSubjectTask(false, """
                config = "configPath"
                version = "0.0.1"  
                ${propName} = "${argValue}"      
        """)

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        def customArgsParts = unityArgs(result.standardOutput)
        hasKeyValue(argName, argValue, customArgsParts)

        where:
        propName       | argName                              | argValue
        "build"        | BuildRequestOption.build.flag        | "CustomBuild"
        "versionCode"  | BuildRequestOption.versionCode.flag  | "codeiguess"
        "toolsVersion" | BuildRequestOption.toolsVersion.flag | "10.0.1"
        "commitHash"   | BuildRequestOption.commitHash.flag   | "a345fc"
    }

    def "throws exception when no config/configPath/configFile property is given"() {
        given: "a custom export task without configuration"
        addSubjectTask(false, """
                version = "0.0.1"  
                outputDirectory = file("any")
        """)

        when:
        runTasksSuccessfully(subjectUnderTestName)

        then:
        def e = thrown(GradleException)
        rootCause(e) instanceof IllegalArgumentException
    }

    @Unroll
    def "property #property will be mapped to cli parameter #expectedParamter when UBSCompatibility is #compatibility"() {

        given: "a configured task"
        addSubjectTask(false, """
                ubsCompatibilityVersion = ${wrapValueBasedOnType(compatibility, "UBSVersion", wrapValueFallback)}
                configPath = ${wrapValueBasedOnType("Assets/CustomConfigs/custom.asset", "File")}
                ${property} = ${wrapValueBasedOnType(expectedValue, "String")}
        """)

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.standardOutput.contains("${expectedParamter} ${expectedValue}")

        where:
        property      | expectedParamter       | expectedValue | compatibility
        "version"     | "--build-version"      | "3.0.0"       | UBSVersion.v160
        "versionCode" | "--build-version-code" | "3"           | UBSVersion.v160
    }
}
