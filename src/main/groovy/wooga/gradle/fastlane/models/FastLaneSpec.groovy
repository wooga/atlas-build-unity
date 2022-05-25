package wooga.gradle.fastlane.models

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

trait FastLaneSpec extends BaseSpec {

    private final Property<String> username = objects.property(String)

    @Internal
    Property<String> getUsername() {
        username
    }

    void setUsername(String value) {
        username.set(value)
    }

    void setUsername(Provider<String> value) {
        username.set(value)
    }

    private final Property<String> password = objects.property(String)

    @Internal
    Property<String> getPassword() {
        password
    }

    void setPassword(String value) {
        password.set(value)
    }

    void setPassword(Provider<String> value) {
        password.set(value)
    }

    private final RegularFileProperty apiKeyPath = objects.fileProperty()

    @Optional
    @InputFile
    RegularFileProperty getApiKeyPath() {
        apiKeyPath
    }

    void setApiKeyPath(File value) {
        apiKeyPath.set(value)
    }

    void setApiKeyPath(Provider<RegularFile> value) {
        apiKeyPath.set(value)
    }

    private final Property<Boolean> skip2faUpgrade = objects.property(Boolean)

    @Internal
    Property<Boolean> getSkip2faUpgrade() {
        skip2faUpgrade
    }

    void setSkip2faUpgrade(Provider<Boolean> value) {
        skip2faUpgrade.set(value)
    }

    void setSkip2faUpgrade(Boolean value) {
        skip2faUpgrade.set(value)
    }

}
