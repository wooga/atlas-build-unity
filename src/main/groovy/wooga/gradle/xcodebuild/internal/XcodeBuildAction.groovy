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
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.internal.io.LineBufferingOutputStream
import org.gradle.internal.io.TextStream
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import wooga.gradle.xcodebuild.ConsoleSettings
import wooga.gradle.xcodebuild.XcodeAction
import com.wooga.xcodebuild.xcpretty.Printer
import com.wooga.xcodebuild.xcpretty.formatters.Simple

class XcodeBuildAction implements XcodeAction {

    final Project project
    final Provider<List<String>> buildArguments
    final Provider<RegularFile> logFile
    final Provider<ConsoleSettings> consoleSettings

    XcodeBuildAction(Project project, Provider<List<String>> buildArguments, Provider<RegularFile> logFile, Provider<ConsoleSettings> consoleSettings) {
        this.project = project
        this.buildArguments = buildArguments
        this.logFile = logFile
        this.consoleSettings = consoleSettings
    }

    ExecResult exec() {
        TextStream handler = new ForkTextStream()
        def outStream = new LineBufferingOutputStream(handler)

        if (logFile.present) {
            File log = logFile.get().asFile
            log.parentFile.mkdirs()
            handler.addWriter(log.newPrintWriter())
        }

        if (consoleSettings.present) {
            def printSettings = consoleSettings.get()
            if (printSettings.prettyPrint) {
                handler.addWriter(new Printer(new Simple(printSettings.useUnicode, printSettings.colorize), System.out.newPrintWriter()))
            } else {
                handler.addWriter(System.out.newPrintWriter())
            }
        } else {
            handler.addWriter(System.out.newPrintWriter())
        }

        project.exec(new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec exec) {
                exec.with {
                    executable "/usr/bin/xcrun"
                    args = buildArguments.get()
                    standardOutput = outStream
                }
            }
        })
    }
}
