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

import com.wooga.gradle.PlatformUtils
import com.wooga.gradle.test.ConventionSource
import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.PropertyQueryTaskWriter
import com.wooga.gradle.test.queries.TestValue
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter
import com.wooga.security.Domain
import com.wooga.security.MacOsKeychainSearchList
import nebula.test.functional.ExecutionResult
import net.wooga.system.ProcessList
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Timeout
import spock.lang.Unroll
import wooga.gradle.fastlane.tasks.PilotUpload
import wooga.gradle.fastlane.tasks.SighRenew
import wooga.gradle.macOS.security.tasks.SecurityCreateKeychain
import wooga.gradle.xcodebuild.config.ExportOptions
import wooga.gradle.xcodebuild.tasks.ExportArchive
import wooga.gradle.xcodebuild.tasks.XcodeArchive

import static com.wooga.gradle.PlatformUtils.escapedPath
import static com.wooga.gradle.test.queries.TestValue.projectFile
import static com.wooga.gradle.test.writers.PropertySetInvocation.*
import static wooga.gradle.xcodebuild.config.ExportOptions.DistributionManifest.distributionManifest

@Requires({ os.macOs })
class IOSBuildPluginIntegrationSpec extends IOSBuildIntegrationSpec {

    static final String extensionName = IOSBuildPlugin.EXTENSION_NAME
    String subjectUnderTestName = "buildIosPluginTask"
    String subjectUnderTestTypeName = ""

    @Shared
    File xcProject

    @Shared
    File xcProjectConfig

    @Shared
    File buildKeychain

    @Shared
    MacOsKeychainSearchList keychainLookupList = new MacOsKeychainSearchList(Domain.user)

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

    @Requires({ os.macOs && env['ATLAS_BUILD_UNITY_IOS_EXECUTE_KEYCHAIN_SPEC'] == 'YES' })
    @Unroll("creates custom build keychain")
    def "creates custom build keychain"() {
        given: "default project"
        environmentVariables.set("ATLAS_BUILD_UNITY_IOS_RESET_KEYCHAINS", resetKeychainsEnabled ? "YES" : "NO")
        setupTestKeychainProperties()

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

    @Requires({ os.macOs })
    def "removes custom build keychain"() {
        given: "an added build keychain"
        setupTestKeychainProperties()

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

    @Requires({ os.macOs && env['ATLAS_BUILD_UNITY_IOS_EXECUTE_KEYCHAIN_SPEC'] == 'YES' })
    @Timeout(value = 10)
    @Unroll
    def "#removes custom build keychain when shutdown with signal #signal"() {
        given: "a basic fork setup"
        fork = true
        and: "a different gradle version to recognize the daemon PID"
        gradleVersion = gradleDaemonVersion

        setupTestKeychainProperties()

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
        // ProcessList.Signal.INT  | true
        ProcessList.Signal.ABRT | false
        ProcessList.Signal.KILL | false
        ProcessList.Signal.ALRM | false
        ProcessList.Signal.TERM | true
        gradleDaemonVersion = "6.8.2"
        removes = removeKeychain ? "runs shutdown hook and removes" : "keeps"
    }

    @Requires({ os.macOs })
    @Unroll
    def "removes custom build keychain when build #message"() {
        given: "project which will succeed/fail the assemble task"
        //skip these tasks to succeed the build
        buildFile << """
            project.xcodeArchive.onlyIf({${!success}})
            project.xcodeArchiveExport.onlyIf({${!success}})
            project.importProvisioningProfiles.onlyIf({${!success}})
        """.stripIndent()

        setupTestKeychainProperties()

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

    @Requires({ os.macOs && env['ATLAS_BUILD_UNITY_IOS_EXECUTE_KEYCHAIN_SPEC'] == 'YES' })
    @Unroll
    def "task #taskToRun resets keychains before build when #message"() {
        given: "project which will succeed/fail the assemble task"
        //skip these tasks to succeed the build
        setupTestKeychainProperties()

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

    def setupTestKeychainProperties() {
        buildFile << """
        iosBuild {
                codeSigningIdentityFilePassphrase = "$certPassword"
                keychainPassword = "$certPassword"
        } 
        """.stripIndent()
    }

    @Requires({ os.macOs })
    @Unroll
    def "sets convention for task type #taskType.simpleName and property #property"() {
        given: "write convention source assignment"
        if (conventionSource != _) {
            conventionSource.write(buildFile, value.toString())
        }

        and: "a task to query property from"

        buildFile << """
        class ${taskType.simpleName}Impl extends ${taskType.name} {
        }
        
        task ${subjectUnderTestName}(type: ${taskType.simpleName}Impl)
        """.stripIndent()

        and: "the test value with replace placeholders"
        if (testValue instanceof String) {
            testValue = testValue.replaceAll("#projectDir#", escapedPath(projectDir.path))
            testValue = testValue.replaceAll("#taskName#", subjectUnderTestName)
        }

        when:
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}", invocation.toString())
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, testValue)

        where:
        taskType               | property             | rawValue                            | type          | conventionSource                                              | inv | expectedValue
        XcodeArchive           | "clean"              | false                               | "Boolean"     | _                                                             | _   | _
        XcodeArchive           | "scheme"             | "test.scheme"                       | "String"      | ConventionSource.extension(extensionName, property)           | _   | _
        XcodeArchive           | "configuration"      | "test.config"                       | "String"      | ConventionSource.extension(extensionName, property)           | _   | _
        XcodeArchive           | "teamId"             | "test.teamId"                       | "String"      | ConventionSource.extension(extensionName, property)           | _   | _

        ExportArchive          | "exportOptionsPlist" | "some_exportOptions.plist"          | "RegularFile" | ConventionSource.extension(extensionName, property)           | _   | "#projectDir#/${rawValue}".toString()

        SecurityCreateKeychain | "baseName"           | "build"                             | "String"      | _                                                             | _   | _
        SecurityCreateKeychain | "extension"          | "keychain"                          | "String"      | _                                                             | _   | _
        SecurityCreateKeychain | "password"           | "some_password"                     | "String"      | ConventionSource.extension(extensionName, 'keychainPassword') | _   | _
        SecurityCreateKeychain | "destinationDir"     | "#projectDir#/build/sign/keychains" | "File"        | _                                                             | _   | _

        SighRenew              | "username"           | "user1"                             | "String"      | ConventionSource.extension("fastlane", property)              | _   | _
        PilotUpload            | "username"           | "user1"                             | "String"      | ConventionSource.extension("fastlane", property)              | _   | _
        SighRenew              | "password"           | "user2"                             | "String"      | ConventionSource.extension("fastlane", property)              | _   | _
        PilotUpload            | "password"           | "user2"                             | "String"      | ConventionSource.extension("fastlane", property)              | _   | _

        SighRenew              | "teamId"             | "test.teamId"                       | "String"      | ConventionSource.extension(extensionName, property)           | _   | _
        SighRenew              | "appIdentifier"      | "test.appIdentifier"                | "String"      | ConventionSource.extension(extensionName, property)           | _   | _
        SighRenew              | "destinationDir"     | "#projectDir#/build/tmp/#taskName#" | "Directory"   | _                                                             | _   | _
        SighRenew              | "provisioningName"   | "provisioningNameValue"             | "String"      | ConventionSource.extension(extensionName, property)           | _   | _
        SighRenew              | "adhoc"              | true                                | "Boolean"     | ConventionSource.extension(extensionName, property)           | _   | _
        SighRenew              | "fileName"           | "signing.mobileprovision"           | "String"      | _                                                             | _   | _

        PilotUpload            | "devPortalTeamId"    | "test.teamId"                       | "String"      | ConventionSource.extension(extensionName, "teamId")           | _   | _
        PilotUpload            | "appIdentifier"      | "test.appIdentifier"                | "String"      | ConventionSource.extension(extensionName, property)           | _   | _

        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), wrapValueFallback) : rawValue
        testValue = (expectedValue == _) ? rawValue : expectedValue
        invocation = (inv == _) ? ".getOrNull()" : inv
    }

    @Unroll()
    def "extension property #property of type #type sets #rawValue when #location"() {
        expect:
        runPropertyQuery(get, set).matches(rawValue)

        where:
        property                            | invocation                         | rawValue                                                                                   | type                     | location
        "keychainPassword"                  | _                                  | "testPassword1"                                                                            | _                        | PropertyLocation.environment
        "keychainPassword"                  | _                                  | "testPassword2"                                                                            | _                        | PropertyLocation.property
        "keychainPassword"                  | assignment                         | "testPassword3"                                                                            | "String"                 | PropertyLocation.script
        "keychainPassword"                  | assignment                         | "testPassword4"                                                                            | "Provider<String>"       | PropertyLocation.script
        "keychainPassword"                  | providerSet                        | "testPassword5"                                                                            | "String"                 | PropertyLocation.script
        "keychainPassword"                  | providerSet                        | "testPassword6"                                                                            | "Provider<String>"       | PropertyLocation.script
        "keychainPassword"                  | setter                             | "testPassword7"                                                                            | "String"                 | PropertyLocation.script
        "keychainPassword"                  | setter                             | "testPassword8"                                                                            | "Provider<String>"       | PropertyLocation.script
        "keychainPassword"                  | _                                  | null                                                                                       | _                        | PropertyLocation.none

        "signingIdentities"                 | _                                  | TestValue.set("ID1,ID2").expect(["ID1", "ID2"])                                            | _                        | PropertyLocation.environment
        "signingIdentities"                 | _                                  | TestValue.set("ID1,ID2").expect(["ID1", "ID2"])                                            | _                        | PropertyLocation.property
        "signingIdentities"                 | assignment                         | ["ID1", "ID2"]                                                                             | "List<String>"           | PropertyLocation.script
        "signingIdentities"                 | assignment                         | ["ID1", "ID2"]                                                                             | "Provider<List<String>>" | PropertyLocation.script
        "signingIdentities"                 | providerSet                        | ["ID1", "ID2"]                                                                             | "List<String>"           | PropertyLocation.script
        "signingIdentities"                 | providerSet                        | ["ID1", "ID2"]                                                                             | "Provider<List<String>>" | PropertyLocation.script
        "signingIdentities"                 | setter                             | ["ID1", "ID2"]                                                                             | "List<String>"           | PropertyLocation.script
        "signingIdentities"                 | setter                             | ["ID1", "ID2"]                                                                             | "Provider<List<String>>" | PropertyLocation.script
        "signingIdentities"                 | customSetter("setSigningIdentity") | TestValue.set(wrapValueBasedOnType("code sign: ID3", "String")).expect(["code sign: ID3"]) | "Provider<String>>"      | PropertyLocation.script
        "signingIdentities"                 | customSetter("setSigningIdentity") | TestValue.set(wrapValueBasedOnType("code sign: ID3", "String")).expect(["code sign: ID3"]) | "String>"                | PropertyLocation.script
        "signingIdentities"                 | _                                  | []                                                                                         | _                        | PropertyLocation.none

        "codeSigningIdentityFile"           | _                                  | osPath("/path/to/p12")                                                                     | _                        | PropertyLocation.environment
        "codeSigningIdentityFile"           | _                                  | osPath("/path/to/p12")                                                                     | _                        | PropertyLocation.property
        "codeSigningIdentityFile"           | assignment                         | osPath("/path/to/p12")                                                                     | "File"                   | PropertyLocation.script
        "codeSigningIdentityFile"           | assignment                         | osPath("/path/to/p12")                                                                     | "Provider<RegularFile>"  | PropertyLocation.script
        "codeSigningIdentityFile"           | providerSet                        | osPath("/path/to/p12")                                                                     | "File"                   | PropertyLocation.script
        "codeSigningIdentityFile"           | providerSet                        | osPath("/path/to/p12")                                                                     | "Provider<RegularFile>"  | PropertyLocation.script
        "codeSigningIdentityFile"           | setter                             | osPath("/path/to/p12")                                                                     | "File"                   | PropertyLocation.script
        "codeSigningIdentityFile"           | setter                             | osPath("/path/to/p12")                                                                     | "Provider<RegularFile>"  | PropertyLocation.script
        "codeSigningIdentityFile"           | _                                  | null                                                                                       | _                        | PropertyLocation.none

        "codeSigningIdentityFilePassphrase" | _                                  | "testPassphrase1"                                                                          | _                        | PropertyLocation.environment
        "codeSigningIdentityFilePassphrase" | _                                  | "testPassphrase2"                                                                          | _                        | PropertyLocation.property
        "codeSigningIdentityFilePassphrase" | assignment                         | "testPassphrase3"                                                                          | "String"                 | PropertyLocation.script
        "codeSigningIdentityFilePassphrase" | assignment                         | "testPassphrase4"                                                                          | "Provider<String>"       | PropertyLocation.script
        "codeSigningIdentityFilePassphrase" | providerSet                        | "testPassphrase5"                                                                          | "String"                 | PropertyLocation.script
        "codeSigningIdentityFilePassphrase" | providerSet                        | "testPassphrase6"                                                                          | "Provider<String>"       | PropertyLocation.script
        "codeSigningIdentityFilePassphrase" | setter                             | "testPassphrase7"                                                                          | "String"                 | PropertyLocation.script
        "codeSigningIdentityFilePassphrase" | setter                             | "testPassphrase8"                                                                          | "Provider<String>"       | PropertyLocation.script
        "codeSigningIdentityFilePassphrase" | _                                  | null                                                                                       | _                        | PropertyLocation.none

        "appIdentifier"                     | _                                  | "com.test.app.1"                                                                           | _                        | PropertyLocation.environment
        "appIdentifier"                     | _                                  | "com.test.app.2"                                                                           | _                        | PropertyLocation.property
        "appIdentifier"                     | assignment                         | "com.test.app.3"                                                                           | "String"                 | PropertyLocation.script
        "appIdentifier"                     | assignment                         | "com.test.app.4"                                                                           | "Provider<String>"       | PropertyLocation.script
        "appIdentifier"                     | providerSet                        | "com.test.app.5"                                                                           | "String"                 | PropertyLocation.script
        "appIdentifier"                     | providerSet                        | "com.test.app.6"                                                                           | "Provider<String>"       | PropertyLocation.script
        "appIdentifier"                     | setter                             | "com.test.app.7"                                                                           | "String"                 | PropertyLocation.script
        "appIdentifier"                     | setter                             | "com.test.app.8"                                                                           | "Provider<String>"       | PropertyLocation.script
        "appIdentifier"                     | _                                  | null                                                                                       | _                        | PropertyLocation.none

        "teamId"                            | _                                  | "team1"                                                                                    | _                        | PropertyLocation.environment
        "teamId"                            | _                                  | "team2"                                                                                    | _                        | PropertyLocation.property
        "teamId"                            | assignment                         | "team3"                                                                                    | "String"                 | PropertyLocation.script
        "teamId"                            | assignment                         | "team4"                                                                                    | "Provider<String>"       | PropertyLocation.script
        "teamId"                            | providerSet                        | "team5"                                                                                    | "String"                 | PropertyLocation.script
        "teamId"                            | providerSet                        | "team6"                                                                                    | "Provider<String>"       | PropertyLocation.script
        "teamId"                            | setter                             | "team7"                                                                                    | "String"                 | PropertyLocation.script
        "teamId"                            | setter                             | "team8"                                                                                    | "Provider<String>"       | PropertyLocation.script
        "teamId"                            | _                                  | null                                                                                       | _                        | PropertyLocation.none

        "scheme"                            | _                                  | "value1"                                                                                   | _                        | PropertyLocation.environment
        "scheme"                            | _                                  | "value2"                                                                                   | _                        | PropertyLocation.property
        "scheme"                            | assignment                         | "value3"                                                                                   | "String"                 | PropertyLocation.script
        "scheme"                            | assignment                         | "value4"                                                                                   | "Provider<String>"       | PropertyLocation.script
        "scheme"                            | providerSet                        | "value5"                                                                                   | "String"                 | PropertyLocation.script
        "scheme"                            | providerSet                        | "value6"                                                                                   | "Provider<String>"       | PropertyLocation.script
        "scheme"                            | setter                             | "value7"                                                                                   | "String"                 | PropertyLocation.script
        "scheme"                            | setter                             | "value8"                                                                                   | "Provider<String>"       | PropertyLocation.script
        "scheme"                            | _                                  | null                                                                                       | _                        | PropertyLocation.none

        "configuration"                     | _                                  | "value1"                                                                                   | _                        | PropertyLocation.environment
        "configuration"                     | _                                  | "value2"                                                                                   | _                        | PropertyLocation.property
        "configuration"                     | assignment                         | "value3"                                                                                   | "String"                 | PropertyLocation.script
        "configuration"                     | assignment                         | "value4"                                                                                   | "Provider<String>"       | PropertyLocation.script
        "configuration"                     | providerSet                        | "value5"                                                                                   | "String"                 | PropertyLocation.script
        "configuration"                     | providerSet                        | "value6"                                                                                   | "Provider<String>"       | PropertyLocation.script
        "configuration"                     | setter                             | "value7"                                                                                   | "String"                 | PropertyLocation.script
        "configuration"                     | setter                             | "value8"                                                                                   | "Provider<String>"       | PropertyLocation.script
        "configuration"                     | _                                  | null                                                                                       | _                        | PropertyLocation.none

        "provisioningName"                  | _                                  | "value1"                                                                                   | _                        | PropertyLocation.environment
        "provisioningName"                  | _                                  | "value2"                                                                                   | _                        | PropertyLocation.property
        "provisioningName"                  | assignment                         | "value3"                                                                                   | "String"                 | PropertyLocation.script
        "provisioningName"                  | assignment                         | "value4"                                                                                   | "Provider<String>"       | PropertyLocation.script
        "provisioningName"                  | providerSet                        | "value5"                                                                                   | "String"                 | PropertyLocation.script
        "provisioningName"                  | providerSet                        | "value6"                                                                                   | "Provider<String>"       | PropertyLocation.script
        "provisioningName"                  | setter                             | "value7"                                                                                   | "String"                 | PropertyLocation.script
        "provisioningName"                  | setter                             | "value8"                                                                                   | "Provider<String>"       | PropertyLocation.script
        "provisioningName"                  | _                                  | null                                                                                       | _                        | PropertyLocation.none

        "adhoc"                             | _                                  | true                                                                                       | _                        | PropertyLocation.environment
        "adhoc"                             | _                                  | true                                                                                       | _                        | PropertyLocation.property
        "adhoc"                             | assignment                         | true                                                                                       | "Boolean"                | PropertyLocation.script
        "adhoc"                             | assignment                         | true                                                                                       | "Provider<Boolean>"      | PropertyLocation.script
        "adhoc"                             | providerSet                        | true                                                                                       | "Boolean"                | PropertyLocation.script
        "adhoc"                             | providerSet                        | true                                                                                       | "Provider<Boolean>"      | PropertyLocation.script
        "adhoc"                             | setter                             | true                                                                                       | "Boolean"                | PropertyLocation.script
        "adhoc"                             | setter                             | true                                                                                       | "Provider<Boolean>"      | PropertyLocation.script
        "adhoc"                             | _                                  | false                                                                                      | _                        | PropertyLocation.none

        "publishToTestFlight"               | _                                  | true                                                                                       | _                        | PropertyLocation.environment
        "publishToTestFlight"               | _                                  | true                                                                                       | _                        | PropertyLocation.property
        "publishToTestFlight"               | assignment                         | true                                                                                       | "Boolean"                | PropertyLocation.script
        "publishToTestFlight"               | assignment                         | true                                                                                       | "Provider<Boolean>"      | PropertyLocation.script
        "publishToTestFlight"               | providerSet                        | true                                                                                       | "Boolean"                | PropertyLocation.script
        "publishToTestFlight"               | providerSet                        | true                                                                                       | "Provider<Boolean>"      | PropertyLocation.script
        "publishToTestFlight"               | setter                             | true                                                                                       | "Boolean"                | PropertyLocation.script
        "publishToTestFlight"               | setter                             | true                                                                                       | "Provider<Boolean>"      | PropertyLocation.script
        "publishToTestFlight"               | _                                  | false                                                                                      | _                        | PropertyLocation.none

        "exportOptionsPlist"                | _                                  | osPath("/path/to/exportOptions.plist")                                                     | _                        | PropertyLocation.environment
        "exportOptionsPlist"                | _                                  | osPath("/path/to/exportOptions.plist")                                                     | _                        | PropertyLocation.property
        "exportOptionsPlist"                | assignment                         | osPath("/path/to/exportOptions.plist")                                                     | "File"                   | PropertyLocation.script
        "exportOptionsPlist"                | assignment                         | osPath("/path/to/exportOptions.plist")                                                     | "Provider<RegularFile>"  | PropertyLocation.script
        "exportOptionsPlist"                | providerSet                        | osPath("/path/to/exportOptions.plist")                                                     | "File"                   | PropertyLocation.script
        "exportOptionsPlist"                | providerSet                        | osPath("/path/to/exportOptions.plist")                                                     | "Provider<RegularFile>"  | PropertyLocation.script
        "exportOptionsPlist"                | setter                             | osPath("/path/to/exportOptions.plist")                                                     | "File"                   | PropertyLocation.script
        "exportOptionsPlist"                | setter                             | osPath("/path/to/exportOptions.plist")                                                     | "Provider<RegularFile>"  | PropertyLocation.script
        "exportOptionsPlist"                | _                                  | projectFile("exportOptions.plist")                                                         | _                        | PropertyLocation.none

        "preferWorkspace"                   | _                                  | false                                                                                      | _                        | PropertyLocation.environment
        "preferWorkspace"                   | _                                  | false                                                                                      | _                        | PropertyLocation.property
        "preferWorkspace"                   | assignment                         | false                                                                                      | "Boolean"                | PropertyLocation.script
        "preferWorkspace"                   | assignment                         | false                                                                                      | "Provider<Boolean>"      | PropertyLocation.script
        "preferWorkspace"                   | providerSet                        | false                                                                                      | "Boolean"                | PropertyLocation.script
        "preferWorkspace"                   | providerSet                        | false                                                                                      | "Provider<Boolean>"      | PropertyLocation.script
        "preferWorkspace"                   | setter                             | false                                                                                      | "Boolean"                | PropertyLocation.script
        "preferWorkspace"                   | setter                             | false                                                                                      | "Provider<Boolean>"      | PropertyLocation.script
        "preferWorkspace"                   | _                                  | true                                                                                       | _                        | PropertyLocation.none

        "xcodeProjectDirectory"             | _                                  | osPath("/path/to/project")                                                                 | _                        | PropertyLocation.environment
        "xcodeProjectDirectory"             | _                                  | osPath("/path/to/project2")                                                                | _                        | PropertyLocation.property
        "xcodeProjectDirectory"             | assignment                         | osPath("/path/to/project3")                                                                | "File"                   | PropertyLocation.script
        "xcodeProjectDirectory"             | assignment                         | osPath("/path/to/project4")                                                                | "Provider<Directory>"    | PropertyLocation.script
        "xcodeProjectDirectory"             | providerSet                        | osPath("/path/to/project5")                                                                | "File"                   | PropertyLocation.script
        "xcodeProjectDirectory"             | providerSet                        | osPath("/path/to/project6")                                                                | "Provider<Directory>"    | PropertyLocation.script
        "xcodeProjectDirectory"             | setter                             | osPath("/path/to/project7")                                                                | "File"                   | PropertyLocation.script
        "xcodeProjectDirectory"             | setter                             | osPath("/path/to/project8")                                                                | "Provider<Directory>"    | PropertyLocation.script
        "xcodeProjectDirectory"             | _                                  | projectFile("")                                                                            | _                        | PropertyLocation.none

        "xcodeProjectPath"                  | _                                  | osPath("/path/to/project")                                                                 | _                        | PropertyLocation.environment
        "xcodeProjectPath"                  | _                                  | osPath("/path/to/project2")                                                                | _                        | PropertyLocation.property
        "xcodeProjectPath"                  | assignment                         | osPath("/path/to/project3")                                                                | "File"                   | PropertyLocation.script
        "xcodeProjectPath"                  | assignment                         | osPath("/path/to/project4")                                                                | "Provider<Directory>"    | PropertyLocation.script
        "xcodeProjectPath"                  | providerSet                        | osPath("/path/to/project5")                                                                | "File"                   | PropertyLocation.script
        "xcodeProjectPath"                  | providerSet                        | osPath("/path/to/project6")                                                                | "Provider<Directory>"    | PropertyLocation.script
        "xcodeProjectPath"                  | setter                             | osPath("/path/to/project7")                                                                | "File"                   | PropertyLocation.script
        "xcodeProjectPath"                  | setter                             | osPath("/path/to/project8")                                                                | "Provider<Directory>"    | PropertyLocation.script
        "xcodeProjectPath"                  | _                                  | projectFile("Unity-iPhone.xcodeproj")                                                      | _                        | PropertyLocation.none

        "xcodeWorkspacePath"                | _                                  | osPath("/path/to/project")                                                                 | _                        | PropertyLocation.environment
        "xcodeWorkspacePath"                | _                                  | osPath("/path/to/project2")                                                                | _                        | PropertyLocation.property
        "xcodeWorkspacePath"                | assignment                         | osPath("/path/to/project3")                                                                | "File"                   | PropertyLocation.script
        "xcodeWorkspacePath"                | assignment                         | osPath("/path/to/project4")                                                                | "Provider<Directory>"    | PropertyLocation.script
        "xcodeWorkspacePath"                | providerSet                        | osPath("/path/to/project5")                                                                | "File"                   | PropertyLocation.script
        "xcodeWorkspacePath"                | providerSet                        | osPath("/path/to/project6")                                                                | "Provider<Directory>"    | PropertyLocation.script
        "xcodeWorkspacePath"                | setter                             | osPath("/path/to/project7")                                                                | "File"                   | PropertyLocation.script
        "xcodeWorkspacePath"                | setter                             | osPath("/path/to/project8")                                                                | "Provider<Directory>"    | PropertyLocation.script
        "xcodeWorkspacePath"                | _                                  | projectFile("Unity-iPhone.xcworkspace")                                                    | _                        | PropertyLocation.none

        "projectBaseName"                   | _                                  | "value1"                                                                                   | _                        | PropertyLocation.environment
        "projectBaseName"                   | _                                  | "value2"                                                                                   | _                        | PropertyLocation.property
        "projectBaseName"                   | assignment                         | "value3"                                                                                   | "String"                 | PropertyLocation.script
        "projectBaseName"                   | assignment                         | "value4"                                                                                   | "Provider<String>"       | PropertyLocation.script
        "projectBaseName"                   | providerSet                        | "value5"                                                                                   | "String"                 | PropertyLocation.script
        "projectBaseName"                   | providerSet                        | "value6"                                                                                   | "Provider<String>"       | PropertyLocation.script
        "projectBaseName"                   | setter                             | "value7"                                                                                   | "String"                 | PropertyLocation.script
        "projectBaseName"                   | setter                             | "value8"                                                                                   | "Provider<String>"       | PropertyLocation.script
        "projectBaseName"                   | _                                  | "Unity-iPhone"                                                                             | _                        | PropertyLocation.none

        "cocoapods.executableName"          | _                                  | "value1"                                                                                   | _                        | PropertyLocation.environment
        "cocoapods.executableName"          | _                                  | "value2"                                                                                   | _                        | PropertyLocation.property
        "cocoapods.executableName"          | assignment                         | "value3"                                                                                   | "String"                 | PropertyLocation.script
        "cocoapods.executableName"          | assignment                         | "value4"                                                                                   | "Provider<String>"       | PropertyLocation.script
        "cocoapods.executableName"          | providerSet                        | "value5"                                                                                   | "String"                 | PropertyLocation.script
        "cocoapods.executableName"          | providerSet                        | "value6"                                                                                   | "Provider<String>"       | PropertyLocation.script
        "cocoapods.executableName"          | setter                             | "value7"                                                                                   | "String"                 | PropertyLocation.script
        "cocoapods.executableName"          | setter                             | "value8"                                                                                   | "Provider<String>"       | PropertyLocation.script
        "cocoapods.executableName"          | _                                  | "pod"                                                                                      | _                        | PropertyLocation.none

        "cocoapods.executableDirectory"     | _                                  | osPath("/path/to/project")                                                                 | _                        | PropertyLocation.environment
        "cocoapods.executableDirectory"     | _                                  | osPath("/path/to/project2")                                                                | _                        | PropertyLocation.property
        "cocoapods.executableDirectory"     | assignment                         | osPath("/path/to/project3")                                                                | "File"                   | PropertyLocation.script
        "cocoapods.executableDirectory"     | assignment                         | osPath("/path/to/project4")                                                                | "Provider<Directory>"    | PropertyLocation.script
        "cocoapods.executableDirectory"     | providerSet                        | osPath("/path/to/project5")                                                                | "File"                   | PropertyLocation.script
        "cocoapods.executableDirectory"     | providerSet                        | osPath("/path/to/project6")                                                                | "Provider<Directory>"    | PropertyLocation.script
        "cocoapods.executableDirectory"     | setter                             | osPath("/path/to/project7")                                                                | "File"                   | PropertyLocation.script
        "cocoapods.executableDirectory"     | setter                             | osPath("/path/to/project8")                                                                | "Provider<Directory>"    | PropertyLocation.script
        "cocoapods.executableDirectory"     | _                                  | null                                                                                       | _                        | PropertyLocation.none

        "projectPath"                       | _                                  | projectFile("Unity-iPhone.xcodeproj")                                                      | "File"                   | PropertyLocation.none
        "xcodeProjectFileName"              | _                                  | "Unity-iPhone.xcodeproj"                                                                   | "String"                 | PropertyLocation.none
        "xcodeWorkspaceFileName"            | _                                  | "Unity-iPhone.xcworkspace"                                                                 | "String"                 | PropertyLocation.none
        "preferredProjectFileName"          | _                                  | "Unity-iPhone.xcworkspace"                                                                 | "String"                 | PropertyLocation.none


        set = new PropertySetterWriter(extensionName, property)
                .serialize(wrapValueFallback)
                .set(rawValue, type)
                .to(location)
                .use(invocation)

        get = new PropertyGetterTaskWriter(set)
    }

    @Unroll
    def "extension property :#property returns '#testValue' if value is provided through exportPlist with key '#plistOption' and value '#rawValue'"() {
        given: "a exportOptions object"
        def options = new ExportOptions()

        and: "the test value set"
        options.setProperty(plistOption, rawValue)

        and: "a exportOptions.plist file"
        def exportOptionsPlist = createFile("exportOptions.plist")
        exportOptionsPlist << options.toXMLPropertyList()

        when:
        def query = new PropertyQueryTaskWriter("${extensionName}.${property}")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, testValue)

        where:
        property            | plistOption                    | rawValue                   || expectedValue
        "adhoc"             | "method"                       | "ad-hoc"                   || true
        "adhoc"             | "method"                       | "enterprise"               || false
        "teamId"            | "teamID"                       | "testTeamId"               || _
        "appIdentifier"     | "distributionBundleIdentifier" | "test.bundle.id"           || _
        "signingIdentities" | "signingCertificate"           | "iPhone Test Distribution" || ["iPhone Test Distribution"]

        testValue = (expectedValue == _) ? rawValue : expectedValue
    }

    @Unroll("can set #property with value of type #type in exportOptions plist file through export options object")
    def "can set value in exportOptions plist file through export options object"() {
        given: "a exportOptions object"
        def options = new ExportOptions()
        and: "the initial value set"
        options.uploadBitcode = true
        options.uploadSymbols = true
        options.compileBitcode = true
        options.teamID = "some value"
        options.setProperty(property, initialValue)

        and: "a exportOptions.plist file"
        def exportOptionsPlist = createFile("exportOptions.plist")
        exportOptionsPlist << options.toXMLPropertyList()

        and: "a value set in export options"
        buildFile << """
        ${extensionName}.exportOptions {
            ${property} = ${value} 
        }
        """.stripIndent()

        when:
        def query = new PropertyQueryTaskWriter("${extensionName}.exportOptions.get().${property}", "")
        query.write(buildFile)

        def query2 = new PropertyQueryTaskWriter("${extensionName}.exportOptionsPlist")
        query2.write(buildFile)

        def result = runTasksSuccessfully(query.taskName, query2.taskName)

        then:
        query.matches(result, testValue)

        where:
        property                                   | initialValue                     | rawValue                                                                                                                | type                                 || expectedValue
        "compileBitcode"                           | false                            | true                                                                                                                    | "Boolean"                            || _
        "destination"                              | "upload"                         | "export"                                                                                                                | "String"                             || _
        "distributionBundleIdentifier"             | "com.wooga.test"                 | "net.wooga.foo"                                                                                                         | "String"                             || _
        "embedOnDemandResourcesAssetPacksInBundle" | false                            | true                                                                                                                    | "Boolean"                            || _
        "generateAppStoreInformation"              | true                             | false                                                                                                                   | "Boolean"                            || _
        "iCloudContainerEnvironment"               | "Development"                    | "Production"                                                                                                            | "String"                             || _
        "installerSigningCertificate"              | "Developer ID Installer"         | "Mac Installer Distribution"                                                                                            | "String"                             || _
        "manifest"                                 | null                             | distributionManifest("http://some/url", "http://some/other/url", "http://yet/another/url")                              | "ExportOptions.DistributionManifest" || _
        "manifest"                                 | null                             | ["appURL": "http://some/url", "displayImageURL": "http://some/other/url", "fullSizeImageURL": "http://yet/another/url"] | "Map"                                || distributionManifest(rawValue)
        "method"                                   | "development"                    | "enterprise"                                                                                                            | "String"                             || _
        "onDemandResourcesAssetPacksBaseURL"       | "http://some/url"                | "http://some/other/url"                                                                                                 | "String"                             || _
        "provisioningProfiles"                     | ["com.wooga.app1": "provision1"] | ["com.wooga.app2": "provision2", "com.wooga.sticker1": "provision3"]                                                    | "Map"                                || _
        "signingCertificate"                       | "Mac Developer"                  | "iOS Distribution"                                                                                                      | "String"                             || _
        "signingStyle"                             | "manual"                         | "automatic"                                                                                                             | "String"                             || _
        "stripSwiftSymbols"                        | false                            | true                                                                                                                    | "Boolean"                            || _
        "teamID"                                   | "team1"                          | "team2"                                                                                                                 | "String"                             || _
        "thinning"                                 | "<none>"                         | "<thin-for-all-variants>"                                                                                               | "String"                             || _
        "uploadBitcode"                            | false                            | true                                                                                                                    | "Boolean"                            || _
        "uploadSymbols"                            | false                            | true                                                                                                                    | "Boolean"                            || _

        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), wrapValueFallback) : rawValue
        testValue = (expectedValue == _) ? rawValue : expectedValue
    }

    def "export options configuration actions are transactional"() {
        given: "a exportOptions object"
        def options = new ExportOptions()
        and: "the initial value set"
        options.teamID = "initial value"

        and: "a exportOptions.plist file"
        def exportOptionsPlist = createFile("exportOptions.plist")
        exportOptionsPlist << options.toXMLPropertyList()

        and: "a value set in export options"
        buildFile << """
        ${extensionName}.exportOptions {
            teamID = teamID + " with a value appended" 
        }
        """.stripIndent()

        and: "another value set in export options"
        buildFile << """
        ${extensionName}.exportOptions {
            teamID = "a prefixed " + teamID 
        }
        """.stripIndent()

        when:
        def query = new PropertyQueryTaskWriter("${extensionName}.exportOptions.get().teamID", "")
        query.write(buildFile)

        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, "a prefixed initial value with a value appended")
    }
}
