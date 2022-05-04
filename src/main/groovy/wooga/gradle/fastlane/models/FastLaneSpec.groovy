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

    private final Property<String> appIdentifier = objects.property(String)

    @Internal
    Property<String> getAppIdentifier() {
        appIdentifier
    }

    void setAppIdentifier(String value) {
        appIdentifier.set(value)
    }

    void setAppIdentifier(Provider<String> value) {
        appIdentifier.set(value)
    }

    private final Property<String> teamId = objects.property(String)

    @Internal
    Property<String> getTeamId() {
        teamId
    }

    void setTeamId(String value) {
        teamId.set(value)
    }

    void setTeamId(Provider<String> value) {
        teamId.set(value)
    }

    private final Property<String> teamName = objects.property(String)

    @Internal
    Property<String> getTeamName() {
        teamName
    }

    void setTeamName(String value) {
        teamName.set(value)
    }

    void setTeamName(Provider<String> value) {
        teamName.set(value)
    }

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
}
