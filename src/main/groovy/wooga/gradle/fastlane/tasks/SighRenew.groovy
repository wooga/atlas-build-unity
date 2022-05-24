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

import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFiles
import wooga.gradle.fastlane.models.SighRenewSpec

class SighRenew extends AbstractFastlaneTask implements SighRenewSpec {

    SighRenew() {
        super()

        internalArguments = project.provider({
            List<String> arguments = new ArrayList<String>()

            arguments << "sigh" << "renew"

            addDefaultArguments(arguments)

            if (provisioningName.present) {
                arguments << "--provisioning_name" << provisioningName.get()
            }

            arguments << "--adhoc" << (adhoc.present && adhoc.get()).toString()
            arguments << "--readonly" << (readOnly.present && readOnly.get()).toString()
            arguments << "--ignore_profiles_with_different_name" << (ignoreProfilesWithDifferentName.present && ignoreProfilesWithDifferentName.get()).toString()
            arguments << "--filename" << fileName.get()
            arguments << "--output_path" << destinationDir.get().asFile.path

            arguments
        })

        onlyIf(new Spec<SighRenew>() {
            @Override
            boolean isSatisfiedBy(SighRenew task) {
                (task.teamId.present || task.teamName.present) && task.appIdentifier.present
            }
        })
    }

    @Internal
    Provider<RegularFile> getMobileProvisioningProfile() {
        destinationDir.file(fileName)
    }

    @OutputFiles
    protected FileCollection getOutputFiles() {
        project.files(mobileProvisioningProfile)
    }


}
