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

package wooga.gradle.build.unity.ios

import org.junit.ClassRule
import org.junit.contrib.java.lang.system.ProvideSystemProperty
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.build.IntegrationSpec
import wooga.gradle.build.unity.ios.internal.utils.SecurityUtil

@Requires({ os.macOs })
class IOSBuildPluginIntegrationSpec extends IntegrationSpec {

    @Shared
    File xcProject

    @Shared
    File xcProjectConfig

    @Shared
    File buildKeychain

    @Shared
    KeychainLookupList keychainLookupList = new KeychainLookupList()


    static String certPassword = "test password"

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
        SecurityUtil.getKeychainConfigFile().parentFile.mkdirs()
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

    def "creates custom build keychain"() {
        given: "default project"

        when:
        def result = runTasksSuccessfully("addKeychain")

        then:
        !result.wasUpToDate("addKeychain")
        buildKeychain.exists()
        keychainLookupList.contains(buildKeychain)

        cleanup:
        keychainLookupList.remove(buildKeychain)
    }

    def "removes custom build keychain"() {
        given: "an added build keychain"
        def result = runTasksSuccessfully("addKeychain")
        assert !result.wasUpToDate("addKeychain")
        assert buildKeychain.exists()
        assert SecurityUtil.keychainIsAdded(buildKeychain)

        when:
        runTasksSuccessfully("removeKeychain")

        then:
        buildKeychain.exists()
        !keychainLookupList.contains(buildKeychain)

        cleanup:
        keychainLookupList.remove(buildKeychain)
    }

    @Unroll
    def "removes custom build keychain when build #message"() {
        given: "project which will succeed/fail the assemble task"
        //skip these tasks to succeed the build
        buildFile << """
            project.xcodeArchive.onlyIf({${!success}})
            project.xcodeExport.onlyIf({${!success}})
            project.importProvisioningProfiles.onlyIf({${!success}})
        """.stripIndent()

        when:
        def result = runTasks("assemble")

        then:
        result.success == success
        result.wasExecuted("addKeychain")
        result.wasExecuted("removeKeychain")
        buildKeychain.exists()
        !keychainLookupList.contains(buildKeychain)

        cleanup:
        keychainLookupList.remove(buildKeychain)

        where:
        message    || success
        "fails"    || false
        "succeeds" || true
    }
}
