package wooga.gradle.fastlane.models


import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal

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
