package wooga.gradle.macOS.security

import com.wooga.gradle.test.IntegrationSpec
import com.wooga.security.Domain
import wooga.gradle.macOS.security.tasks.SecuritySetKeychainSearchList

abstract class SecurityIntegrationSpec extends IntegrationSpec {

    def setup() {
        def gradleVersion = System.getenv("GRADLE_VERSION")
        if (gradleVersion) {
            this.gradleVersion = gradleVersion
            fork = true
        }
    }

    static wrapValueFallback = { Object rawValue, String type, Closure<String> fallback ->
        switch (type) {
            case Domain.simpleName:
                return "${Domain.class.name}.valueOf('${rawValue.toString()}')"
            case SecuritySetKeychainSearchList.Action.simpleName:
                return "${SecuritySetKeychainSearchList.Action.class.name}.valueOf('${rawValue.toString()}')"
            default:
                return rawValue.toString()
        }
    }


}
