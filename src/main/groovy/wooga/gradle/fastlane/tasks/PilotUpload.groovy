/*
 * Copyright 2018-2020 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.fastlane.tasks

import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SkipWhenEmpty

class PilotUpload extends AbstractFastlaneTask {

    @SkipWhenEmpty
    @InputFiles
    protected FileCollection getInputFiles() {
        if (ipa.present) {
            return project.files(ipa)
        }
        project.files()
    }

    private final RegularFileProperty ipa

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

    PilotUpload ipa(File value) {
        setIpa(value)
        this
    }

    PilotUpload ipa(Provider<RegularFile> value) {
        setIpa(value)
        this
    }

    private final Property<String> appIdentifier

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

    PilotUpload appIdentifier(String value) {
        setAppIdentifier(value)
        this
    }

    PilotUpload appIdentifier(Provider<String> value) {
        setAppIdentifier(value)
        this
    }

    private final Property<String> teamId

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

    PilotUpload teamId(String value) {
        setTeamId(value)
        this
    }

    PilotUpload teamId(Provider<String> value) {
        setTeamId(value)
        this
    }

    private final Property<String> teamName

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

    PilotUpload teamName(String value) {
        setTeamName(value)
        this
    }

    PilotUpload teamName(Provider<String> value) {
        setTeamName(value)
        this
    }

    private final Property<String> username

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

    PilotUpload username(String value) {
        setUsername(value)
        this
    }

    PilotUpload username(Provider<String> value) {
        setUsername(value)
        this
    }

    private final Property<String> password

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

    PilotUpload password(String value) {
        setPassword(value)
        this
    }

    PilotUpload password(Provider<String> value) {
        setPassword(value)
        this
    }

    private final Property<String> devPortalTeamId

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

    PilotUpload devPortalTeamId(String value) {
        setDevPortalTeamId(value)
        this
    }

    PilotUpload devPortalTeamId(Provider<String> value) {
        setDevPortalTeamId(value)
        this
    }

    private final Property<String> itcProvider

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

    PilotUpload itcProvider(String value) {
        setItcProvider(value)
        this
    }

    PilotUpload itcProvider(Provider<String> value) {
        setItcProvider(value)
        this
    }

    private final Property<Boolean> skipSubmission

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

    PilotUpload skipSubmission(Boolean value) {
        setSkipSubmission(value)
        this
    }

    PilotUpload skipSubmission(Provider<Boolean> value) {
        setSkipSubmission(value)
        this
    }

    private final Property<Boolean> skipWaitingForBuildProcessing

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

    PilotUpload skipWaitingForBuildProcessing(Boolean value) {
        setSkipWaitingForBuildProcessing(value)
        this
    }

    PilotUpload skipWaitingForBuildProcessing(Provider<Boolean> value) {
        setSkipWaitingForBuildProcessing(value)
        this
    }

    private final Provider<List<String>> arguments

    @Input
    Provider<List<String>> getArguments() {
        arguments
    }

    private final Provider<Map<String, String>> environment

    @Input
    Provider<Map<String, String>> getEnvironment() {
        environment
    }

    PilotUpload() {
        ipa = project.objects.fileProperty()
        appIdentifier = project.objects.property(String)
        teamId = project.objects.property(String)
        devPortalTeamId = project.objects.property(String)
        teamName = project.objects.property(String)
        username = project.objects.property(String)
        password = project.objects.property(String)
        itcProvider = project.objects.property(String)
        skipSubmission = project.objects.property(Boolean)
        skipWaitingForBuildProcessing = project.objects.property(Boolean)

        outputs.upToDateWhen(new Spec<Task>() {
            @Override
            boolean isSatisfiedBy(Task task) {
                false
            }
        })

        environment = project.provider({
            Map<String, String> environment = [:]

            if (password.isPresent()) {
                environment['FASTLANE_PASSWORD'] = password.get()
            }

            environment as Map<String, String>
        })

        arguments = project.provider({
            List<String> arguments = new ArrayList<String>()

            arguments << "pilot" << "upload"

            if (username.present) {
                arguments << "--username" << username.get()
            }

            if (teamId.present) {
                arguments << "--team_id" << teamId.get()
            }

            if (teamName.present) {
                arguments << "--team_name" << teamName.get()
            }

            if (devPortalTeamId.present) {
                arguments << "--dev_portal_team_id" << devPortalTeamId.get()
            }

            if (appIdentifier.present) {
                arguments << "--app_identifier" << appIdentifier.get()
            }

            if (itcProvider.present) {
                arguments << "--itc_provider" << itcProvider.get()
            }

            if (apiKeyPath.present) {
                arguments << "--api-key-path" << apiKeyPath.get().asFile.path
            }

            arguments << "--skip_submission" << (skipSubmission.present && skipSubmission.get()).toString()
            arguments << "--skip_waiting_for_build_processing" << (skipWaitingForBuildProcessing.present && skipWaitingForBuildProcessing.get()).toString()
            arguments << "--ipa" << ipa.get().asFile.path

            if (additionalArguments.present) {
                additionalArguments.get().each {
                    arguments << it
                }
            }

            arguments
        })
    }
}
