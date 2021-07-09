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

import wooga.gradle.build.UnityIntegrationSpec
import wooga.gradle.build.unity.secrets.internal.EncryptionSpecHelper
import wooga.gradle.secrets.internal.SecretText
import wooga.gradle.secrets.internal.Secrets

import javax.crypto.spec.SecretKeySpec

class UnityBuildEngineTaskIntegrationSpec extends UnityIntegrationSpec {

    def setup() {
        buildFile << "import wooga.gradle.build.unity.tasks.UnityBuildEngineTask\n".stripIndent()
    }

    def "uses default settings when not configured"() {
        given: "a custom export task without configuration"
        buildFile << """
            task("customExport", type: UnityBuildEngineTask) {
                build = "UBSBuild"
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")

        then:
        result.standardOutput.contains("-executeMethod Wooga.UnifiedBuildSystem.Editor.BuildEngine.BuildFromEnvironment")
        result.standardOutput.contains("-CustomArgs:build=UBSBuild")
    }

    def "can configure custom unity entrypoint"() {
        given: "a export task with a custom unity entrypoint"
        buildFile << """
            task("customExport", type: UnityBuildEngineTask) {
                build = "mandatoryBuildName"
                unityMethodName = "${entrypoint}"
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
        customArgsString.contains("outputPath=${new File(projectDir, outputPath).path}")

        where:
        outputPath << ["custom"]
    }

    def "can configure extra arguments to be passed through unity's -CustomArgs"() {
        given: "a export task custom configuration"
        buildFile << """
            task("customExport", type: UnityBuildEngineTask) {
                build = "mandatoryBuildName"
                extraArgs = ${extraArgsString}
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")

        then:
        def customArgsString = substringAt(result.standardOutput, "-CustomArgs")
        customArgsString.contains(expectedCustomArgs)

        where:
        extraArgsString                             | expectedCustomArgs
        """["-an-arg", ["-valued-arg":"value"]]"""  | "-an-arg -valued-arg=value"
        """["-an-arg", "-valued-arg=value"]"""      | "-an-arg -valued-arg=value"
    }


    def "can configure encrypted secrets file to be passed through unity's -CustomArgs"() {
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
            secretsFile = project.file('${escapedPath(secretsFile.path)}')
            secretsKey = new SecretKeySpec(project.file('${escapedPath(secretsKey.path)}').bytes, 'AES')
        }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("customExport")

        then:
        def customArgsString = substringAt(result.standardOutput, "-CustomArgs")
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


    private static String substringAt(String base, String expression) {
        def customArgsIndex = base.indexOf(expression)
        return base.substring(customArgsIndex)
    }

}
