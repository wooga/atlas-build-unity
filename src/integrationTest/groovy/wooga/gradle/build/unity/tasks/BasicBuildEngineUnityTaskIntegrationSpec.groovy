/*
 * Copyright 2018-2020 Wooga GmbH
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
 */

package wooga.gradle.build.unity.tasks

import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import spock.lang.Shared
import wooga.gradle.build.UnityIntegrationSpec
import wooga.gradle.build.unity.secrets.internal.EncryptionSpecHelper
import wooga.gradle.secrets.internal.SecretText
import wooga.gradle.secrets.internal.Secrets

import javax.crypto.spec.SecretKeySpec

class BasicBuildEngineUnityTaskIntegrationSpec extends UnityIntegrationSpec {

    @Shared
    File configFile;

    def setup() {
        buildFile << "import wooga.gradle.build.unity.tasks.BasicBuildEngineUnityTask\n".stripIndent()
        configFile = createAppConfig("Assets/CustomConfigs")
    }

    def "uses default settings when not configured"() {
        given: "a custom export task without configuration"
        buildFile << """
            def ext = project.extensions.getByType(wooga.gradle.build.unity.UnityBuildPluginExtension)
            ext.customArguments.set(["--key":"value"])
            task("customExport", type: BasicBuildEngineUnityTask) {
                build = "UBSBuild"
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")

        then:
        def customArgsParts = unityArgs(result.standardOutput)
        hasKeyValue("--build", "UBSBuild", customArgsParts)
        hasKeyValue("--outputPath", new File(projectDir, "build/export").path, customArgsParts)
        hasKeyValue("--logPath", new File(projectDir, "build/export/logs").path, customArgsParts)
        hasKeyValue("--key", "value", customArgsParts)
        hasKeyValue("-executeMethod", "Wooga.UnifiedBuildSystem.Editor.BuildEngine.BuildFromEnvironment", customArgsParts)
        !customArgsParts.contains("--config")
    }

    def "fails if build property isn't set"() {
        given: "a custom export task without configuration"
        buildFile << """
            def ext = project.extensions.getByType(wooga.gradle.build.unity.UnityBuildPluginExtension)
            ext.customArguments.set(["--key":"value"])
            task("customExport", type: BasicBuildEngineUnityTask) {
            }
        """.stripIndent()

        when:
        runTasksSuccessfully("customExport")

        then:
        def e = thrown(GradleException)
        def rootE = rootCause(e)
        rootE.class.name == InvalidUserDataException.class.name
        rootE.message.contains("No value has been specified for property 'build'")
    }

    def "can configure custom unity entrypoint"() {
        given: "a export task with a custom unity entrypoint"
        buildFile << """
            task("customExport", type: BasicBuildEngineUnityTask) {
                build = "mandatoryBuildName"
                exportMethodName = "${entrypoint}"
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")

        then:
        def customArgsParts = unityArgs(result.standardOutput)
        hasKeyValue("-executeMethod", entrypoint, customArgsParts)

        where:
        entrypoint << ["CustomEntrypoint.Build"]
    }

    def "can configure custom output directory"() {
        given: "a export task with a custom output directory"
        buildFile << """
            task("customExport", type: BasicBuildEngineUnityTask) {
                build = "mandatoryBuildName"
                outputPath = "${outputPath}"
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")

        then:
        def customArgsParts = unityArgs(result.standardOutput)
        hasKeyValue("--outputPath", new File(projectDir, outputPath).path, customArgsParts)

        where:
        outputPath << ["custom"]
    }

    def "can configure custom log directory"() {
        given: "a export task with a custom log directory"
        buildFile << """
            task("customExport", type: BasicBuildEngineUnityTask) {
                build = "mandatoryBuildName"
                logPath = "${logPath}"
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")

        then:
        def customArgsParts = unityArgs(result.standardOutput)
        hasKeyValue("--logPath", new File(projectDir, logPath).path, customArgsParts)

        where:
        logPath << ["custom"]
    }


    def "can configure custom configuration"() {
        given: "a export task with a custom output directory"
        buildFile << """
            task("customExport", type: BasicBuildEngineUnityTask) {
                build = "mandatoryBuildName"
                config = "${escapedPath(configFile.path)}"
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")

        then:
        def customArgsParts = unityArgs(result.standardOutput)
        hasKeyValue("--config", configFile.absolutePath, customArgsParts)
    }

    def "can configure extra arguments"() {
        given: "a export task custom configuration"
        buildFile << """
            task("customExport", type: BasicBuildEngineUnityTask) {
                build = "mandatoryBuildName"
                customArguments = ${extraArgsString}
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")

        then:
        def customArgsParts = unityArgs(result.standardOutput)
        expectedCustomArgs.each { expectedArgs ->
            if(expectedArgs instanceof Map) {
                def argsPair = (expectedArgs as Map).entrySet().first() as Map.Entry<String, String>
                return hasKeyValue(argsPair.key, argsPair.value, customArgsParts)
            }
            return customArgsParts.contains(expectedArgs)
        }

        where:
        extraArgsString                                         | expectedCustomArgs
        """["--an-arg", ["--valued-arg":"value"]]"""            | ["--an-arg", ["--valued-arg": "value"]]
        """["--an-arg", "--valued-arg value"]"""                | ["--an-arg", ["--valued-arg": "value"]]
        """["--an-arg", ["--varg":"val", "--oarg":"oval"]]"""   | ["--an-arg", ["--varg": "val"], ["--oarg": "oval"]]
    }

    def "can configure encrypted secrets file"() {
        given: "a basic export task"
        buildFile << """
            import javax.crypto.spec.SecretKeySpec
            task("customExport", type: BasicBuildEngineUnityTask) {
                build = "mandatoryBuildName"
        """.stripIndent()

        and: "a secret key"
        SecretKeySpec key = EncryptionSpecHelper.createSecretKey("some_value")
        def secretsKey = File.createTempFile("atlas-build-unity.GradleBuild", ".key")
        secretsKey.bytes = key.encoded

        and: "a secrets file encoded with the key"
        def secretsFile = generateSecretsFile(key, secretsMap)

        and: "secrets and key configured in task"
        buildFile << """
            secretsFile = file('${escapedPath(secretsFile.path)}')
            secretsKey = new SecretKeySpec(file('${escapedPath(secretsKey.path)}').bytes, 'AES')
        }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")

        then:
        def customArgsString = substringAt(result.standardOutput, "environment")
        secretsMap.every {secretPair ->
            customArgsString.contains("${secretPair.key.toUpperCase()}=${secretPair.value}")
        }

        where:
        secretsMap << [["secretid": "secretvalue"], ["secretid": "secretvalue", "othersecid": "othervalue"]]
    }

    private static File generateSecretsFile(SecretKeySpec key, Map<String, String> secretsMap) {
        Secrets secrets = new Secrets()
        secretsMap.each{secretPair ->
            secrets.putSecret(secretPair.key, new SecretText(secretPair.value), key)
        }
        def secretsFile = File.createTempFile("atlas-build-unity.GradleBuild", ".secrets.yaml")
        secretsFile.text = secrets.encode()
        return secretsFile
    }

    Throwable rootCause(Throwable e) {
        if(e.cause == null) {
            return e
        }
        return rootCause(e.cause)

    }

}
