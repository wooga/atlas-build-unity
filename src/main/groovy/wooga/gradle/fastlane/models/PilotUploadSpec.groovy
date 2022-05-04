package wooga.gradle.fastlane.models

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
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
