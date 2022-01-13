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

import com.wooga.xcodebuild.xcpretty.Printer
import com.wooga.xcodebuild.xcpretty.formatters.Simple
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import wooga.gradle.xcodebuild.ConsoleSettings
import wooga.gradle.xcodebuild.XcodeAction

class XcodeBuildAction implements XcodeAction {

    final Project project
    final List<String> buildArguments
    final File logFile
    final ConsoleSettings consoleSettings

    XcodeBuildAction(Project project, List<String> buildArguments, File logFile, ConsoleSettings consoleSettings) {
        this.project = project
        this.buildArguments = buildArguments
        this.logFile = logFile
        this.consoleSettings = consoleSettings
    }

    ExecResult exec() {
        TextStream handler = new ForkTextStream()

        def outStream = new LineBufferingOutputStream(handler)
        def errStream = new LineBufferingOutputStream(handler)
        def logWriter = System.out.newPrintWriter()
        if (logFile) {
            logFile.parentFile.mkdirs()
            handler.addWriter(logFile.newPrintWriter())
        }

        if (consoleSettings.prettyPrint) {
            handler.addWriter(new Printer(new Simple(consoleSettings.useUnicode, consoleSettings.hasColors()), logWriter))
        } else {
            handler.addWriter(logWriter)
        }

        project.exec(new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec exec) {
                exec.with {
                    executable "/usr/bin/xcrun"
                    args = buildArguments
                    standardOutput = outStream
                    errorOutput = errStream
                }
            }
        })
    }
}
