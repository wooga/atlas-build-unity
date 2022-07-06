package wooga.gradle.macOS.security

import com.wooga.security.Domain
import wooga.gradle.build.IntegrationSpec
import wooga.gradle.macOS.security.tasks.SecuritySetKeychainSearchList

abstract class SecurityIntegrationSpec extends IntegrationSpec {
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
