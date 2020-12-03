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

import spock.lang.Issue
import spock.lang.Requires
import spock.lang.Unroll

/**
 * The test examples in this class are not 100% integration/functional tests.
 *
 * We can't run the real fastlane and connect to apple because there is no easy way to setup and maintain a test app and
 * account with necessary credentials. We only test the invocation of fastlane and its parameters.
 */
@Requires({ os.macOs })
class SighRenewIntegrationSpec extends AbstractFastlaneTaskIntegrationSpec {

    String testTaskName = "sighRenew"
    Class taskType = SighRenew

    String workingFastlaneTaskConfig = """
        task("${testTaskName}", type: ${taskType.name}) {
            appIdentifier = 'test'
            teamId = "fakeTeamId"
            fileName = 'test.mobileprovisioning'
            destinationDir = file('build')
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
        property                          | method                                | rawValue                   | type           || expectedCommandlineFlag
        "appIdentifier"                   | "appIdentifier.set"                   | "com.test.app"             | "String"       || "--app_identifier com.test.app"
        "teamId"                          | "teamId.set"                          | "test"                     | "String"       || "--team_id test"
        "teamName"                        | "teamName.set"                        | "test"                     | "String"       || "--team_name test"
        "provisioningName"                | "provisioningName.set"                | "test"                     | "String"       || "--provisioning_name test"
        "username"                        | "username.set"                        | "testUser"                 | "String"       || "--username testUser"
        "adhoc"                           | "adhoc.set"                           | true                       | "Boolean"      || "--adhoc true"
        "adhoc"                           | "adhoc.set"                           | false                      | "Boolean"      || "--adhoc false"
        "adhoc"                           | _                                     | _                          | "Boolean"      || "--adhoc false"
        "readOnly"                        | "readOnly.set"                        | true                       | "Boolean"      || "--readonly true"
        "readOnly"                        | "readOnly.set"                        | false                      | "Boolean"      || "--readonly false"
        "readOnly"                        | _                                     | _                          | "Boolean"      || "--readonly false"
        "ignoreProfilesWithDifferentName" | "ignoreProfilesWithDifferentName.set" | true                       | "Boolean"      || "--ignore_profiles_with_different_name true"
        "ignoreProfilesWithDifferentName" | "ignoreProfilesWithDifferentName.set" | false                      | "Boolean"      || "--ignore_profiles_with_different_name false"
        "ignoreProfilesWithDifferentName" | _                                     | _                          | "Boolean"      || "--ignore_profiles_with_different_name false"
        "fileName"                        | "fileName.set"                        | "test2.mobileprovisioning" | "String"       || "--filename test2.mobileprovisioning"
        "destinationDir"                  | "destinationDir.set"                  | "/some/path"               | "File"         || "--output_path /some/path"
        "additionalArguments"             | "setAdditionalArguments"              | ["--verbose", "--foo bar"] | "List<String>" || "--verbose --foo bar"
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
    def "can set property XcodeArchive"() {
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
        property                          | method                                | rawValue        | type
        "appIdentifier"                   | "appIdentifier"                       | "com.test.app1" | "String"
        "appIdentifier"                   | "appIdentifier"                       | "com.test.app2" | "Provider<String>"
        "appIdentifier"                   | "appIdentifier.set"                   | "com.test.app1" | "String"
        "appIdentifier"                   | "appIdentifier.set"                   | "com.test.app2" | "Provider<String>"
        "appIdentifier"                   | "setAppIdentifier"                    | "com.test.app3" | "String"
        "appIdentifier"                   | "setAppIdentifier"                    | "com.test.app4" | "Provider<String>"

        "teamId"                          | "teamId"                              | "1234561"       | "String"
        "teamId"                          | "teamId"                              | "1234562"       | "Provider<String>"
        "teamId"                          | "teamId.set"                          | "1234561"       | "String"
        "teamId"                          | "teamId.set"                          | "1234562"       | "Provider<String>"
        "teamId"                          | "setTeamId"                           | "1234563"       | "String"
        "teamId"                          | "setTeamId"                           | "1234564"       | "Provider<String>"

        "teamName"                        | "teamName"                            | "someTeam1"     | "String"
        "teamName"                        | "teamName"                            | "someTeam2"     | "Provider<String>"
        "teamName"                        | "teamName.set"                        | "someTeam3"     | "String"
        "teamName"                        | "teamName.set"                        | "someTeam4"     | "Provider<String>"
        "teamName"                        | "setTeamName"                         | "someTeam5"     | "String"
        "teamName"                        | "setTeamName"                         | "someTeam6"     | "Provider<String>"

        "username"                        | "username"                            | "someName1"     | "String"
        "username"                        | "username"                            | "someName2"     | "Provider<String>"
        "username"                        | "username.set"                        | "someName3"     | "String"
        "username"                        | "username.set"                        | "someName4"     | "Provider<String>"
        "username"                        | "setUsername"                         | "someName5"     | "String"
        "username"                        | "setUsername"                         | "someName6"     | "Provider<String>"

        "password"                        | "password"                            | "1234561"       | "String"
        "password"                        | "password"                            | "1234562"       | "Provider<String>"
        "password"                        | "password.set"                        | "1234561"       | "String"
        "password"                        | "password.set"                        | "1234562"       | "Provider<String>"
        "password"                        | "setPassword"                         | "1234563"       | "String"
        "password"                        | "setPassword"                         | "1234564"       | "Provider<String>"

        "fileName"                        | "fileName"                            | "name1"         | "String"
        "fileName"                        | "fileName"                            | "name2"         | "Provider<String>"
        "fileName"                        | "fileName.set"                        | "name3"         | "String"
        "fileName"                        | "fileName.set"                        | "name4"         | "Provider<String>"
        "fileName"                        | "setFileName"                         | "name5"         | "String"
        "fileName"                        | "setFileName"                         | "name6"         | "Provider<String>"

        "provisioningName"                | "provisioningName"                    | "name1"         | "String"
        "provisioningName"                | "provisioningName"                    | "name2"         | "Provider<String>"
        "provisioningName"                | "provisioningName.set"                | "name3"         | "String"
        "provisioningName"                | "provisioningName.set"                | "name4"         | "Provider<String>"
        "provisioningName"                | "setProvisioningName"                 | "name5"         | "String"
        "provisioningName"                | "setProvisioningName"                 | "name6"         | "Provider<String>"

        "adhoc"                           | "adhoc"                               | true            | "Boolean"
        "adhoc"                           | "adhoc"                               | false           | "Boolean"
        "adhoc"                           | "adhoc"                               | true            | "Provider<Boolean>"
        "adhoc"                           | "adhoc"                               | false           | "Provider<Boolean>"
        "adhoc"                           | "adhoc.set"                           | true            | "Boolean"
        "adhoc"                           | "adhoc.set"                           | false           | "Boolean"
        "adhoc"                           | "adhoc.set"                           | true            | "Provider<Boolean>"
        "adhoc"                           | "adhoc.set"                           | false           | "Provider<Boolean>"
        "adhoc"                           | "setAdhoc"                            | true            | "Boolean"
        "adhoc"                           | "setAdhoc"                            | false           | "Boolean"
        "adhoc"                           | "setAdhoc"                            | true            | "Provider<Boolean>"
        "adhoc"                           | "setAdhoc"                            | false           | "Provider<Boolean>"

        "readOnly"                        | "readOnly"                            | true            | "Boolean"
        "readOnly"                        | "readOnly"                            | false           | "Boolean"
        "readOnly"                        | "readOnly"                            | true            | "Provider<Boolean>"
        "readOnly"                        | "readOnly"                            | false           | "Provider<Boolean>"
        "readOnly"                        | "readOnly.set"                        | true            | "Boolean"
        "readOnly"                        | "readOnly.set"                        | false           | "Boolean"
        "readOnly"                        | "readOnly.set"                        | true            | "Provider<Boolean>"
        "readOnly"                        | "readOnly.set"                        | false           | "Provider<Boolean>"
        "readOnly"                        | "setReadOnly"                         | true            | "Boolean"
        "readOnly"                        | "setReadOnly"                         | false           | "Boolean"
        "readOnly"                        | "setReadOnly"                         | true            | "Provider<Boolean>"
        "readOnly"                        | "setReadOnly"                         | false           | "Provider<Boolean>"

        "ignoreProfilesWithDifferentName" | "ignoreProfilesWithDifferentName"     | true            | "Boolean"
        "ignoreProfilesWithDifferentName" | "ignoreProfilesWithDifferentName"     | false           | "Boolean"
        "ignoreProfilesWithDifferentName" | "ignoreProfilesWithDifferentName"     | true            | "Provider<Boolean>"
        "ignoreProfilesWithDifferentName" | "ignoreProfilesWithDifferentName"     | false           | "Provider<Boolean>"
        "ignoreProfilesWithDifferentName" | "ignoreProfilesWithDifferentName.set" | true            | "Boolean"
        "ignoreProfilesWithDifferentName" | "ignoreProfilesWithDifferentName.set" | false           | "Boolean"
        "ignoreProfilesWithDifferentName" | "ignoreProfilesWithDifferentName.set" | true            | "Provider<Boolean>"
        "ignoreProfilesWithDifferentName" | "ignoreProfilesWithDifferentName.set" | false           | "Provider<Boolean>"
        "ignoreProfilesWithDifferentName" | "setIgnoreProfilesWithDifferentName"  | true            | "Boolean"
        "ignoreProfilesWithDifferentName" | "setIgnoreProfilesWithDifferentName"  | false           | "Boolean"
        "ignoreProfilesWithDifferentName" | "setIgnoreProfilesWithDifferentName"  | true            | "Provider<Boolean>"
        "ignoreProfilesWithDifferentName" | "setIgnoreProfilesWithDifferentName"  | false           | "Provider<Boolean>"

        "destinationDir"                  | "destinationDir"                      | "/some/path/1"  | "File"
        "destinationDir"                  | "destinationDir"                      | "/some/path/2"  | "Provider<Directory>"
        "destinationDir"                  | "destinationDir.set"                  | "/some/path/3"  | "File"
        "destinationDir"                  | "destinationDir.set"                  | "/some/path/4"  | "Provider<Directory>"
        "destinationDir"                  | "setDestinationDir"                   | "/some/path/5"  | "File"
        "destinationDir"                  | "setDestinationDir"                   | "/some/path/6"  | "Provider<Directory>"

        value = wrapValueBasedOnType(rawValue, type)
        expectedValue = rawValue
    }

    @Issue("https://github.com/wooga/atlas-build-unity/issues/38")
    def "task is never up-to-date"() {
        given: "call import tasks once"
        def r = runTasks(testTaskName)

        when: "no parameter changes"
        def result = runTasksSuccessfully(testTaskName)

        then:
        !result.wasUpToDate(testTaskName)
    }
}
