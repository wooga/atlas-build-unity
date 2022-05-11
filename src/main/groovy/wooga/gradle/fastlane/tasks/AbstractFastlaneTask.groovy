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

import com.wooga.gradle.ArgumentsSpec
import com.wooga.gradle.io.LogFileSpec
import com.wooga.gradle.io.OutputStreamSpec
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecSpec
import wooga.gradle.build.unity.internal.ExecUtil
import wooga.gradle.fastlane.models.FastLaneTaskSpec

abstract class AbstractFastlaneTask extends DefaultTask implements FastLaneTaskSpec,
    ArgumentsSpec,
    LogFileSpec,
    OutputStreamSpec {

    AbstractFastlaneTask() {

        environment.set(project.provider({
            Map<String, String> environment = [:]

            if (password.isPresent()) {
                environment['FASTLANE_PASSWORD'] = password.get()
            }

            environment as Map<String, String>
        }))

        outputs.upToDateWhen(new Spec<Task>() {
            @Override
            boolean isSatisfiedBy(Task task) {
                false
            }
        })
    }

    @TaskAction
    protected void exec() {

        def executablePath = ExecUtil.getExecutable("fastlane")
        def _environment = environment.get()

        project.exec(new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec exec) {
                exec.with {
                    executable executablePath
                    args arguments.get()
                    environment = _environment
                    standardOutput = getOutputStream(logFile.asFile.getOrNull())
                }
            }
        })
    }

    void addDefaultArguments(List<String> arguments) {
        if (username.present) {
            arguments << "--username" << username.get()
        }

        if (teamId.present) {
            arguments << "--team_id" << teamId.get()
        }

        if (teamName.present) {
            arguments << "--team_name" << teamName.get()
        }

        if (appIdentifier.present) {
            arguments << "--app_identifier" << appIdentifier.get()
        }

        if (apiKeyPath.present) {
            arguments << "--api-key-path" << apiKeyPath.get().asFile.path
        }
    }
}
