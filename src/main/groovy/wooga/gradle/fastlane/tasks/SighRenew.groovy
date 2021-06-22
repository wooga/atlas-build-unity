/*
 * Copyright 2020 Wooga GmbH
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
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile

class SighRenew extends AbstractFastlaneTask {

    final Property<String> appIdentifier

    void setAppIdentifier(String value) {
        appIdentifier.set(value)
    }

    void setAppIdentifier(Provider<String> value) {
        appIdentifier.set(value)
    }

    SighRenew appIdentifier(String value) {
        setAppIdentifier(value)
        this
    }

    SighRenew appIdentifier(Provider<String> value) {
        setAppIdentifier(value)
        this
    }

    final Property<String> teamId

    void setTeamId(String value) {
        teamId.set(value)
    }

    void setTeamId(Provider<String> value) {
        teamId.set(value)
    }

    SighRenew teamId(String value) {
        setTeamId(value)
        this
    }

    SighRenew teamId(Provider<String> value) {
        setTeamId(value)
        this
    }

    final Property<String> teamName

    void setTeamName(String value) {
        teamName.set(value)
    }

    void setTeamName(Provider<String> value) {
        teamName.set(value)
    }

    SighRenew teamName(String value) {
        setTeamName(value)
        this
    }

    SighRenew teamName(Provider<String> value) {
        setTeamName(value)
        this
    }

    final Property<String> username

    void setUsername(String value) {
        username.set(value)
    }

    void setUsername(Provider<String> value) {
        username.set(value)
    }

    SighRenew username(String value) {
        setUsername(value)
        this
    }

    SighRenew username(Provider<String> value) {
        setUsername(value)
        this
    }

    final Property<String> password

    void setPassword(String value) {
        password.set(value)
    }

    void setPassword(Provider<String> value) {
        password.set(value)
    }

    SighRenew password(String value) {
        setPassword(value)
        this
    }

    SighRenew password(Provider<String> value) {
        setPassword(value)
        this
    }

    final Property<String> fileName

    void setFileName(String value) {
        fileName.set(value)
    }

    void setFileName(Provider<String> value) {
        fileName.set(value)
    }

    SighRenew fileName(String value) {
        setFileName(value)
        this
    }

    SighRenew fileName(Provider<String> value) {
        setFileName(value)
        this
    }

    final Property<String> provisioningName


    void setProvisioningName(String value) {
        provisioningName.set(value)
    }

    void setProvisioningName(Provider<String> value) {
        provisioningName.set(value)
    }

    SighRenew provisioningName(String value) {
        setProvisioningName(value)
        this
    }

    SighRenew provisioningName(Provider<String> value) {
        setProvisioningName(value)
        this
    }

    final Property<Boolean> adhoc

    void setAdhoc(Boolean value) {
        adhoc.set(value)
    }

    void setAdhoc(Provider<Boolean> value) {
        adhoc.set(value)
    }

    SighRenew adhoc(Boolean value) {
        setAdhoc(value)
        this
    }

    SighRenew adhoc(Provider<Boolean> value) {
        setAdhoc(value)
        this
    }

    final DirectoryProperty destinationDir

    void setDestinationDir(File value) {
        destinationDir.set(value)
    }

    void setDestinationDir(Provider<Directory> value) {
        destinationDir.set(value)
    }

    SighRenew destinationDir(File value) {
        setDestinationDir(value)
        this
    }

    SighRenew destinationDir(Provider<Directory> value) {
        setDestinationDir(value)
        this
    }

    final Property<Boolean> readOnly

    void setReadOnly(Boolean value) {
        readOnly.set(value)
    }

    void setReadOnly(Provider<Boolean> value) {
        readOnly.set(value)
    }

    SighRenew readOnly(Boolean value) {
        setReadOnly(value)
        this
    }

    SighRenew readOnly(Provider<Boolean> value) {
        setReadOnly(value)
        this
    }

    final Property<Boolean> ignoreProfilesWithDifferentName

    void setIgnoreProfilesWithDifferentName(Boolean value) {
        ignoreProfilesWithDifferentName.set(value)
    }

    void setIgnoreProfilesWithDifferentName(Provider<Boolean> value) {
        ignoreProfilesWithDifferentName.set(value)
    }

    SighRenew ignoreProfilesWithDifferentName(Boolean value) {
        setIgnoreProfilesWithDifferentName(value)
        this
    }

    SighRenew ignoreProfilesWithDifferentName(Provider<Boolean> value) {
        setIgnoreProfilesWithDifferentName(value)
        this
    }

    @OutputFile
    final Provider<RegularFile> mobileProvisioningProfile

    @Input
    final Provider<List<String>> arguments

    @Input
    final Provider<Map<String, String>> environment

    SighRenew() {
        super()
        appIdentifier = project.objects.property(String)
        teamId = project.objects.property(String)
        teamName = project.objects.property(String)
        username = project.objects.property(String)
        password = project.objects.property(String)
        fileName = project.objects.property(String)
        provisioningName = project.objects.property(String)
        adhoc = project.objects.property(Boolean)
        readOnly = project.objects.property(Boolean)
        ignoreProfilesWithDifferentName = project.objects.property(Boolean)
        destinationDir = project.objects.directoryProperty()
        mobileProvisioningProfile = destinationDir.file(fileName)

        environment = project.provider({
            Map<String, String> environment = [:]

            if (password.isPresent()) {
                environment['FASTLANE_PASSWORD'] = password.get()
            }

            environment
        })

        outputs.upToDateWhen(new Spec<Task>() {
            @Override
            boolean isSatisfiedBy(Task task) {
                false
            }
        })

        arguments = project.provider({
            List<String> arguments = new ArrayList<String>()

            arguments << "sigh" << "renew"

            if (username.present) {
                arguments << "--username" << username.get()
            }

            if (teamId.present) {
                arguments << "--team_id" << teamId.get()
            }

            if (teamName.present) {
                arguments << "--team_name" << teamName.get()
            }

            arguments << "--app_identifier" << appIdentifier.get()

            if (provisioningName.present) {
                arguments << "--provisioning_name" << provisioningName.get()
            }

            if (apiKeyPath.present) {
                arguments << "--api-key-path" << apiKeyPath.get().asFile.path
            }

            arguments << "--adhoc" << (adhoc.present && adhoc.get()).toString()
            arguments << "--readonly" << (readOnly.present && readOnly.get()).toString()
            arguments << "--ignore_profiles_with_different_name" << (ignoreProfilesWithDifferentName.present && ignoreProfilesWithDifferentName.get()).toString()
            arguments << "--filename" << fileName.get()
            arguments << "--output_path" << destinationDir.get().asFile.path

            if (additionalArguments.present) {
                additionalArguments.get().each {
                    arguments << it
                }
            }

            arguments
        })

        onlyIf(new Spec<SighRenew>() {
            @Override
            boolean isSatisfiedBy(SighRenew task) {
                (task.teamId.present || task.teamName.present) && task.appIdentifier.present
            }
        })
    }
}
