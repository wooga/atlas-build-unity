package wooga.gradle.macOS.security.tasks

import jdk.nashorn.internal.ir.annotations.Ignore
import spock.lang.Requires

@Requires({ os.macOs && env['ATLAS_BUILD_UNITY_IOS_EXECUTE_KEYCHAIN_SPEC'] == 'YES' })
class SecurityResetKeychainSearchListIntegrationSpec extends KeychainSearchListSpec<SecurityResetKeychainSearchList> {

    @Ignore
    def "can reset keychain lookup list"() {
        given: "a default keychain"
        def defaultLookupList = keychainSearchList.collect()

        and: "value in environment to opt in for reset"
        environmentVariables.set("ATLAS_BUILD_UNITY_IOS_RESET_KEYCHAINS", "YES")

        and: "some keychains added"
        keychainSearchList.addAll(keychains.collect { it.location })

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        !result.wasSkipped(subjectUnderTestName)
        keychainSearchList.collect() == defaultLookupList

        where:
        keychains                                       | message
        [buildKeychain, buildKeychain2, buildKeychain3] | "multiple keychains"
    }

    def "skip reset if ATLAS_BUILD_UNITY_IOS_RESET_KEYCHAINS is not set to YES"() {
        given: "environment setting disabled"
        environmentVariables.set('ATLAS_BUILD_UNITY_IOS_RESET_KEYCHAINS', "NO")
        fork = true

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.wasSkipped(subjectUnderTestName)
    }
}
