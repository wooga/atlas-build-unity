package wooga.gradle.build.unity.ios.tasks

import com.wooga.gradle.test.PropertyQueryTaskWriter
import com.wooga.security.MacOsKeychain
import com.wooga.security.SecurityHelper
import com.wooga.spock.extensios.security.Keychain
import spock.lang.Requires
import spock.lang.Unroll
import wooga.gradle.build.unity.ios.IOSBuildIntegrationSpec

import static com.wooga.gradle.test.PropertyUtils.toProviderSet
import static com.wooga.gradle.test.PropertyUtils.toSetter

@Requires({ os.macOs })
class ImportCodeSigningIdentitiesIntegrationSpec extends IOSBuildIntegrationSpec {

    String subjectUnderTestName = "importSigningIdentity"
    String subjectUnderTestTypeName = ImportCodeSigningIdentities.class.name

    @Keychain(unlockKeychain = true)
    MacOsKeychain buildKeychain

    def setup() {
        buildFile << """
        task ${subjectUnderTestName}(type: ${subjectUnderTestTypeName}) {
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
        ${subjectUnderTestName} {
            passphrase = "${passphrase}"
            p12 = file('${cert.path}')
            signingIdentity = "${expectedSigningIdentity}"
            ignoreInvalidSigningIdentity = ${ignoreInvalidSigningIdentities}
        }
        """.stripIndent()

        when:
        def result = runTasks(subjectUnderTestName)

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
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        query.write(buildFile)

        and: "a set property"
        appendToSubjectTask("${method}($value)")

        when:
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, expectedValue)

        where:
        property                       | method                      | rawValue              | returnValue | type
        "ignoreInvalidSigningIdentity" | property                    | true                  | _           | "Boolean"
        "ignoreInvalidSigningIdentity" | property                    | false                 | _           | "Provider<Boolean>"
        "ignoreInvalidSigningIdentity" | toProviderSet(property)     | true                  | _           | "Boolean"
        "ignoreInvalidSigningIdentity" | toProviderSet(property)     | false                 | _           | "Provider<Boolean>"
        "ignoreInvalidSigningIdentity" | toSetter(property)          | true                  | _           | "Boolean"
        "ignoreInvalidSigningIdentity" | toSetter(property)          | false                 | _           | "Provider<Boolean>"

        "p12"                          | property                    | "/path/to/p12"        | _           | "File"
        "p12"                          | property                    | "/path/to/p12"        | _           | "Provider<RegularFile>"
        "p12"                          | toProviderSet(property)     | "/path/to/p12"        | _           | "File"
        "p12"                          | toProviderSet(property)     | "/path/to/p12"        | _           | "Provider<RegularFile>"
        "p12"                          | toSetter(property)          | "/path/to/p12"        | _           | "File"
        "p12"                          | toSetter(property)          | "/path/to/p12"        | _           | "Provider<RegularFile>"

        "keychain"                     | property                    | "/path/to/keychain1"  | _           | "File"
        "keychain"                     | property                    | "/path/to/keychain2"  | _           | "Provider<RegularFile>"
        "keychain"                     | toProviderSet(property)     | "/path/to/keychain3"  | _           | "File"
        "keychain"                     | toProviderSet(property)     | "/path/to/keychain4"  | _           | "Provider<RegularFile>"
        "keychain"                     | toSetter(property)          | "/path/to/keychain5"  | _           | "File"
        "keychain"                     | toSetter(property)          | "/path/to/keychain6"  | _           | "Provider<RegularFile>"

        "passphrase"                   | property                    | "testPassphrase1"     | _           | "String"
        "passphrase"                   | property                    | "testPassphrase2"     | _           | "Provider<String>"
        "passphrase"                   | toProviderSet(property)     | "testPassphrase3"     | _           | "String"
        "passphrase"                   | toProviderSet(property)     | "testPassphrase4"     | _           | "Provider<String>"
        "passphrase"                   | toSetter(property)          | "testPassphrase5"     | _           | "String"
        "passphrase"                   | toSetter(property)          | "testPassphrase6"     | _           | "Provider<String>"

        "signingIdentities"            | toProviderSet(property)     | ["code sign: ID1"]    | _           | "List<String>"
        "signingIdentities"            | toProviderSet(property)     | ["code sign: ID2"]    | _           | "Provider<List<String>>"
        "signingIdentities"            | toSetter(property)          | ["code sign: ID3"]    | _           | "List<String>"
        "signingIdentities"            | toSetter(property)          | ["code sign: ID4"]    | _           | "Provider<List<String>>"
        "signingIdentities"            | toSetter("signingIdentity") | "code sign: ID3"      | [rawValue]  | "String"
        "signingIdentities"            | toSetter("signingIdentity") | "code sign: ID4"      | [rawValue]  | "Provider<String>"

        "applicationAccessPaths"       | toProviderSet(property)     | ["/usr/bin/codesign"] | _           | "List<String>"
        "applicationAccessPaths"       | toProviderSet(property)     | ["/usr/bin/codesign"] | _           | "Provider<List<String>>"
        "applicationAccessPaths"       | toSetter(property)          | ["/usr/bin/codesign"] | _           | "List<String>"
        "applicationAccessPaths"       | toSetter(property)          | ["/usr/bin/codesign"] | _           | "Provider<List<String>>"

        value = wrapValueBasedOnType(rawValue, type, wrapValueFallback)
        expectedValue = returnValue == _ ? rawValue : returnValue
    }

    @Unroll
    def "#method will #setType value for collection property #property"() {
        given: "a custom fetch secrets task"
        appendToSubjectTask(baseSetter)

        and: "a task to read back the value"
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        query.write(buildFile)

        and: "a set property"
        appendToSubjectTask("${method}($value)")

        when:
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, expectedValue)

        where:
        property                 | method                      | rawValue       | type           | append | listItemType | initialValue        || expectedValue
        "applicationAccessPaths" | toProviderSet(property)     | ["/some/path"] | "List<String>" | false  | "String"     | ['/bin/something1'] || rawValue
        "applicationAccessPaths" | toSetter(property)          | ["/some/path"] | "List<String>" | false  | "String"     | ['/bin/something2'] || rawValue
        "applicationAccessPaths" | "${property}.addAll"        | ["/some/path"] | "List<String>" | true   | "String"     | ['/bin/something3'] || initialValue + rawValue
        "applicationAccessPaths" | "applicationAccessPath"     | "/some/path"   | "String"       | true   | "String"     | ['/bin/something4'] || initialValue + [rawValue]

        "signingIdentities"      | toProviderSet(property)     | ["/some/path"] | "List<String>" | false  | "String"     | ['ID1']             || rawValue
        "signingIdentities"      | toSetter(property)          | ["/some/path"] | "List<String>" | false  | "String"     | ['ID2']             || rawValue
        "signingIdentities"      | "${property}.addAll"        | ["/some/path"] | "List<String>" | true   | "String"     | ['ID3']             || initialValue + rawValue
        "signingIdentities"      | "signingIdentity"           | "/some/path"   | "String"       | true   | "String"     | ['ID4']             || initialValue + [rawValue]
        "signingIdentities"      | toSetter("signingIdentity") | "/some/path"   | "String"       | false  | "String"     | ['ID5']             || [rawValue]

        setType = (append) ? 'append' : 'replace'
        baseValue = wrapValueBasedOnType(initialValue, "List<${listItemType}>")
        baseSetter = "${property} = ${baseValue}"
        value = wrapValueBasedOnType(rawValue, type, wrapValueFallback)
    }

    @Unroll
    def "#expectedBehavior task if #message"() {
        given: "a test certificate"
        def passphrase = "123456"
        def identityName = "codesign test"
        def cert = SecurityHelper.createTestCodeSigningCertificatePkcs12([commonName: identityName], passphrase)

        if (identityIsInKeychain) {
            buildKeychain.importFile(cert, [passphrase: passphrase])
        }

        buildFile << """
        ${subjectUnderTestName} {
            passphrase = "${passphrase}"
            p12 = file('${cert.path}')
            signingIdentity = "${identityName}"
            ignoreInvalidSigningIdentity = true
            applicationAccessPaths = ${appAccessPathsValue} 
        }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.wasSkipped(subjectUnderTestName) == wasSkipped

        where:
        appAccessPaths        | identityIsInKeychain | wasSkipped
        []                    | true                 | true
        []                    | false                | false
        ['/usr/bin/codesign'] | true                 | false
        ['/usr/bin/codesign'] | false                | false
        expectedBehavior = wasSkipped ? "skips" : "does not skip"
        appAccessPathsMessage = appAccessPaths.empty ? "no app access paths are configured" : "app access path are configured"
        message = identityIsInKeychain ? "identity is already in keychain and ${appAccessPathsMessage}" : "identity is not in keychain and ${appAccessPathsMessage}"
        appAccessPathsValue = wrapValueBasedOnType(appAccessPaths, "List<String>")
    }

    def "skips with NO-SOURCE when p12 file is not set"() {
        given:
        buildFile << """
        ${subjectUnderTestName} {
        }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.standardOutput.contains("Task :${subjectUnderTestName} NO-SOURCE")
    }
}
