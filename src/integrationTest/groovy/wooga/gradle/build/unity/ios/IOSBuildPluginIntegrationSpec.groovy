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

import com.wooga.gradle.test.ConventionSource
import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.PropertyQueryTaskWriter
import nebula.test.functional.ExecutionResult
import net.wooga.system.ProcessList
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Timeout
import spock.lang.Unroll
import wooga.gradle.fastlane.tasks.PilotUpload
import wooga.gradle.fastlane.tasks.SighRenew
import wooga.gradle.macOS.security.tasks.SecurityCreateKeychain
import wooga.gradle.xcodebuild.tasks.ExportArchive
import wooga.gradle.xcodebuild.tasks.XcodeArchive

import static com.wooga.gradle.PlatformUtils.escapedPath
import static com.wooga.gradle.test.PropertyUtils.*

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

    @Unroll
    def "extension property :#property returns '#testValue' if #reason"() {
        given: "a set value"
        switch (location) {
            case PropertyLocation.script:
                buildFile << "${extensionName}.${invocation}"
                break
            case PropertyLocation.property:
                def propertiesFile = createFile("gradle.properties")
                propertiesFile << "${extensionName}.${property} = ${escapedValue}"
                break
            case PropertyLocation.environment:
                def envPropertyKey = envNameFromProperty(extensionName, property)
                environmentVariables.set(envPropertyKey, value.toString())
                break
            default:
                break
        }

        and: "the test value with replace placeholders"
        if (testValue instanceof String) {
            testValue = testValue.replaceAll("#projectDir#", escapedPath(projectDir.path))
        }

        when:
        def query = new PropertyQueryTaskWriter("${extensionName}.${property}")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, testValue)

        where:
        property                            | method                      | rawValue                                | expectedValue                               | type                     | location
        "keychainPassword"                  | _                           | _                                       | null                                        | "Provider<String>"       | PropertyLocation.none
        "keychainPassword"                  | toSetter(property)          | "password1"                             | _                                           | "Provider<String>"       | PropertyLocation.script
        "keychainPassword"                  | toSetter(property)          | "password1"                             | _                                           | "String"                 | PropertyLocation.script
        "keychainPassword"                  | toProviderSet(property)     | "password1"                             | _                                           | "Provider<String>"       | PropertyLocation.script
        "keychainPassword"                  | toProviderSet(property)     | "password1"                             | _                                           | "String"                 | PropertyLocation.script

        "signingIdentities"                 | _                           | _                                       | []                                          | _                        | PropertyLocation.none
        "signingIdentities"                 | toSetter(property)          | ["ID1", "ID2"]                          | _                                           | "List<String>"           | PropertyLocation.script
        "signingIdentities"                 | toSetter(property)          | ["ID3", "ID4"]                          | _                                           | "Provider<List<String>>" | PropertyLocation.script
        "signingIdentities"                 | toProviderSet(property)     | ["ID5", "ID6"]                          | _                                           | "List<String>"           | PropertyLocation.script
        "signingIdentities"                 | toProviderSet(property)     | ["ID7", "ID8"]                          | _                                           | "Provider<List<String>>" | PropertyLocation.script
        "signingIdentities"                 | toSetter("signingIdentity") | "code sign: ID3"                        | [rawValue]                                  | "String"                 | PropertyLocation.script
        "signingIdentities"                 | toSetter("signingIdentity") | "code sign: ID4"                        | [rawValue]                                  | "Provider<String>"       | PropertyLocation.script

        "codeSigningIdentityFile"           | _                           | _                                       | null                                        | "Provider<RegularFile>"  | PropertyLocation.none
        "codeSigningIdentityFile"           | property                    | "/path/to/p12"                          | _                                           | "File"                   | PropertyLocation.script
        "codeSigningIdentityFile"           | property                    | "/path/to/p12"                          | _                                           | "Provider<RegularFile>"  | PropertyLocation.script
        "codeSigningIdentityFile"           | toProviderSet(property)     | "/path/to/p12"                          | _                                           | "File"                   | PropertyLocation.script
        "codeSigningIdentityFile"           | toProviderSet(property)     | "/path/to/p12"                          | _                                           | "Provider<RegularFile>"  | PropertyLocation.script
        "codeSigningIdentityFile"           | toSetter(property)          | "/path/to/p12"                          | _                                           | "File"                   | PropertyLocation.script
        "codeSigningIdentityFile"           | toSetter(property)          | "/path/to/p12"                          | _                                           | "Provider<RegularFile>"  | PropertyLocation.script

        "codeSigningIdentityFilePassphrase" | _                           | _                                       | null                                        | "Provider<String>"       | PropertyLocation.none
        "codeSigningIdentityFilePassphrase" | property                    | "testPassphrase1"                       | _                                           | "String"                 | PropertyLocation.script
        "codeSigningIdentityFilePassphrase" | property                    | "testPassphrase2"                       | _                                           | "Provider<String>"       | PropertyLocation.script
        "codeSigningIdentityFilePassphrase" | toProviderSet(property)     | "testPassphrase3"                       | _                                           | "String"                 | PropertyLocation.script
        "codeSigningIdentityFilePassphrase" | toProviderSet(property)     | "testPassphrase4"                       | _                                           | "Provider<String>"       | PropertyLocation.script
        "codeSigningIdentityFilePassphrase" | toSetter(property)          | "testPassphrase5"                       | _                                           | "String"                 | PropertyLocation.script
        "codeSigningIdentityFilePassphrase" | toSetter(property)          | "testPassphrase6"                       | _                                           | "Provider<String>"       | PropertyLocation.script

        "appIdentifier"                     | property                    | "com.test.app.1"                        | _                                           | "String"                 | PropertyLocation.script
        "appIdentifier"                     | property                    | "com.test.app.2"                        | _                                           | "Provider<String>"       | PropertyLocation.script
        "appIdentifier"                     | toProviderSet(property)     | "com.test.app.3"                        | _                                           | "String"                 | PropertyLocation.script
        "appIdentifier"                     | toProviderSet(property)     | "com.test.app.4"                        | _                                           | "Provider<String>"       | PropertyLocation.script
        "appIdentifier"                     | toSetter(property)          | "com.test.app.5"                        | _                                           | "String"                 | PropertyLocation.script
        "appIdentifier"                     | toSetter(property)          | "com.test.app.6"                        | _                                           | "Provider<String>"       | PropertyLocation.script

        "teamId"                            | property                    | "team1"                                 | _                                           | "String"                 | PropertyLocation.script
        "teamId"                            | property                    | "team2"                                 | _                                           | "Provider<String>"       | PropertyLocation.script
        "teamId"                            | toProviderSet(property)     | "team3"                                 | _                                           | "String"                 | PropertyLocation.script
        "teamId"                            | toProviderSet(property)     | "team4"                                 | _                                           | "Provider<String>"       | PropertyLocation.script
        "teamId"                            | toSetter(property)          | "team5"                                 | _                                           | "String"                 | PropertyLocation.script
        "teamId"                            | toSetter(property)          | "team6"                                 | _                                           | "Provider<String>"       | PropertyLocation.script

        "scheme"                            | property                    | "value1"                                | _                                           | "String"                 | PropertyLocation.script
        "scheme"                            | property                    | "value2"                                | _                                           | "Provider<String>"       | PropertyLocation.script
        "scheme"                            | toProviderSet(property)     | "value3"                                | _                                           | "String"                 | PropertyLocation.script
        "scheme"                            | toProviderSet(property)     | "value4"                                | _                                           | "Provider<String>"       | PropertyLocation.script
        "scheme"                            | toSetter(property)          | "value5"                                | _                                           | "String"                 | PropertyLocation.script
        "scheme"                            | toSetter(property)          | "value6"                                | _                                           | "Provider<String>"       | PropertyLocation.script

        "configuration"                     | property                    | "value1"                                | _                                           | "String"                 | PropertyLocation.script
        "configuration"                     | property                    | "value2"                                | _                                           | "Provider<String>"       | PropertyLocation.script
        "configuration"                     | toProviderSet(property)     | "value3"                                | _                                           | "String"                 | PropertyLocation.script
        "configuration"                     | toProviderSet(property)     | "value4"                                | _                                           | "Provider<String>"       | PropertyLocation.script
        "configuration"                     | toSetter(property)          | "value5"                                | _                                           | "String"                 | PropertyLocation.script
        "configuration"                     | toSetter(property)          | "value6"                                | _                                           | "Provider<String>"       | PropertyLocation.script

        "provisioningName"                  | property                    | "value1"                                | _                                           | "String"                 | PropertyLocation.script
        "provisioningName"                  | property                    | "value2"                                | _                                           | "Provider<String>"       | PropertyLocation.script
        "provisioningName"                  | toProviderSet(property)     | "value3"                                | _                                           | "String"                 | PropertyLocation.script
        "provisioningName"                  | toProviderSet(property)     | "value4"                                | _                                           | "Provider<String>"       | PropertyLocation.script
        "provisioningName"                  | toSetter(property)          | "value5"                                | _                                           | "String"                 | PropertyLocation.script
        "provisioningName"                  | toSetter(property)          | "value6"                                | _                                           | "Provider<String>"       | PropertyLocation.script

        "adhoc"                             | property                    | true                                    | _                                           | "Boolean"                | PropertyLocation.script
        "adhoc"                             | property                    | true                                    | _                                           | "Provider<Boolean>"      | PropertyLocation.script
        "adhoc"                             | toProviderSet(property)     | true                                    | _                                           | "Boolean"                | PropertyLocation.script
        "adhoc"                             | toProviderSet(property)     | true                                    | _                                           | "Provider<Boolean>"      | PropertyLocation.script
        "adhoc"                             | toSetter(property)          | true                                    | _                                           | "Boolean"                | PropertyLocation.script
        "adhoc"                             | toSetter(property)          | true                                    | _                                           | "Provider<Boolean>"      | PropertyLocation.script

        "publishToTestFlight"               | property                    | true                                    | _                                           | "Boolean"                | PropertyLocation.script
        "publishToTestFlight"               | property                    | true                                    | _                                           | "Provider<Boolean>"      | PropertyLocation.script
        "publishToTestFlight"               | toProviderSet(property)     | true                                    | _                                           | "Boolean"                | PropertyLocation.script
        "publishToTestFlight"               | toProviderSet(property)     | true                                    | _                                           | "Provider<Boolean>"      | PropertyLocation.script
        "publishToTestFlight"               | toSetter(property)          | true                                    | _                                           | "Boolean"                | PropertyLocation.script
        "publishToTestFlight"               | toSetter(property)          | true                                    | _                                           | "Provider<Boolean>"      | PropertyLocation.script

        "exportOptionsPlist"                | _                           | "exportOptions.plist"                   | "#projectDir#/${rawValue}".toString()       | "File"                   | PropertyLocation.script
        "exportOptionsPlist"                | toProviderSet(property)     | "path/to/exportOptions1.plist"          | "#projectDir#/${rawValue}".toString()       | "File"                   | PropertyLocation.script
        "exportOptionsPlist"                | toProviderSet(property)     | "path/to/exportOptions1.plist"          | "#projectDir#/build/${rawValue}".toString() | "Provider<RegularFile>"  | PropertyLocation.script
        "exportOptionsPlist"                | toSetter(property)          | "path/to/exportOptions1.plist"          | "#projectDir#/${rawValue}".toString()       | "File"                   | PropertyLocation.script
        "exportOptionsPlist"                | toSetter(property)          | "path/to/exportOptions1.plist"          | "#projectDir#/build/${rawValue}".toString() | "Provider<RegularFile>"  | PropertyLocation.script

        "projectBaseName"                   | property                    | "Unity-iPhone"                          | _                                           | "String"                 | PropertyLocation.none
        "projectBaseName"                   | property                    | "value1"                                | _                                           | "String"                 | PropertyLocation.script
        "projectBaseName"                   | property                    | "value2"                                | _                                           | "Provider<String>"       | PropertyLocation.script
        "projectBaseName"                   | toProviderSet(property)     | "value3"                                | _                                           | "String"                 | PropertyLocation.script
        "projectBaseName"                   | toProviderSet(property)     | "value4"                                | _                                           | "Provider<String>"       | PropertyLocation.script
        "projectBaseName"                   | toSetter(property)          | "value5"                                | _                                           | "String"                 | PropertyLocation.script
        "projectBaseName"                   | toSetter(property)          | "value6"                                | _                                           | "Provider<String>"       | PropertyLocation.script

        "preferWorkspace"                   | property                    | true                                    | _                                           | "Boolean"                | PropertyLocation.none
        "preferWorkspace"                   | property                    | false                                   | _                                           | "Boolean"                | PropertyLocation.script
        "preferWorkspace"                   | property                    | true                                    | _                                           | "Provider<Boolean>"      | PropertyLocation.script
        "preferWorkspace"                   | toProviderSet(property)     | false                                   | _                                           | "Boolean"                | PropertyLocation.script
        "preferWorkspace"                   | toProviderSet(property)     | true                                    | _                                           | "Provider<Boolean>"      | PropertyLocation.script
        "preferWorkspace"                   | toSetter(property)          | false                                   | _                                           | "Boolean"                | PropertyLocation.script
        "preferWorkspace"                   | toSetter(property)          | true                                    | _                                           | "Provider<Boolean>"      | PropertyLocation.script

        "xcodeProjectDirectory"             | _                           | "#projectDir#"                          | _                                           | "File"                   | PropertyLocation.none
        "xcodeProjectDirectory"             | _                           | "path/to/project"                       | "#projectDir#/${rawValue}".toString()       | "File"                   | PropertyLocation.script
        "xcodeProjectDirectory"             | toProviderSet(property)     | "path/to/project2"                      | "#projectDir#/${rawValue}".toString()       | "File"                   | PropertyLocation.script
        "xcodeProjectDirectory"             | toProviderSet(property)     | "path/to/project3"                      | "#projectDir#/build/${rawValue}".toString() | "Provider<Directory>"    | PropertyLocation.script
        "xcodeProjectDirectory"             | toSetter(property)          | "path/to/project4"                      | "#projectDir#/${rawValue}".toString()       | "File"                   | PropertyLocation.script
        "xcodeProjectDirectory"             | toSetter(property)          | "path/to/project5"                      | "#projectDir#/build/${rawValue}".toString() | "Provider<Directory>"    | PropertyLocation.script

        "xcodeProjectPath"                  | _                           | "#projectDir#/Unity-iPhone.xcodeproj"   | _                                           | "File"                   | PropertyLocation.none
        "xcodeProjectPath"                  | _                           | "path/to/Unity-iPhone1.xcodeproj"       | "#projectDir#/${rawValue}".toString()       | "File"                   | PropertyLocation.script
        "xcodeProjectPath"                  | toProviderSet(property)     | "path/to/Unity-iPhone2.xcodeproj"       | "#projectDir#/${rawValue}".toString()       | "File"                   | PropertyLocation.script
        "xcodeProjectPath"                  | toProviderSet(property)     | "path/to/Unity-iPhone3.xcodeproj"       | "#projectDir#/build/${rawValue}".toString() | "Provider<Directory>"    | PropertyLocation.script
        "xcodeProjectPath"                  | toSetter(property)          | "path/to/Unity-iPhone4.xcodeproj"       | "#projectDir#/${rawValue}".toString()       | "File"                   | PropertyLocation.script
        "xcodeProjectPath"                  | toSetter(property)          | "path/to/Unity-iPhone5.xcodeproj"       | "#projectDir#/build/${rawValue}".toString() | "Provider<Directory>"    | PropertyLocation.script

        "xcodeWorkspacePath"                | _                           | "#projectDir#/Unity-iPhone.xcworkspace" | _                                           | "File"                   | PropertyLocation.none
        "xcodeWorkspacePath"                | _                           | "path/to/Unity-iPhone1.xcworkspace"     | "#projectDir#/${rawValue}".toString()       | "File"                   | PropertyLocation.script
        "xcodeWorkspacePath"                | toProviderSet(property)     | "path/to/Unity-iPhone2.xcworkspace"     | "#projectDir#/${rawValue}".toString()       | "File"                   | PropertyLocation.script
        "xcodeWorkspacePath"                | toProviderSet(property)     | "path/to/Unity-iPhone3.xcworkspace"     | "#projectDir#/build/${rawValue}".toString() | "Provider<Directory>"    | PropertyLocation.script
        "xcodeWorkspacePath"                | toSetter(property)          | "path/to/Unity-iPhone4.xcworkspace"     | "#projectDir#/${rawValue}".toString()       | "File"                   | PropertyLocation.script
        "xcodeWorkspacePath"                | toSetter(property)          | "path/to/Unity-iPhone5.xcworkspace"     | "#projectDir#/build/${rawValue}".toString() | "Provider<Directory>"    | PropertyLocation.script

        "projectPath"                       | _                           | "#projectDir#/Unity-iPhone.xcodeproj"   | _                                           | "File"                   | PropertyLocation.none
        "xcodeProjectFileName"              | _                           | "Unity-iPhone.xcodeproj"                | _                                           | "String"                 | PropertyLocation.none
        "xcodeWorkspaceFileName"            | _                           | "Unity-iPhone.xcworkspace"              | _                                           | "String"                 | PropertyLocation.none
        "preferredProjectFileName"          | _                           | "Unity-iPhone.xcworkspace"              | _                                           | "String"                 | PropertyLocation.none

        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), wrapValueFallback) : rawValue
        providedValue = (location == PropertyLocation.script) ? type : value
        testValue = (expectedValue == _) ? rawValue : expectedValue
        reason = location.reason() + ((location == PropertyLocation.none) ? "" : "  with '$providedValue' ")
        escapedValue = (value instanceof String) ? escapedPath(value) : value
        invocation = (method != _) ? "${method}(${escapedValue})" : "${property} = ${escapedValue}"
    }
}
