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

package wooga.gradle.fastlane

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import wooga.gradle.fastlane.internal.DefaultFastlanePluginExtension
import wooga.gradle.fastlane.tasks.AbstractFastlaneTask
import wooga.gradle.fastlane.tasks.PilotUpload
import wooga.gradle.fastlane.tasks.SighRenew

import static FastlanePluginConventions.*

class FastlanePlugin implements Plugin<Project> {
    static final String EXTENSION_NAME = "fastlane"
    static final String FASTLANE_GROUP = "fastlane"

    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        def extension = project.extensions.create(FastlanePluginExtension, EXTENSION_NAME, DefaultFastlanePluginExtension, project)

        extension.username.set(USERNAME_LOOKUP.getStringValueProvider(project))
        extension.password.set(PASSWORD_LOOKUP.getStringValueProvider(project))
        extension.apiKeyPath.set(API_KEY_PATH_LOOKUP.getFileValueProvider(project))

        project.tasks.withType(AbstractFastlaneTask, new Action<AbstractFastlaneTask>() {
            @Override
            void execute(AbstractFastlaneTask task) {
                task.apiKeyPath.set(extension.apiKeyPath)
            }
        })

        project.tasks.withType(SighRenew, new Action<SighRenew>() {
            @Override
            void execute(SighRenew task) {
                task.group = FASTLANE_GROUP
                task.description = "runs fastlane sigh renew"

                task.username.set(extension.username)
                task.password.set(extension.password)
            }
        })

        project.tasks.withType(PilotUpload, new Action<PilotUpload>() {
            @Override
            void execute(PilotUpload task) {
                task.group = BasePlugin.UPLOAD_GROUP
                task.description = "runs fastlane pilot upload"

                task.username.set(extension.username)
                task.password.set(extension.password)
            }
        })
    }
}
