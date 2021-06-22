package wooga.gradle.build.unity.ios.tasks

import com.wooga.security.MacOsKeychain
import com.wooga.security.SecurityHelper
import com.wooga.spock.extensios.security.Keychain
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.build.IntegrationSpec

@Requires({ os.macOs })
class ImportCodeSigningIdentityIntegrationSpec extends IntegrationSpec {

    String testTaskName = "importSigningIdentity"
    Class taskType = ImportCodeSigningIdentity

    @Keychain(unlockKeychain = true)
    MacOsKeychain buildKeychain

    def setup() {
        buildFile << """
        task ${testTaskName}(type: ${taskType.name}) {
            keychain = file('${buildKeychain.location}')
        }
        """.stripIndent()
    }

    @Unroll("import #taskStatus when #reason")
    def "imports signing identity"() {
        given: "an invalid sighing certificate"
        def passphrase = "123456"
        def cert = SecurityHelper.createTestCodeSigningCertificatePkcs12([commonName: testSigningIdentity], passphrase)
        buildFile << """
        ${testTaskName} {
            passphrase = "${passphrase}"
            p12 = file('${cert.path}')
            signingIdentity = "${expectedSigningIdentity}"
            ignoreInvalidSigningIdentity = ${ignoreInvalidSigningIdentities}
        }
        """.stripIndent()

        when:
        def result = runTasks(testTaskName)

        then:
        result.success == taskSuccessfull
        outputContains(result, expectedLogMessage)

        where:
        testSigningIdentity        | signingIdentity          | ignoreInvalidSigningIdentities || taskSuccessfull | reason                                           | logMessage
        "test signing: Wooga GmbH" | _                        | false                          || false           | "imported identity is invalid"                   | "Unable to find valid code sign identity '#expectedSigningIdentity' in keychain"
        "test signing: Wooga GmbH" | _                        | true                           || true            | "imported identity is invalid but error ignored" | "Signing Identity '#expectedSigningIdentity' found but invalid"
        "test signing: Wooga GmbH" | "some signing: Foo GmbH" | true                           || false           | "expected identity was not imported"             | "Unable to find code sign identity '#expectedSigningIdentity' in keychain"

        expectedSigningIdentity = signingIdentity == _ ? testSigningIdentity : signingIdentity
        expectedLogMessage = logMessage.toString().replaceAll("#expectedSigningIdentity", expectedSigningIdentity.toString())
        taskStatus = taskSuccessfull ? "succeeds" : "fails"
    }

    @Unroll("can set property #property with #method and type #type")
    def "can set property"() {
        given: "a task to read back the value"
        buildFile << """
            task("readValue") {
                doLast {
                    println("property: " + ${testTaskName}.${property}.get())
                }
            }
        """.stripIndent()
        and: "a set property"

        buildFile << """
            ${testTaskName}.${method}($value)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        outputContains(result, "property: " + expectedValue.toString())

        where:
        property                       | method                             | rawValue             | type
        "ignoreInvalidSigningIdentity" | "ignoreInvalidSigningIdentity"     | true                 | "Boolean"
        "ignoreInvalidSigningIdentity" | "ignoreInvalidSigningIdentity"     | false                | "Provider<Boolean>"
        "ignoreInvalidSigningIdentity" | "ignoreInvalidSigningIdentity.set" | true                 | "Boolean"
        "ignoreInvalidSigningIdentity" | "ignoreInvalidSigningIdentity.set" | false                | "Provider<Boolean>"
        "ignoreInvalidSigningIdentity" | "setIgnoreInvalidSigningIdentity"  | true                 | "Boolean"
        "ignoreInvalidSigningIdentity" | "setIgnoreInvalidSigningIdentity"  | false                | "Provider<Boolean>"

        "p12"                          | "p12"                              | "/path/to/p12"       | "File"
        "p12"                          | "p12"                              | "/path/to/p12"       | "Provider<RegularFile>"
        "p12"                          | "p12.set"                          | "/path/to/p12"       | "File"
        "p12"                          | "p12.set"                          | "/path/to/p12"       | "Provider<RegularFile>"
        "p12"                          | "setP12"                           | "/path/to/p12"       | "File"
        "p12"                          | "setP12"                           | "/path/to/p12"       | "Provider<RegularFile>"

        "keychain"                     | "keychain"                         | "/path/to/keychain1" | "File"
        "keychain"                     | "keychain"                         | "/path/to/keychain2" | "Provider<RegularFile>"
        "keychain"                     | "keychain.set"                     | "/path/to/keychain3" | "File"
        "keychain"                     | "keychain.set"                     | "/path/to/keychain4" | "Provider<RegularFile>"
        "keychain"                     | "setKeychain"                      | "/path/to/keychain5" | "File"
        "keychain"                     | "setKeychain"                      | "/path/to/keychain6" | "Provider<RegularFile>"

        "passphrase"                   | "passphrase"                       | "testPassphrase1"    | "String"
        "passphrase"                   | "passphrase"                       | "testPassphrase2"    | "Provider<String>"
        "passphrase"                   | "passphrase.set"                   | "testPassphrase3"    | "String"
        "passphrase"                   | "passphrase.set"                   | "testPassphrase4"    | "Provider<String>"
        "passphrase"                   | "setPassphrase"                    | "testPassphrase5"    | "String"
        "passphrase"                   | "setPassphrase"                    | "testPassphrase6"    | "Provider<String>"

        "signingIdentity"              | "signingIdentity"                  | "code sign: ID1"     | "String"
        "signingIdentity"              | "signingIdentity"                  | "code sign: ID2"     | "Provider<String>"
        "signingIdentity"              | "signingIdentity.set"              | "code sign: ID3"     | "String"
        "signingIdentity"              | "signingIdentity.set"              | "code sign: ID4"     | "Provider<String>"
        "signingIdentity"              | "setSigningIdentity"               | "code sign: ID5"     | "String"
        "signingIdentity"              | "setSigningIdentity"               | "code sign: ID6"     | "Provider<String>"
        value = wrapValueBasedOnType(rawValue, type)
        expectedValue = rawValue
    }

    def "skips task if identity is already in keychain"() {
        def passphrase = "123456"
        def identityName = "codesign test"
        def cert = SecurityHelper.createTestCodeSigningCertificatePkcs12([commonName: identityName], passphrase)

        buildKeychain.importFile(cert, [passphrase: passphrase])

        buildFile << """
        ${testTaskName} {
            passphrase = "${passphrase}"
            p12 = file('${cert.path}')
            signingIdentity = "${identityName}"
            ignoreInvalidSigningIdentity = true
        }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(testTaskName)

        then:
        result.wasSkipped(testTaskName)
    }
}
