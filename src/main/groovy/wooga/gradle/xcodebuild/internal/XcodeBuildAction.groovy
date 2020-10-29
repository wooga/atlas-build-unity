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
 *
 *
 *
 */

package wooga.gradle.xcodebuild.internal

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import wooga.gradle.xcodebuild.XcodeAction

class XcodeBuildAction implements XcodeAction {

    final Project project
    final Provider<List<String>> buildArguments

    XcodeBuildAction(Project project, Provider<List<String>> buildArguments) {
        this.project = project
        this.buildArguments = buildArguments
    }

    ExecResult exec() {
        project.exec(new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec exec) {
                exec.with {
                    executable "/usr/bin/xcrun"
                    args = buildArguments.get()
                    errorOutput = System.err
                    standardOutput = System.out
                }
            }
        })
    }
}
