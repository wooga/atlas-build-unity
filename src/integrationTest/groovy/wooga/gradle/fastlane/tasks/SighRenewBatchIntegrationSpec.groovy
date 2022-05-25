package wooga.gradle.fastlane.tasks

import spock.lang.Requires
import spock.lang.Unroll

@Requires({ os.macOs })
class SighRenewBatchIntegrationSpec extends SighRenewIntegrationSpec {
    String testTaskName = "sighRenewBatch"
    Class taskType = SighRenewBatch

    String workingFastlaneTaskConfig = """
        task("${testTaskName}", type: ${taskType.name}) {
            appIdentifier = 'test'
            teamId = "fakeTeamId"
            destinationDir = file('build')
            profiles = ["foo.bar.baz":"fooBar"]
        }
        """.stripIndent()

    def "imports multiple profiles"() {
        given: "set multiple profiles"
        buildFile << """
        ${testTaskName} {
            profiles = ${wrapValueBasedOnType(profiles, "Map<String,String>")}
        } 
        """

        when:
        def result = runTasksSuccessfully(testTaskName)

        then:
        profiles.each { bundleId, profileName ->
            result.standardOutput.contains("import provisioning profile '${profileName}' for bundleIdentifier '${bundleId}' to file '${profileName}.mobileprovision'")
        }

        where:
        profileNames = ["profile1", "profile2", "profile3"]
        bundleIds = ["net.test.app1", "net.test.app2", "net.test.app3"]
        profiles = [bundleIds, profileNames].transpose().collectEntries()
    }

    @Unroll
    def "imports configured profileName/appIdentifier when set"() {
        given: "set multiple profiles"
        buildFile << """
        ${testTaskName} {
            profiles = ${wrapValueBasedOnType(profiles, "Map<String,String>")}
            appIdentifier = ${appIdentifier ? wrapValueBasedOnType(appIdentifier, String) : null}
            provisioningName = ${provisioningName ? wrapValueBasedOnType(provisioningName, String) : null}
        } 
        """

        when:
        def result = runTasksSuccessfully(testTaskName)

        then:
        result.standardOutput.contains("import ${expectedProfiles.size()} profiles")
        profiles.each { bundleId, profileName ->
            result.standardOutput.contains("import provisioning profile '${profileName}' for bundleIdentifier '${bundleId}' to file '${profileName}.mobileprovision'")
        }

        where:
        provisioningName | appIdentifier | profiles                  || expectedProfiles
        null             | null          | ["foo.bar.baz": "FooBar"] || profiles
        "FooBar"         | "foo.bar.baz" | ["foo.bar.baz": "FooBar"] || profiles
        "FooBar"         | null          | ["foo.bar.baz": "FooBar"] || profiles
        null             | "foo.bar.baz" | ["foo.bar.baz": "FooBar"] || profiles
        "FooBar2"        | "foo.bar.baz" | ["foo.bar.baz": "FooBar"] || ["foo.bar.baz": "FooBar", (appIdentifier): (provisioningName)]
        "BazBar"         | "baz.foo.bar" | ["foo.bar.baz": "FooBar"] || ["foo.bar.baz": "FooBar", (appIdentifier): (provisioningName)]
    }
}
