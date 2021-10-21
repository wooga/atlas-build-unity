package wooga.gradle.build.unity.tasks

import com.wooga.gradle.test.executable.FakeExecutables
import spock.lang.Unroll
import wooga.gradle.build.IntegrationSpec
import wooga.gradle.build.unity.secrets.internal.EncryptionSpecHelper
import wooga.gradle.secrets.internal.SecretText
import wooga.gradle.secrets.internal.Secrets

import javax.crypto.spec.SecretKeySpec

class ShellBuildIntegrationSpec extends IntegrationSpec {

    def "runs script file pipeline with secret values on environment"() {
        given: "test-build-publish sh executables"
        def buildSh = FakeExecutables.argsReflector(new File(projectDir, "fakeSh").absolutePath, 0)

        and: "a secrets file with a matching key"
        Secrets secrets = new Secrets()
        SecretKeySpec key = EncryptionSpecHelper.createSecretKey("some_value")
        secrets.putSecret(secretId, new SecretText(secretValue), key)

        and: "serialized key and secrets text"
        def secretsKey = File.createTempFile("atlas-build-unity.GradleBuild", ".key")
        def secretsFile = File.createTempFile("atlas-build-unity.GradleBuild", ".secrets.yaml")
        secretsKey.bytes = key.encoded
        secretsFile.text = secrets.encode()
        and: ""
        buildFile << """
            import javax.crypto.spec.SecretKeySpec
            task("customTask", type:wooga.gradle.build.unity.tasks.ScriptBuild) {
                dir = ${wrapValueBasedOnType(projectDir, File)}
                script = ${wrapValueBasedOnType(buildSh.executable, File)}
                secretsFile = project.file('${escapedPath(secretsFile.path)}')
                secretsKey = new SecretKeySpec(project.file('${escapedPath(secretsKey.path)}').bytes, 'AES')
                logsShellOutput = true
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully('customTask')

        then:
        def buildResult = buildSh.firstResult(result)
        buildResult.envs[secretId.toUpperCase()] == secretValue

        where:
        secretId  | secretValue
        "secret1" | "secret1Value"
        "secret2" | "secret2Value"

    }

    @Unroll
    def "runs script pipeline with given args and environment"() {
        given: ""
        def script = FakeExecutables.argsReflector(new File(projectDir, "fakeSh").absolutePath, 0)
        and: ""
        buildFile << """
            task("customTask", type:wooga.gradle.build.unity.tasks.ScriptBuild) {
                dir = ${wrapValueBasedOnType(projectDir, File)}
                script = ${wrapValueBasedOnType(script.executable, File)}
                arguments = ${wrapValueBasedOnType(arguments, List)}
                environment = ${wrapValueBasedOnType(environment, Map)}
                logsShellOutput = true
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully('customTask')

        then: "build shell has been executed with arguments"
        def scriptResult = script.firstResult(result)
        scriptResult != null
        scriptResult.args == arguments
        environment.every {scriptResult.envs.entrySet().contains(it)}

        where:
        arguments | environment
        ["arg", "k=v"] | ["a": "b", "c": "d"]
        ["k=x"]        | ["e": "f"]
    }

    @Unroll
    def "does #action output when 'logsShellOutput' is #logShellOutput"() {
        given: ""
        def buildSh = FakeExecutables.argsReflector(new File(projectDir, "fakeSh").absolutePath, 0)
        and: ""
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.ScriptBuild) {
                dir = ${wrapValueBasedOnType(projectDir, File)}
                script = ${wrapValueBasedOnType(buildSh.executable, File)}
                logsShellOutput = $logShellOutput
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully('externalGradle')

        then:
        (buildSh.firstResult(result) != null) == logShellOutput

        where:
        action        | logShellOutput
        "logs"        | true
        "doesn't log" | false

    }

}
