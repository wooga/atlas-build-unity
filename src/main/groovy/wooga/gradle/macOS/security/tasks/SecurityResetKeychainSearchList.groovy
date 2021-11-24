package wooga.gradle.macOS.security.tasks

import org.gradle.api.specs.Spec
import org.gradle.api.tasks.TaskAction

class SecurityResetKeychainSearchList extends AbstractSecurityKeychainSearchListTask {

    SecurityResetKeychainSearchList() {
        super()
        onlyIf(new Spec<SecurityResetKeychainSearchList>() {
            @Override
            boolean isSatisfiedBy(SecurityResetKeychainSearchList task) {
                return System.getenv("ATLAS_BUILD_UNITY_IOS_RESET_KEYCHAINS") == "YES"
            }
        })
    }

    @TaskAction
    protected void reset() {
        searchList.reset()
    }
}
