/*
 * Copyright 2021 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wooga.security.command

import spock.lang.Requires
import spock.lang.Unroll
import static com.wooga.security.SecurityHelper.*

@Requires({ os.macOs })
class ImportSpec extends SecurityCommandSpec<Import> {

    Import command

    def setup() {
        command = new Import(File.createTempFile("some", "input"), testKeychainLocation)
    }

    def "imports p12 certificate and keys"() {
        given: "settings configuration"
        command.withKeychain(testKeychainLocation)
                .withInputFile(cert)
                .withPassphrase(passphrase)

        when:
        command.execute()

        then:
        noExceptionThrown()
        keychainHasCertificateWithName(testKeychainLocation, certificateName)
        keychainHasCertificateWithEmail(testKeychainLocation, emailAddress)
        keychainHasPrivateKey(testKeychainLocation, privateKeyName)
        !keychainHasPrivateKey(testKeychainLocation, "some other name")

        where:
        passphrase = "12345"
        privateKeyName = "CustomKey"
        certificateName = "ImportCI"
        emailAddress = "jenkins-test@wooga.net"
        cert = createTestCertificatePkcs12(
                commonName: certificateName,
                privateKeyName: privateKeyName,
                emailAddress: emailAddress,
                passphrase
        )
    }

    def "imports keys in pem format"() {
        given: "settings configuration"
        command.withKeychain(testKeychainLocation)
                .withInputFile(key)
        assert !keychainHasPrivateKey(testKeychainLocation, "Imported Private Key")

        when:
        command.execute()

        then:
        noExceptionThrown()
        keychainHasPrivateKey(testKeychainLocation, "Imported Private Key")
        !keychainHasPrivateKey(testKeychainLocation, "Some random key")

        where:
        key = createPrivateKey()
    }

    @Unroll("fails with #expectedExeption when #reason")
    def "fails with exception when property is invalid"() {
        given: "invalid keychain"
        command.withInputFile(File.createTempFile("some", "input"))
        command.setProperty(property, value)

        when:
        command.execute()

        then:
        def e = thrown(expectedExeption)
        e.message.matches(messagePattern)

        where:
        property    | value                  | expectedExeption               | message
        "keychain"  | null                   | NullPointerException.class     | "is null"
        "keychain"  | new File("/some/file") | IllegalArgumentException.class | "does not exist"
        "keychain"  | File.createTempDir()   | IllegalArgumentException.class | "is not a file"

        "inputFile" | null                   | NullPointerException.class     | "is null"
        "inputFile" | new File("/some/file") | IllegalArgumentException.class | "does not exist"
        "inputFile" | File.createTempDir()   | IllegalArgumentException.class | "is not a file"

        reason = "provided ${property} ${message}"
        messagePattern = /provided ${property}.*${message}/
    }

    @Unroll("property #property sets commandline flag #commandlineFlag")
    def "property sets commandline base"() {
        given: "set property"
        command.invokeMethod(method, value)

        when:
        def arguments = command.getArguments().join(" ")
        then:
        arguments.contains(expectedArguments)

        where:
        property                | commandlineFlag | value
        "type"                  | "-t"            | Import.Type.Priv
        "passphrase"            | "-P"            | "somePhrase"
        "format"                | "-f"            | Import.Format.Openssh1
        "allowGlobalAccess"     | "-A"            | true
        "keysAreWrapped"        | "-w"            | true
        "keysAreNonExtractable" | "-x"            | true
        expectedArguments = Boolean.isInstance(value) ? "${commandlineFlag}" : "${commandlineFlag} ${value.toString()}"
        method = Boolean.isInstance(value) ? property : "with${property.capitalize()}"
    }

    def "method allowAccessFrom sets commandline flag -T"() {
        given:
        command.allowAccessFrom("/some/path")
        command.allowAccessFrom("/some/other/path")

        when:
        def arguments = command.getArguments().join(" ")

        then:
        arguments.contains("-T /some/path")
        arguments.contains("-T /some/other/path")
    }

    def "method withAttribute sets commandline flag -a"() {
        given:
        command.withAttribute("foo", "bar")
        command.withAttribute("bar", "foo")

        when:
        def arguments = command.getArguments().join(" ")

        then:
        arguments.contains("-a foo bar")
        arguments.contains("-a bar foo")
    }
}
