/*
 * Copyright 2019 Wooga GmbH
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

import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Requires
import spock.lang.Unroll
import wooga.gradle.build.IntegrationSpec

/**
 * The test examples in this class are not 100% integration/functional tests.
 *
 * We can't run the real fastlane and connect to apple because there is no easy way to setup and maintain a test app and
 * account with necessary credentials. We only test the invocation of fastlane and its parameters.
 */
@Requires({ os.macOs })
class ImportProvisioningProfileSpec extends FastlaneSpec {

    def setup() {
        buildFile << """
            task importProfiles(type: wooga.gradle.build.unity.ios.tasks.ImportProvisioningProfile) {
                appIdentifier = "com.test.testapp"
                teamId = "fakeTeamId"
                profileName = "signing.mobileprovisioning"
                destinationDir = file("build")
            }
        """.stripIndent()
    }

    @Issue("https://github.com/wooga/atlas-build-unity/issues/38")
    def "task :#taskToRun is never up-to-date"() {
        given: "call import tasks once"
        def r = runTasks(taskToRun)

        when: "no parameter changes"
        def result = runTasksSuccessfully(taskToRun)

        then:
        !result.wasUpToDate(taskToRun)

        where:
        taskToRun = "importProfiles"
    }

    // Test fails with current implementation.
    // When we switch to gradle provider API we can fix this usecase.
    @Ignore()
    def "task :#taskToRun accepts input #parameter is unset when not configured"() {
        given: "task adhoc property is not configured"

        when:
        def result = runTasksSuccessfully(taskToRun)

        then:
        !outputContains(result, commandlineFlag)

        where:
        parameter | commandlineFlag
        "adhoc"   | "--adhoc"

        taskToRun = "importProfiles"
    }

    @Unroll
    def "task :#taskToRun accepts input #parameter with #method and type #type"() {
        given: "task with configured properties"
        buildFile << """
            ${taskToRun} {
                ${method}(${value})
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(taskToRun)

        then:
        result.standardOutput.contains(expectedCommandlineSwitch.replace("#{value_path}", new File(projectDir, rawValue.toString()).path))

        where:
        parameter           | rawValue                | type       | useSetter | expectedCommandlineSwitchRaw
        "appIdentifier"     | "com.test.app2"         | 'String'   | true      | "--app_identifier #{value}"
        "appIdentifier"     | "com.test.app3"         | 'String'   | false     | "--app_identifier #{value}"
        "appIdentifier"     | "com.test.app4"         | 'Closure'  | true      | "--app_identifier #{value}"
        "appIdentifier"     | "com.test.app5"         | 'Closure'  | false     | "--app_identifier #{value}"
        "appIdentifier"     | "com.test.app6"         | 'Callable' | true      | "--app_identifier #{value}"
        "appIdentifier"     | "com.test.app7"         | 'Callable' | false     | "--app_identifier #{value}"
        "appIdentifier"     | "com.test.app8"         | 'Object'   | true      | "--app_identifier #{value}"
        "appIdentifier"     | "com.test.app9"         | 'Object'   | false     | "--app_identifier #{value}"

        "teamId"            | "1234561"               | 'String'   | true      | "--team_id #{value}"
        "teamId"            | "1234562"               | 'String'   | false     | "--team_id #{value}"
        "teamId"            | "1234563"               | 'Closure'  | true      | "--team_id #{value}"
        "teamId"            | "1234564"               | 'Closure'  | false     | "--team_id #{value}"
        "teamId"            | "1234565"               | 'Callable' | true      | "--team_id #{value}"
        "teamId"            | "1234566"               | 'Callable' | false     | "--team_id #{value}"
        "teamId"            | "1234567"               | 'Object'   | true      | "--team_id #{value}"
        "teamId"            | "1234568"               | 'Object'   | false     | "--team_id #{value}"

        "username"          | "tester1"               | 'String'   | true      | "--username #{value}"
        "username"          | "tester2"               | 'String'   | false     | "--username #{value}"
        "username"          | "tester3"               | 'Closure'  | true      | "--username #{value}"
        "username"          | "tester4"               | 'Closure'  | false     | "--username #{value}"
        "username"          | "tester5"               | 'Callable' | true      | "--username #{value}"
        "username"          | "tester6"               | 'Callable' | false     | "--username #{value}"
        "username"          | "tester7"               | 'Object'   | true      | "--username #{value}"
        "username"          | "tester8"               | 'Object'   | false     | "--username #{value}"

        "password"          | "pass1"                 | 'String'   | true      | "FASTLANE_PASSWORD=#{value}"
        "password"          | "pass2"                 | 'String'   | false     | "FASTLANE_PASSWORD=#{value}"
        "password"          | "pass3"                 | 'Closure'  | true      | "FASTLANE_PASSWORD=#{value}"
        "password"          | "pass4"                 | 'Closure'  | false     | "FASTLANE_PASSWORD=#{value}"
        "password"          | "pass5"                 | 'Callable' | true      | "FASTLANE_PASSWORD=#{value}"
        "password"          | "pass6"                 | 'Callable' | false     | "FASTLANE_PASSWORD=#{value}"
        "password"          | "pass7"                 | 'Object'   | true      | "FASTLANE_PASSWORD=#{value}"
        "password"          | "pass8"                 | 'Object'   | false     | "FASTLANE_PASSWORD=#{value}"

        "profileName"       | "sign1.mobileprovision" | 'String'   | true      | "--filename #{value}"
        "profileName"       | "sign2.mobileprovision" | 'String'   | false     | "--filename #{value}"

        "provisioningName" | "profile_1"             | 'String'   | true      | "--provisioning_name #{value}"
        "provisioningName" | "profile_2"             | 'String'   | false     | "--provisioning_name #{value}"
        "provisioningName" | "profile_3"             | 'Closure'  | true      | "--provisioning_name #{value}"
        "provisioningName" | "profile_4"             | 'Closure'  | false     | "--provisioning_name #{value}"
        "provisioningName" | "profile_5"             | 'Callable' | true      | "--provisioning_name #{value}"
        "provisioningName" | "profile_6"             | 'Callable' | false     | "--provisioning_name #{value}"
        "provisioningName" | "profile_7"             | 'Object'   | true      | "--provisioning_name #{value}"
        "provisioningName" | "profile_8"             | 'Object'   | false     | "--provisioning_name #{value}"

        "adhoc"            | true                    | 'Boolean'  | true      | "--adhoc true"
        "adhoc"            | true                    | 'Boolean'  | false     | "--adhoc true"
        "adhoc"            | true                    | 'Closure'  | true      | "--adhoc true"
        "adhoc"            | true                    | 'Closure'  | false     | "--adhoc true"
        "adhoc"            | true                    | 'Callable' | true      | "--adhoc true"
        "adhoc"            | true                    | 'Callable' | false     | "--adhoc true"
        "adhoc"            | false                   | 'Boolean'  | true      | "--adhoc false"
        "adhoc"            | false                   | 'Boolean'  | false     | "--adhoc false"
        "adhoc"            | false                   | 'Closure'  | true      | "--adhoc false"
        "adhoc"            | false                   | 'Closure'  | false     | "--adhoc false"
        "adhoc"            | false                   | 'Callable' | true      | "--adhoc false"
        "adhoc"            | false                   | 'Callable' | false     | "--adhoc false"

        "destinationDir"   | "build/out1"            | 'String'   | true      | "--output_path #{value_path}"
        "destinationDir"   | "build/out2"            | 'String'   | false     | "--output_path #{value_path}"
        "destinationDir"   | "build/out3"            | 'File'     | true      | "--output_path #{value_path}"
        "destinationDir"   | "build/out4"            | 'File'     | false     | "--output_path #{value_path}"
        "destinationDir"   | "build/out5"            | 'Closure'  | true      | "--output_path #{value_path}"
        "destinationDir"   | "build/out6"            | 'Closure'  | false     | "--output_path #{value_path}"

        taskToRun = "importProfiles"
        value = wrapValueBasedOnType(rawValue, type)
        method = (useSetter) ? "set${parameter.capitalize()}" : parameter
        expectedCommandlineSwitch = expectedCommandlineSwitchRaw.replace("#{value}", rawValue.toString())
    }
}
