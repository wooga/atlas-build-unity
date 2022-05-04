package wooga.gradle.fastlane.models

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional


trait FastLaneTaskSpec extends FastLaneSpec {

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
}
