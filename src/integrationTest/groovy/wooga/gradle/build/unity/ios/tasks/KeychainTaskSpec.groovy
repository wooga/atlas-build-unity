/*
 * Copyright 2018 Wooga GmbH
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
 *
 */

package wooga.gradle.build.unity.ios.tasks

import spock.lang.Requires
import spock.lang.Shared
import wooga.gradle.build.IntegrationSpec
import wooga.gradle.build.unity.ios.IOSBuildPlugin
import wooga.gradle.build.unity.ios.KeychainLookupList

import wooga.gradle.build.unity.ios.internal.utils.SecurityUtil

@Requires({ os.macOs })
class KeychainTaskSpec extends IntegrationSpec {
    @Shared
    File xcProject

    @Shared
    File xcProjectConfig

    @Shared
    File buildKeychain

    @Shared
    KeychainLookupList keychainLookupList = new KeychainLookupList()
    static String certPassword = "test password"

    //TODO: remove duplicate code
    def createTestCertificate(File cert, String password) {
        def certInfo = new File(projectDir, "certInfo")
        certInfo << """
        DE
        Germany
        Berlin
        Wooga GmbH
        Gradle tests
        Test CA certificate
        jenkins@wooga.net
        .
        .
        """.stripIndent().trim()

        def createScript = new File(projectDir, "certCreate.sh")
        createScript << """
        <${certInfo.path} openssl req -new -x509 -outform PEM -newkey rsa:2048 -nodes -keyout /tmp/ca.key -keyform PEM -out /tmp/ca.crt -days 365
        echo "${password}" | openssl pkcs12 -export -in /tmp/ca.crt -inkey /tmp/ca.key -out ${cert.path} -name \"Test CA\" -passout stdin
        """.stripIndent()

        new ProcessBuilder("sh", createScript.path).start().waitFor()
        createScript.delete()
        certInfo.delete()
    }

    def setup() {
        buildFile << """
            ${applyPlugin(IOSBuildPlugin)}

            iosBuild {
                certificatePassphrase = "$certPassword"
                keychainPassword = "$certPassword"
            }
        """.stripIndent()

        xcProject = new File(projectDir, "test.xcodeproj")
        xcProject.mkdirs()
        xcProjectConfig = new File(xcProject, "project.pbxproj")
        xcProjectConfig << ""

        buildKeychain = new File(projectDir, 'build/sign/keychains/build.keychain')

        createTestCertificate(new File(projectDir, "test_ca.p12"), certPassword)

    }

    def "buildKeychain caches task outputs"() {
        given: "a gradle run with buildKeychain"
        runTasksSuccessfully('buildKeychain')

        when:
        def result = runTasksSuccessfully('buildKeychain')

        then:
        result.wasUpToDate('buildKeychain')
    }

    def "fails with security stderr printed to error log"() {
        given: "wrong certificatePassphrase"
        buildFile << """
            iosBuild.certificatePassphrase = "randomPassphrase"
        """.stripIndent()

        when:
        def result = runTasksWithFailure('buildKeychain')

        then:
        outputContains(result, "security: SecKeychainItemImport: MAC verification failed during PKCS12 import (wrong password?)")

    }
}
