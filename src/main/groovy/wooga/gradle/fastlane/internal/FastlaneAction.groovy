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

package wooga.gradle.fastlane.internal


import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import wooga.gradle.build.unity.internal.ExecUtil
import wooga.gradle.xcodebuild.internal.ForkTextStream
import wooga.gradle.xcodebuild.internal.LineBufferingOutputStream
import wooga.gradle.xcodebuild.internal.TextStream

class FastlaneAction {

    final Project project
    final List<String> arguments
    final Map<String, String> environment
    final File logFile

    FastlaneAction(Project project, List<String> arguments, Map<String, String> environment, File logFile) {
        this.project = project
        this.arguments = arguments
        this.environment = environment
        this.logFile = logFile
    }

    ExecResult exec() {
        TextStream handler = new ForkTextStream()

        def outStream = new LineBufferingOutputStream(handler)
        def logWriter = System.out.newPrintWriter()
        if (logFile) {
            logFile.parentFile.mkdirs()
            handler.addWriter(logFile.newPrintWriter())
        }

        handler.addWriter(logWriter)


        def executablePath = ExecUtil.getExecutable("fastlane")
        def env = environment

        project.exec(new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec exec) {
                exec.with {
                    executable executablePath
                    args = arguments
                    environment(env)
                    standardOutput = outStream
                }
            }
        })
    }
}
