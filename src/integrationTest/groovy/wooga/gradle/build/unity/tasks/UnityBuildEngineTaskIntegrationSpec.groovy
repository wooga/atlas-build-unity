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

import spock.lang.Shared
import wooga.gradle.build.UnityIntegrationSpec
import wooga.gradle.build.unity.secrets.internal.EncryptionSpecHelper
import wooga.gradle.secrets.internal.SecretText
import wooga.gradle.secrets.internal.Secrets

import javax.crypto.spec.SecretKeySpec
import java.nio.file.Paths

class UnityBuildEngineTaskIntegrationSpec extends UnityIntegrationSpec {

    @Shared
    File configFile;

    def setup() {
        buildFile << "import wooga.gradle.build.unity.tasks.UnityBuildEngineTask\n".stripIndent()
        configFile = createAppConfig("Assets/CustomConfigs")
    }

    def "uses default settings when not configured"() {
        given: "a custom export task without configuration"
        buildFile << """
            def ext = project.extensions.getByType(wooga.gradle.build.unity.UnityBuildPluginExtension)
            ext.customArguments.set([key:"value"])
            task("customExport", type: UnityBuildEngineTask) {
                build = "UBSBuild"
            }
        """.stripIndent()
        and:
        when:
        def result = runTasksSuccessfully("customExport")

        then:
        result.standardOutput.contains("-executeMethod Wooga.UnifiedBuildSystem.Editor.BuildEngine.BuildFromEnvironment")
        result.standardOutput.contains("--build UBSBuild")
        result.standardOutput.contains("--outputPath ${new File(projectDir, "build/export").path}")
        result.standardOutput.contains("key value")
        !result.standardOutput.contains("--config")
    }

    def "can configure custom unity entrypoint"() {
        given: "a export task with a custom unity entrypoint"
        buildFile << """
            task("customExport", type: UnityBuildEngineTask) {
                build = "mandatoryBuildName"
                exportMethodName = "${entrypoint}"
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")

        then:
        result.standardOutput.contains("-executeMethod ${entrypoint}")

        where:
        entrypoint << ["CustomEntrypoint.Build"]
    }

    def "can configure custom output directory"() {
        given: "a export task with a custom output directory"
        buildFile << """
            task("customExport", type: UnityBuildEngineTask) {
                build = "mandatoryBuildName"
                outputPath = "${outputPath}"
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")

        then:
        def customArgsString = substringAt(result.standardOutput, "-CustomArgs")
        customArgsString.contains("--outputPath ${new File(projectDir, outputPath).path}")

        where:
        outputPath << ["custom"]
    }

    def "can configure custom configuration"() {
        given: "a export task with a custom output directory"
        buildFile << """
            task("customExport", type: UnityBuildEngineTask) {
                build = "mandatoryBuildName"
                config = "${escapedPath(configFile.path)}"
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")

        then:
        def customArgsString = substringAt(result.standardOutput, "-CustomArgs")
        customArgsString.contains("--config ${configFile.absolutePath}")
    }

    def "can configure extra arguments"() {
        given: "a export task custom configuration"
        buildFile << """
            task("customExport", type: UnityBuildEngineTask) {
                build = "mandatoryBuildName"
                customArguments = ${extraArgsString}
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")

        then:
        def customArgsString = substringAt(result.standardOutput, "-CustomArgs:")
        customArgsString.contains(expectedCustomArgs)

        where:
        extraArgsString                                         | expectedCustomArgs
        """["--an-arg", ["--valued-arg":"value"]]"""            | "--an-arg --valued-arg value"
        """["--an-arg", "--valued-arg value"]"""                | "--an-arg --valued-arg value"
        """["--an-arg", ["--varg":"val", "--oarg":"oval"]]"""   | "--an-arg --varg val --oarg oval"
    }

    def "can configure encrypted secrets file"() {
        given: "a basic export task"
        buildFile << """
            import javax.crypto.spec.SecretKeySpec
            task("customExport", type: UnityBuildEngineTask) {
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

}
