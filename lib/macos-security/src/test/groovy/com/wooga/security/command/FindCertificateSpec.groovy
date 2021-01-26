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
class FindCertificateSpec extends SecurityCommandSpec<FindCertificate> {
    FindCertificate command = new FindCertificate()

    def "finds certificate in keychain"() {
        given: "a certificate imported in the keychain"
        importPK12(p12, passphrase, testKeychainLocation)

        and: "a configured command"
        command.withKeychain(testKeychainLocation)
                .withName(certificateName)
                .printPem()
                .printEmail()
        when:
        def result = command.execute()

        then:
        noExceptionThrown()
        result != null
        !result.empty
        result.contains(emailAddress)
        result.contains(cert.text)

        where:
        passphrase = "12345"
        privateKeyName = "CustomKey"
        certificateName = "ImportCI"
        emailAddress = "jenkins-test@wooga.net"
        key = createPrivateKey()
        cert = createSelfSignedCertificate(
                commonName: certificateName,
                emailAddress: emailAddress, key, passphrase)
        p12 = createTestCertificatePkcs12(
                privateKeyName: privateKeyName,
                key, cert, passphrase)
    }

    @Unroll("fails with #expectedExeption when #reason")
    def "fails with exception when property is invalid"() {
        given: "invalid keychain"
        command.setProperty(property, value)

        when:
        command.execute()

        then:
        def e = thrown(expectedExeption)
        e.message.matches(messagePattern)

        where:
        property    | value                    | expectedExeption               | message
        "keychains" | [new File("/some/file")] | IllegalArgumentException.class | "does not exist"
        "keychains" | [File.createTempDir()]   | IllegalArgumentException.class | "is not a file"

        reason = "provided ${property} ${message}"
        messagePattern = /provided keychain.*${message}/
    }

    @Unroll("property #property sets commandline flag #commandlineFlag")
    def "property sets commandline"() {
        given: "set property"
        command.invokeMethod(method, value)

        when:
        def arguments = command.getArguments().join(" ")
        then:
        arguments.contains(expectedArguments)

        where:
        property      | commandlineFlag | value
        "name"        | "-c"            | "someName"
        "email"       | "-e"            | "someEmail"
        "allMatching" | "-a"            | true
        "printHash"   | "-Z"            | true
        "printEmail"  | "-m"            | true
        "printPem"    | "-p"            | true
        expectedArguments = Boolean.isInstance(value) ? "${commandlineFlag}" : "${commandlineFlag} ${value.toString()}"
        method = Boolean.isInstance(value) ? property : "with${property.capitalize()}"
    }
}
