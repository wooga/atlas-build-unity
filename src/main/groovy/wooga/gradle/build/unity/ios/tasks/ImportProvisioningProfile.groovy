/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.build.unity.ios.tasks

import org.gradle.api.file.FileCollection
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

class ImportProvisioningProfile extends ConventionTask {

    @Input
    String appIdentifier

    @Input
    String teamId

    @Optional
    @Input
    String username

    @Optional
    @Input
    String password

    private Object mobileProvisioningProfile

    @OutputFile
    File getMobileProvisioningProfile() {
        project.files(mobileProvisioningProfile).singleFile
    }

    @OutputFiles
    FileCollection getOutputFiles() {
        project.files(mobileProvisioningProfile)
    }

    void setMobileProvisioningProfile(Object profile) {
        mobileProvisioningProfile = profile
    }

    ImportProvisioningProfile mobileProvisioningProfile(Object profile) {
        mobileProvisioningProfile = profile
        this
    }

    @TaskAction
    protected void importProfilesl() {
        project.exec {
            executable "fastlane"
            args "sigh"
            if (password) {
                environment('FASTLANE_PASSWORD', password)
            }

            if (username) {
                args "--username", username
            }

            args "--team_id", teamId
            args "--app_identifier", appIdentifier
            args "--filename", getMobileProvisioningProfile().getName()
            args "--output_path", getMobileProvisioningProfile().parentFile
        }
    }
}
