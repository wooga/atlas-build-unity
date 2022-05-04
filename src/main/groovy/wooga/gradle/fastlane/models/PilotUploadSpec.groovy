package wooga.gradle.fastlane.models

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SkipWhenEmpty

trait PilotUploadSpec extends BaseSpec {

    private final RegularFileProperty ipa = objects.fileProperty()

    @Internal
    RegularFileProperty getIpa() {
        ipa
    }

    void setIpa(File value) {
        ipa.set(value)
    }

    void setIpa(Provider<RegularFile> value) {
        ipa.set(value)
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

    private final Property<String> devPortalTeamId = objects.property(String)

    @Internal
    Property<String> getDevPortalTeamId() {
        devPortalTeamId
    }

    void setDevPortalTeamId(String value) {
        devPortalTeamId.set(value)
    }

    void setDevPortalTeamId(Provider<String> value) {
        devPortalTeamId.set(value)
    }

    private final Property<String> itcProvider = objects.property(String)

    @Internal
    Property<String> getItcProvider() {
        itcProvider
    }

    void setItcProvider(String value) {
        itcProvider.set(value)
    }

    void setItcProvider(Provider<String> value) {
        itcProvider.set(value)
    }

    private final Property<Boolean> skipSubmission = objects.property(Boolean)

    @Internal
    Property<Boolean> getSkipSubmission() {
        skipSubmission
    }

    void setSkipSubmission(Boolean value) {
        skipSubmission.set(value)
    }

    void setSkipSubmission(Provider<Boolean> value) {
        skipSubmission.set(value)
    }

    private final Property<Boolean> skipWaitingForBuildProcessing = objects.property(Boolean)

    @Internal
    Property<Boolean> getSkipWaitingForBuildProcessing() {
        skipWaitingForBuildProcessing
    }

    void setSkipWaitingForBuildProcessing(Boolean value) {
        skipWaitingForBuildProcessing.set(value)
    }

    void setSkipWaitingForBuildProcessing(Provider<Boolean> value) {
        skipWaitingForBuildProcessing.set(value)
    }
}
