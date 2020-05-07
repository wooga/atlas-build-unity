package wooga.gradle.build.unity.ios.tasks

import spock.lang.Requires
import spock.lang.Unroll

@Requires({ os.macOs })
class PublishTestFlightSpec extends FastlaneSpec {

    def setup() {
        def ipaFile = File.createTempFile("mockIpa", ".ipa")

        buildFile << """
            task publishTestFlight(type: wooga.gradle.build.unity.ios.tasks.PublishTestFlight) {
                appIdentifier = "com.test.testapp"
                teamId = "fakeTeamId"
                ipa = file("${ipaFile.path}")
            }
        """.stripIndent()
    }

    @Unroll
    def "task :#taskToRun executes fastlane #fastlaneCommand #fastlaneSubCommand"() {
        given: "ipa path"
        def ipaFile = File.createTempFile("mockIpa", ".ipa")

        and: "a configured task"
        buildFile << """
            ${taskToRun} {
                ipa = file("${ipaFile.path}")
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(taskToRun)

        then:
        result.standardOutput.readLines().any { it.matches("${fastlaneCommand} ${fastlaneSubCommand}.*? --ipa ${ipaFile.path}") }

        where:
        taskToRun = "publishTestFlight"
        fastlaneCommand = "pilot"
        fastlaneSubCommand = "upload"
    }

    @Unroll
    def "task :#taskToRun accepts input #parameter with #method and type #type"() {
        given: "task with configured properties"
        buildFile << """
            ${taskToRun} {
                ${method}(${value})
            }
        """.stripIndent()

        and:
        if (parameter == "ipa") {
            createFile(rawValue.toString(), projectDir)
        }

        when:
        def result = runTasksSuccessfully(taskToRun)

        then:
        result.standardOutput.contains(expectedCommandlineSwitch.replace("#{value_path}", new File(projectDir, rawValue.toString()).path))

        where:
        parameter                       | rawValue              | type       | useSetter | expectedCommandlineSwitchRaw
        "appIdentifier"                 | "com.test.app2"       | 'String'   | true      | "--app_identifier #{value}"
        "appIdentifier"                 | "com.test.app3"       | 'String'   | false     | "--app_identifier #{value}"
        "appIdentifier"                 | "com.test.app4"       | 'Closure'  | true      | "--app_identifier #{value}"
        "appIdentifier"                 | "com.test.app5"       | 'Closure'  | false     | "--app_identifier #{value}"
        "appIdentifier"                 | "com.test.app6"       | 'Callable' | true      | "--app_identifier #{value}"
        "appIdentifier"                 | "com.test.app7"       | 'Callable' | false     | "--app_identifier #{value}"
        "appIdentifier"                 | "com.test.app8"       | 'Object'   | true      | "--app_identifier #{value}"
        "appIdentifier"                 | "com.test.app9"       | 'Object'   | false     | "--app_identifier #{value}"

        "teamId"                        | "1234561"             | 'String'   | true      | "--team_id #{value}"
        "teamId"                        | "1234562"             | 'String'   | false     | "--team_id #{value}"
        "teamId"                        | "1234563"             | 'Closure'  | true      | "--team_id #{value}"
        "teamId"                        | "1234564"             | 'Closure'  | false     | "--team_id #{value}"
        "teamId"                        | "1234565"             | 'Callable' | true      | "--team_id #{value}"
        "teamId"                        | "1234566"             | 'Callable' | false     | "--team_id #{value}"
        "teamId"                        | "1234567"             | 'Object'   | true      | "--team_id #{value}"
        "teamId"                        | "1234568"             | 'Object'   | false     | "--team_id #{value}"

        "teamName"                      | "testName"            | 'String'   | true      | "--team_name #{value}"
        "teamName"                      | "testName"            | 'String'   | false     | "--team_name #{value}"
        "teamName"                      | "testName"            | 'Closure'  | true      | "--team_name #{value}"
        "teamName"                      | "testName"            | 'Closure'  | false     | "--team_name #{value}"
        "teamName"                      | "testName"            | 'Callable' | true      | "--team_name #{value}"
        "teamName"                      | "testName"            | 'Callable' | false     | "--team_name #{value}"
        "teamName"                      | "testName"            | 'Object'   | true      | "--team_name #{value}"
        "teamName"                      | "testName"            | 'Object'   | false     | "--team_name #{value}"

        "itcProvider"                   | "testItcProvider"     | 'String'   | true      | "--itc_provider #{value}"
        "itcProvider"                   | "testItcProvider"     | 'String'   | false     | "--itc_provider #{value}"
        "itcProvider"                   | "testItcProvider"     | 'Closure'  | true      | "--itc_provider #{value}"
        "itcProvider"                   | "testItcProvider"     | 'Closure'  | false     | "--itc_provider #{value}"
        "itcProvider"                   | "testItcProvider"     | 'Callable' | true      | "--itc_provider #{value}"
        "itcProvider"                   | "testItcProvider"     | 'Callable' | false     | "--itc_provider #{value}"
        "itcProvider"                   | "testItcProvider"     | 'Object'   | true      | "--itc_provider #{value}"
        "itcProvider"                   | "testItcProvider"     | 'Object'   | false     | "--itc_provider #{value}"

        "devPortalTeamId"               | "1234561"             | 'String'   | true      | "--dev_portal_team_id #{value}"
        "devPortalTeamId"               | "1234562"             | 'String'   | false     | "--dev_portal_team_id #{value}"
        "devPortalTeamId"               | "1234563"             | 'Closure'  | true      | "--dev_portal_team_id #{value}"
        "devPortalTeamId"               | "1234564"             | 'Closure'  | false     | "--dev_portal_team_id #{value}"
        "devPortalTeamId"               | "1234565"             | 'Callable' | true      | "--dev_portal_team_id #{value}"
        "devPortalTeamId"               | "1234566"             | 'Callable' | false     | "--dev_portal_team_id #{value}"
        "devPortalTeamId"               | "1234567"             | 'Object'   | true      | "--dev_portal_team_id #{value}"
        "devPortalTeamId"               | "1234568"             | 'Object'   | false     | "--dev_portal_team_id #{value}"

        "username"                      | "tester1"             | 'String'   | true      | "--username #{value}"
        "username"                      | "tester2"             | 'String'   | false     | "--username #{value}"
        "username"                      | "tester3"             | 'Closure'  | true      | "--username #{value}"
        "username"                      | "tester4"             | 'Closure'  | false     | "--username #{value}"
        "username"                      | "tester5"             | 'Callable' | true      | "--username #{value}"
        "username"                      | "tester6"             | 'Callable' | false     | "--username #{value}"
        "username"                      | "tester7"             | 'Object'   | true      | "--username #{value}"
        "username"                      | "tester8"             | 'Object'   | false     | "--username #{value}"

        "password"                      | "pass1"               | 'String'   | true      | "FASTLANE_PASSWORD=#{value}"
        "password"                      | "pass2"               | 'String'   | false     | "FASTLANE_PASSWORD=#{value}"
        "password"                      | "pass3"               | 'Closure'  | true      | "FASTLANE_PASSWORD=#{value}"
        "password"                      | "pass4"               | 'Closure'  | false     | "FASTLANE_PASSWORD=#{value}"
        "password"                      | "pass5"               | 'Callable' | true      | "FASTLANE_PASSWORD=#{value}"
        "password"                      | "pass6"               | 'Callable' | false     | "FASTLANE_PASSWORD=#{value}"
        "password"                      | "pass7"               | 'Object'   | true      | "FASTLANE_PASSWORD=#{value}"
        "password"                      | "pass8"               | 'Object'   | false     | "FASTLANE_PASSWORD=#{value}"

        "skipSubmission"                | true                  | 'Boolean'  | true      | "--skip_submission true"
        "skipSubmission"                | true                  | 'Boolean'  | false     | "--skip_submission true"
        "skipSubmission"                | true                  | 'Closure'  | true      | "--skip_submission true"
        "skipSubmission"                | true                  | 'Closure'  | false     | "--skip_submission true"
        "skipSubmission"                | true                  | 'Callable' | true      | "--skip_submission true"
        "skipSubmission"                | true                  | 'Callable' | false     | "--skip_submission true"
        "skipSubmission"                | false                 | 'Boolean'  | true      | "--skip_submission false"
        "skipSubmission"                | false                 | 'Boolean'  | false     | "--skip_submission false"
        "skipSubmission"                | false                 | 'Closure'  | true      | "--skip_submission false"
        "skipSubmission"                | false                 | 'Closure'  | false     | "--skip_submission false"
        "skipSubmission"                | false                 | 'Callable' | true      | "--skip_submission false"
        "skipSubmission"                | false                 | 'Callable' | false     | "--skip_submission false"

        "skipWaitingForBuildProcessing" | true                  | 'Boolean'  | true      | "--skip_waiting_for_build_processing true"
        "skipWaitingForBuildProcessing" | true                  | 'Boolean'  | false     | "--skip_waiting_for_build_processing true"
        "skipWaitingForBuildProcessing" | true                  | 'Closure'  | true      | "--skip_waiting_for_build_processing true"
        "skipWaitingForBuildProcessing" | true                  | 'Closure'  | false     | "--skip_waiting_for_build_processing true"
        "skipWaitingForBuildProcessing" | true                  | 'Callable' | true      | "--skip_waiting_for_build_processing true"
        "skipWaitingForBuildProcessing" | true                  | 'Callable' | false     | "--skip_waiting_for_build_processing true"
        "skipWaitingForBuildProcessing" | false                 | 'Boolean'  | true      | "--skip_waiting_for_build_processing false"
        "skipWaitingForBuildProcessing" | false                 | 'Boolean'  | false     | "--skip_waiting_for_build_processing false"
        "skipWaitingForBuildProcessing" | false                 | 'Closure'  | true      | "--skip_waiting_for_build_processing false"
        "skipWaitingForBuildProcessing" | false                 | 'Closure'  | false     | "--skip_waiting_for_build_processing false"
        "skipWaitingForBuildProcessing" | false                 | 'Callable' | true      | "--skip_waiting_for_build_processing false"
        "skipWaitingForBuildProcessing" | false                 | 'Callable' | false     | "--skip_waiting_for_build_processing false"

        "ipa"                           | "build/out1/test.ipa" | 'String'   | true      | "--ipa #{value_path}"
        "ipa"                           | "build/out2/test.ipa" | 'String'   | false     | "--ipa #{value_path}"
        "ipa"                           | "build/out3/test.ipa" | 'File'     | true      | "--ipa #{value_path}"
        "ipa"                           | "build/out4/test.ipa" | 'File'     | false     | "--ipa #{value_path}"
        "ipa"                           | "build/out5/test.ipa" | 'Closure'  | true      | "--ipa #{value_path}"
        "ipa"                           | "build/out6/test.ipa" | 'Closure'  | false     | "--ipa #{value_path}"

        taskToRun = "publishTestFlight"
        value = wrapValueBasedOnType(rawValue, type)
        method = (useSetter) ? "set${parameter.capitalize()}" : parameter
        expectedCommandlineSwitch = expectedCommandlineSwitchRaw.replace("#{value}", rawValue.toString())
    }
}
