package wooga.gradle.build.unity.tasks

import org.gradle.api.Task
import spock.lang.Unroll
import wooga.gradle.build.UnityIntegrationSpec
import wooga.gradle.build.unity.secrets.internal.EncryptionSpecHelper

import static com.wooga.gradle.PlatformUtils.escapedPath

class SecretSpecIntegrationSpec extends UnityIntegrationSpec {

    @Unroll("#containerTypeName of type #containerType.name can set secrets key with #method(#type)")
    def "can set secrets key"() {
        given: "secret key saved to disc"
        def key = EncryptionSpecHelper.createSecretKey("a random key")
        def keyFile = File.createTempFile("secret", "key")
        def outputKeyFile = File.createTempFile("secretOut", "key")
        def keyPath = escapedPath(keyFile.path)
        keyFile.bytes = key.encoded

        and: "the value to set"
        def value = ""
        if (type == "key") {
            value = "new javax.crypto.spec.SecretKeySpec(project.file('${keyPath}').bytes, 'AES')"
        } else if (type == "keyFile") {
            value = "project.file('${keyPath}')"
        }

        and: "the key configured"
        if (Task.isAssignableFrom(containerType)) {
            buildFile << """
                task("temp", type: ${containerType.name}) {
                    ${method}(${value})
                }
            """.stripIndent()
        } else {
            buildFile << """
                extensions.create('temp', ${containerType.name}, project)
                temp.${method}(${value})
            """.stripIndent()
        }

        and: "a task to write out the key"
        buildFile << """
            task("writeKey") {
                doLast {
                    def output = new File("${escapedPath(outputKeyFile.path)}")
                    output.bytes = temp.secretsKey.get().encoded
                }
            }
        """

        when:
        runTasksSuccessfully("writeKey")

        then:
        outputKeyFile.exists()
        outputKeyFile.bytes == keyFile.bytes

        cleanup:
        keyFile.delete()
        outputKeyFile.delete()

        where:
        containerType                    | property         | type      | useSetter
        GradleBuild                      | "secretsKey"     | "key"     | false
        GradleBuild                      | "secretsKey"     | "key"     | true
        GradleBuild                      | "secretsKey.set" | "key"     | false
        GradleBuild                      | "secretsKey"     | "keyFile" | false
        GradleBuild                      | "secretsKey"     | "keyFile" | true

        UnityBuildPlayerTask             | "secretsKey"     | "key"     | false
        UnityBuildPlayerTask             | "secretsKey"     | "key"     | true
        UnityBuildPlayerTask             | "secretsKey.set" | "key"     | false
        UnityBuildPlayerTask             | "secretsKey"     | "keyFile" | false
        UnityBuildPlayerTask             | "secretsKey"     | "keyFile" | true

        method = (useSetter) ? "set${property.capitalize()}" : property
        containerTypeName = Task.isAssignableFrom(containerType) ? "task" : "extension"
    }
}
