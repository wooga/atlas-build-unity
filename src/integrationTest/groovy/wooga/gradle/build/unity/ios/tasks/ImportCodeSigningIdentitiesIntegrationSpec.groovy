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
            //inputKeychain = file('${buildKeychain.location}') 
            keychain = file('${buildKeychain.location}')
        }
        """.stripIndent()
    }

    @Unroll("import #taskStatus when #reason")
    def "imports signing identity"() {
        given: "an invalid sighing certificate"
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
        testSigningIdentity                           | signingIdentity            | ignoreInvalidSigningIdentities || taskSuccessfull | reason                                                                       | logMessage
        "test signing: Wooga GmbH"                    | _                          | false                          || false           | "imported identity is invalid"                                               | "Unable to find valid code sign identity '#expectedSigningIdentity' in keychain"
        "test signing: Wooga GmbH"                    | _                          | true                           || true            | "imported identity is invalid but error ignored"                             | "Signing Identity '#expectedSigningIdentity' found but invalid"
        "test signing: Wooga GmbH"                    | "some signing: Foo GmbH"   | true                           || false           | "expected identity was not imported"                                         | "Unable to find code sign identity '#expectedSigningIdentity' in keychain"
        "Mac App Distribution: Wooga GmbH"            | "Mac App Distribution"     | true                           || true            | "expected identity was imported with automatic selector: ${signingIdentity}" | "Signing Identity '#expectedSigningIdentity' found but invalid"
        "iOS Developer: Markus Mustermann"            | "iOS Developer"            | true                           || true            | "expected identity was imported with automatic selector: ${signingIdentity}" | "Signing Identity '#expectedSigningIdentity' found but invalid"
        "iOS Distribution: Wooga GmbH"                | "iOS Distribution"         | true                           || true            | "expected identity was imported with automatic selector: ${signingIdentity}" | "Signing Identity '#expectedSigningIdentity' found but invalid"
        "iPhone Developer: Markus Mustermann"         | "iPhone Developer"         | true                           || true            | "expected identity was imported with automatic selector: ${signingIdentity}" | "Signing Identity '#expectedSigningIdentity' found but invalid"
        "iPhone Distribution: Wooga GmbH"             | "iPhone Distribution"      | true                           || true            | "expected identity was imported with automatic selector: ${signingIdentity}" | "Signing Identity '#expectedSigningIdentity' found but invalid"
        "Developer ID Application: Markus Mustermann" | "Developer ID Application" | true                           || true            | "expected identity was imported with automatic selector: ${signingIdentity}" | "Signing Identity '#expectedSigningIdentity' found but invalid"
        "Apple Distribution: Wooga GmbH"              | "Apple Distribution"       | true                           || true            | "expected identity was imported with automatic selector: ${signingIdentity}" | "Signing Identity '#expectedSigningIdentity' found but invalid"
        "Mac Developer: Markus Mustermann"            | "Mac Developer"            | true                           || true            | "expected identity was imported with automatic selector: ${signingIdentity}" | "Signing Identity '#expectedSigningIdentity' found but invalid"
        "Apple Development: Wooga GmbH"               | "Apple Development"        | true                           || true            | "expected identity was imported with automatic selector: ${signingIdentity}" | "Signing Identity '#expectedSigningIdentity' found but invalid"
        "test signing: Wooga GmbH"                    | "SHA-1"                    | true                           || true            | "expected identity was imported with SHA-1 fingerprint"                      | "Signing Identity '#expectedSigningIdentity' found but invalid"

        passphrase = "123456"
        testCertificate = SecurityHelper.createTestCodeSigningCertificatePkcs12([commonName: testSigningIdentity], passphrase)
        cert = testCertificate.pkcs12
        expectedSigningIdentity = signingIdentity == _ ? testSigningIdentity : signingIdentity.replaceAll("SHA-1", testCertificate.certFingerprint)
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

    def "is up-to-date when #reason"() {
        given: "an invalid sighing certificate"
        def passphrase = "123456"
        def cert = SecurityHelper.createTestCodeSigningCertificatePkcs12([commonName: signingIdentity], passphrase).pkcs12
        def futureKeychain = new File(projectDir, "build/keychains/test.keychain")
        assert !futureKeychain.exists()

        buildFile << """
        ${subjectUnderTestName} {
            passphrase = "${passphrase}"
            p12 = file('${cert.path}')
            signingIdentity = "${signingIdentity}"
            ignoreInvalidSigningIdentity = true
            inputKeychain = file('${buildKeychain.location}') 
            keychain = file('${futureKeychain.path}')
            password = "123456" 
        }
        """.stripIndent()

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        result.success
        !result.wasSkipped(subjectUnderTestName)
        futureKeychain.exists()

        when:
        result = runTasks(subjectUnderTestName)

        then:
        result.success
        result.wasUpToDate(subjectUnderTestName)

        when:
        futureKeychain.delete()
        result = runTasks(subjectUnderTestName)

        then:
        result.success
        !result.wasUpToDate(subjectUnderTestName)

        where:
        signingIdentity            | _
        "test signing: Wooga GmbH" | _
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
