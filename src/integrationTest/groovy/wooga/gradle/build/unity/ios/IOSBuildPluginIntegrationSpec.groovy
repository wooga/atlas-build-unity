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

import nebula.test.functional.ExecutionResult
import net.wooga.system.ProcessList
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Timeout
import spock.lang.Unroll
import wooga.gradle.build.IntegrationSpec

@Requires({ os.macOs && env['ATLAS_BUILD_UNITY_IOS_EXECUTE_KEYCHAIN_SPEC'] == 'YES' })
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

        keychainLookupList.reset()
    }

    def cleanup() {
        keychainLookupList.reset()
    }

    @Unroll("creates custom build keychain")
    def "creates custom build keychain"() {
        given: "default project"
        environmentVariables.set("ATLAS_BUILD_UNITY_IOS_RESET_KEYCHAINS", resetKeychainsEnabled ? "YES" : "NO")

        when:
        def result = runTasksSuccessfully("addKeychain")

        then:
        !result.wasUpToDate("addKeychain")
        result.wasExecuted("resetKeychains")
        result.wasSkipped("resetKeychains") != resetKeychainsEnabled
        buildKeychain.exists()
        keychainLookupList.contains(buildKeychain)

        cleanup:
        keychainLookupList.remove(buildKeychain)

        where:
        resetKeychainsEnabled << [true, false]
    }

    def "removes custom build keychain"() {
        given: "an added build keychain"
        def result = runTasksSuccessfully("addKeychain")
        assert !result.wasUpToDate("addKeychain")
        assert buildKeychain.exists()
        assert keychainLookupList.contains(buildKeychain)

        when:
        runTasksSuccessfully("removeKeychain")

        then:
        buildKeychain.exists()
        !keychainLookupList.contains(buildKeychain)

        cleanup:
        keychainLookupList.remove(buildKeychain)
    }

    @Timeout(value = 10)
    @Unroll
    def "#removes custom build keychain when shutdown with signal #signal"() {
        given: "a basic fork setup"
        fork = true
        and: "a different gradle version to recognize the daemon PID"
        gradleVersion = gradleDaemonVersion

        and: "a long running task"
        buildFile << """
            task longRunningTask {
                doLast {
                    System.sleep(5 * 1000 * 60)
                }
            }
        """.stripIndent()

        when:
        ExecutionResult result
        Thread t = new Thread({
            result = runTasks("addKeychain", "longRunningTask")
        })

        t.start()

        //wait for the process to spawn
        def pids = ProcessList.waitForProcess { it.contains("org.gradle.launcher.daemon.bootstrap.GradleDaemon ${gradleDaemonVersion}") }

        //wait for keychain to be added
        while (!keychainLookupList.contains(buildKeychain)) {
            sleep(1000)
        }

        pids.each {
            ProcessList.kill(it, signal)
        }
        t.join()

        then:
        result != null
        !result.success
        keychainLookupList.contains(buildKeychain) != removeKeychain

        cleanup:
        keychainLookupList.remove(buildKeychain)

        where:
        signal                  | removeKeychain
        ProcessList.Signal.HUP  | true
        //ProcessList.Signal.INT  | true
        ProcessList.Signal.ABRT | false
        ProcessList.Signal.KILL | false
        ProcessList.Signal.ALRM | false
        ProcessList.Signal.TERM | true
        gradleDaemonVersion = "6.8.2"
        removes = removeKeychain ? "runs shutdown hook and removes" : "keeps"
    }


    @Unroll
    def "removes custom build keychain when build #message"() {
        given: "project which will succeed/fail the assemble task"
        //skip these tasks to succeed the build
        buildFile << """
            project.xcodeArchive.onlyIf({${!success}})
            project.xcodeArchiveExport.onlyIf({${!success}})
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

    @Unroll
    def "task #taskToRun resets keychains before build when #message"() {
        given: "project which will succeed/fail the assemble task"
        //skip these tasks to succeed the build
        buildFile << """
            project.xcodeArchive.onlyIf({false})
            project.xcodeArchiveExport.onlyIf({false})
            project.importProvisioningProfiles.onlyIf({false})
        """.stripIndent()

        and:
        environmentVariables.set("ATLAS_BUILD_UNITY_IOS_RESET_KEYCHAINS", resetEnabled ? "YES" : "NO")

        when:
        def result = runTasks("assemble")

        then:
        result.wasExecuted("resetKeychains")
        result.wasSkipped("resetKeychains") != resetEnabled

        where:
        taskToRun     | resetEnabled
        "assemble"    | true
        "assemble"    | false
        "addKeychain" | true
        "addKeychain" | false
        message = (resetEnabled) ? "reset is enabled" : "reset is disabled"
    }
}
