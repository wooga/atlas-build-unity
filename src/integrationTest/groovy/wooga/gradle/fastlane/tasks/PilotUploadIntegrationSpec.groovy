/*
 * Copyright 2018-2020 Wooga GmbH
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

package wooga.gradle.fastlane.tasks

import spock.lang.Requires
import spock.lang.Unroll

/**
 * The test examples in this class are not 100% integration/functional tests.
 *
 * We can't run the real fastlane and connect to apple because there is no easy way to setup and maintain a test app and
 * account with necessary credentials. We only test the invocation of fastlane and its parameters.
 */
@Requires({ os.macOs })
class PilotUploadIntegrationSpec extends AbstractFastlaneTaskIntegrationSpec {

    String testTaskName = "pilotUpload"

    Class taskType = PilotUpload

    def ipaFile = File.createTempFile("mockIpa", ".ipa")

    String workingFastlaneTaskConfig = """
        task("${testTaskName}", type: ${taskType.name}) {
            ipa = file("${ipaFile.path}")
        }
        """.stripIndent()

    @Unroll("property #property #valueMessage sets flag #expectedCommandlineFlag")
    def "constructs arguments"() {
        given: "a task to read the build arguments"
        buildFile << """
            task("readValue") {
                doLast {
                    println("arguments: " + ${testTaskName}.arguments.get().join(" "))
                }
            }
        """.stripIndent()

        and: "a set property"
        if (method != _) {
            buildFile << """
            ${testTaskName}.${method}($value)
            """.stripIndent()
        }

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        outputContains(result, expectedCommandlineFlag)

        where:
        property                        | method                              | rawValue                   | type           || expectedCommandlineFlag
        "appIdentifier"                 | "appIdentifier.set"                 | "com.test.app"             | "String"       || "--app_identifier com.test.app"
        "teamId"                        | "teamId.set"                        | "test"                     | "String"       || "--team_id test"
        "devPortalTeamId"               | "devPortalTeamId.set"               | "test"                     | "String"       || "--dev_portal_team_id test"
        "teamName"                      | "teamName.set"                      | "test"                     | "String"       || "--team_name test"
        "username"                      | "username.set"                      | "testUser"                 | "String"       || "--username testUser"
        "itcProvider"                   | "itcProvider.set"                   | "iphone"                   | "String"       || "--itc_provider iphone"
        "skipSubmission"                | "skipSubmission.set"                | true                       | "Boolean"      || "--skip_submission true"
        "skipSubmission"                | "skipSubmission.set"                | false                      | "Boolean"      || "--skip_submission false"
        "skipSubmission"                | _                                   | _                          | "Boolean"      || "--skip_submission false"
        "skipWaitingForBuildProcessing" | "skipWaitingForBuildProcessing.set" | true                       | "Boolean"      || "--skip_waiting_for_build_processing true"
        "skipWaitingForBuildProcessing" | "skipWaitingForBuildProcessing.set" | false                      | "Boolean"      || "--skip_waiting_for_build_processing false"
        "skipWaitingForBuildProcessing" | _                                   | _                          | "Boolean"      || "--skip_waiting_for_build_processing false"
        "ipa"                           | "ipa.set"                           | "/path/to/test2.ipa"       | "File"         || "--ipa /path/to/test2.ipa"
        "additionalArguments"           | "setAdditionalArguments"            | ["--verbose", "--foo bar"] | "List<String>" || "--verbose --foo bar"
        value = wrapValueBasedOnType(rawValue, type)
        valueMessage = (rawValue != _) ? "with value ${value}" : "without value"
    }

    @Unroll("property #property #valueMessage sets environment #expectedEnvironmentPair")
    def "constructs process environment"() {
        given: "a task to read the build arguments"
        buildFile << """
            task("readValue") {
                doLast {
                    println("arguments: " + ${testTaskName}.environment.get().collect {k,v -> k + '=' + v}.join("\\n"))
                }
            }
        """.stripIndent()

        and: "a set property"
        if (method != _) {
            buildFile << """
            ${testTaskName}.${method}($value)
            """.stripIndent()
        }

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        outputContains(result, expectedEnvironmentPair)

        where:
        property   | method         | rawValue      | type     || expectedEnvironmentPair
        "password" | "password.set" | "secretValue" | "String" || "FASTLANE_PASSWORD=secretValue"
        value = wrapValueBasedOnType(rawValue, type)
        valueMessage = (rawValue != _) ? "with value ${value}" : "without value"
    }

    @Unroll("can set property #property with #method and type #type")
    def "can set property SighRenew"() {
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
        property                        | method                              | rawValue             | type
        "appIdentifier"                 | "appIdentifier"                     | "com.test.app1"      | "String"
        "appIdentifier"                 | "appIdentifier"                     | "com.test.app2"      | "Provider<String>"
        "appIdentifier"                 | "appIdentifier.set"                 | "com.test.app1"      | "String"
        "appIdentifier"                 | "appIdentifier.set"                 | "com.test.app2"      | "Provider<String>"
        "appIdentifier"                 | "setAppIdentifier"                  | "com.test.app3"      | "String"
        "appIdentifier"                 | "setAppIdentifier"                  | "com.test.app4"      | "Provider<String>"

        "teamId"                        | "teamId"                            | "1234561"            | "String"
        "teamId"                        | "teamId"                            | "1234562"            | "Provider<String>"
        "teamId"                        | "teamId.set"                        | "1234561"            | "String"
        "teamId"                        | "teamId.set"                        | "1234562"            | "Provider<String>"
        "teamId"                        | "setTeamId"                         | "1234563"            | "String"
        "teamId"                        | "setTeamId"                         | "1234564"            | "Provider<String>"

        "devPortalTeamId"               | "devPortalTeamId"                   | "1234561"            | "String"
        "devPortalTeamId"               | "devPortalTeamId"                   | "1234562"            | "Provider<String>"
        "devPortalTeamId"               | "devPortalTeamId.set"               | "1234561"            | "String"
        "devPortalTeamId"               | "devPortalTeamId.set"               | "1234562"            | "Provider<String>"
        "devPortalTeamId"               | "setDevPortalTeamId"                | "1234563"            | "String"
        "devPortalTeamId"               | "setDevPortalTeamId"                | "1234564"            | "Provider<String>"

        "teamName"                      | "teamName"                          | "someTeam1"          | "String"
        "teamName"                      | "teamName"                          | "someTeam2"          | "Provider<String>"
        "teamName"                      | "teamName.set"                      | "someTeam3"          | "String"
        "teamName"                      | "teamName.set"                      | "someTeam4"          | "Provider<String>"
        "teamName"                      | "setTeamName"                       | "someTeam5"          | "String"
        "teamName"                      | "setTeamName"                       | "someTeam6"          | "Provider<String>"

        "username"                      | "username"                          | "someName1"          | "String"
        "username"                      | "username"                          | "someName2"          | "Provider<String>"
        "username"                      | "username.set"                      | "someName3"          | "String"
        "username"                      | "username.set"                      | "someName4"          | "Provider<String>"
        "username"                      | "setUsername"                       | "someName5"          | "String"
        "username"                      | "setUsername"                       | "someName6"          | "Provider<String>"

        "password"                      | "password"                          | "1234561"            | "String"
        "password"                      | "password"                          | "1234562"            | "Provider<String>"
        "password"                      | "password.set"                      | "1234561"            | "String"
        "password"                      | "password.set"                      | "1234562"            | "Provider<String>"
        "password"                      | "setPassword"                       | "1234563"            | "String"
        "password"                      | "setPassword"                       | "1234564"            | "Provider<String>"

        "itcProvider"                   | "itcProvider"                       | "test1"              | "String"
        "itcProvider"                   | "itcProvider"                       | "test2"              | "Provider<String>"
        "itcProvider"                   | "itcProvider.set"                   | "test1"              | "String"
        "itcProvider"                   | "itcProvider.set"                   | "test2"              | "Provider<String>"
        "itcProvider"                   | "setItcProvider"                    | "test3"              | "String"
        "itcProvider"                   | "setItcProvider"                    | "test4"              | "Provider<String>"

        "ipa"                           | "ipa"                               | "/path/to/test1.ipa" | "File"
        "ipa"                           | "ipa"                               | "/path/to/test2.ipa" | "Provider<RegularFile>"
        "ipa"                           | "ipa.set"                           | "/path/to/test3.ipa" | "File"
        "ipa"                           | "ipa.set"                           | "/path/to/test4.ipa" | "Provider<RegularFile>"
        "ipa"                           | "setIpa"                            | "/path/to/test5.ipa" | "File"
        "ipa"                           | "setIpa"                            | "/path/to/test6.ipa" | "Provider<RegularFile>"

        "skipSubmission"                | "skipSubmission"                    | true                 | "Boolean"
        "skipSubmission"                | "skipSubmission"                    | false                | "Boolean"
        "skipSubmission"                | "skipSubmission"                    | true                 | "Provider<Boolean>"
        "skipSubmission"                | "skipSubmission"                    | false                | "Provider<Boolean>"
        "skipSubmission"                | "skipSubmission.set"                | true                 | "Boolean"
        "skipSubmission"                | "skipSubmission.set"                | false                | "Boolean"
        "skipSubmission"                | "skipSubmission.set"                | true                 | "Provider<Boolean>"
        "skipSubmission"                | "skipSubmission.set"                | false                | "Provider<Boolean>"
        "skipSubmission"                | "setSkipSubmission"                 | true                 | "Boolean"
        "skipSubmission"                | "setSkipSubmission"                 | false                | "Boolean"
        "skipSubmission"                | "setSkipSubmission"                 | true                 | "Provider<Boolean>"
        "skipSubmission"                | "setSkipSubmission"                 | false                | "Provider<Boolean>"

        "skipWaitingForBuildProcessing" | "skipWaitingForBuildProcessing"     | true                 | "Boolean"
        "skipWaitingForBuildProcessing" | "skipWaitingForBuildProcessing"     | false                | "Boolean"
        "skipWaitingForBuildProcessing" | "skipWaitingForBuildProcessing"     | true                 | "Provider<Boolean>"
        "skipWaitingForBuildProcessing" | "skipWaitingForBuildProcessing"     | false                | "Provider<Boolean>"
        "skipWaitingForBuildProcessing" | "skipWaitingForBuildProcessing.set" | true                 | "Boolean"
        "skipWaitingForBuildProcessing" | "skipWaitingForBuildProcessing.set" | false                | "Boolean"
        "skipWaitingForBuildProcessing" | "skipWaitingForBuildProcessing.set" | true                 | "Provider<Boolean>"
        "skipWaitingForBuildProcessing" | "skipWaitingForBuildProcessing.set" | false                | "Provider<Boolean>"
        "skipWaitingForBuildProcessing" | "setSkipWaitingForBuildProcessing"  | true                 | "Boolean"
        "skipWaitingForBuildProcessing" | "setSkipWaitingForBuildProcessing"  | false                | "Boolean"
        "skipWaitingForBuildProcessing" | "setSkipWaitingForBuildProcessing"  | true                 | "Provider<Boolean>"
        "skipWaitingForBuildProcessing" | "setSkipWaitingForBuildProcessing"  | false                | "Provider<Boolean>"

        value = wrapValueBasedOnType(rawValue, type)
        expectedValue = rawValue
    }

    def "task is never up-to-date"() {
        given: "call tasks once"
        def r = runTasks(testTaskName)

        when: "no parameter changes"
        def result = runTasksSuccessfully(testTaskName)

        then:
        !result.wasUpToDate(testTaskName)
    }

    def "task skips with no-source when ipa is not set"() {
        given: "call tasks once"
        def result = runTasks(testTaskName)
        assert !outputContains(result, "Task :${testTaskName} NO-SOURCE")

        when: "the task with ipa param set to null"
        buildFile << """
        ${testTaskName}.ipa = null
        """.stripIndent()

        result = runTasksSuccessfully(testTaskName)

        then:
        outputContains(result, "Task :${testTaskName} NO-SOURCE")
    }
}
