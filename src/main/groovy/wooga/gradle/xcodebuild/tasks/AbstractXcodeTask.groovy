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

package wooga.gradle.xcodebuild.tasks

import com.wooga.gradle.ArgumentsSpec
import com.wooga.gradle.io.*
import com.wooga.xcodebuild.xcpretty.Printer
import com.wooga.xcodebuild.xcpretty.formatters.Simple
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecSpec
import wooga.gradle.xcodebuild.XcodeTaskSpec

abstract class AbstractXcodeTask extends DefaultTask implements XcodeTaskSpec, LogFileSpec, ArgumentsSpec, OutputStreamSpec {

    AbstractXcodeTask() {
    }

    @TaskAction
    protected void exec() {
        // TODO: Refactor with output stream spec -> add an overload to set a custom handler
        TextStream handler = new ForkTextStream()

        def outStream = new LineBufferingOutputStream(handler, System.lineSeparator())
        def errStream = new LineBufferingOutputStream(handler, System.lineSeparator())
        def logWriter = System.out.newPrintWriter()

        if (logFile.present) {
            FileUtils.ensureFile(logFile)
            handler.addWriter(logFile.get().asFile.newPrintWriter())
        }

        def consoleSettings = consoleSettings.getOrNull()

        if (consoleSettings && consoleSettings.prettyPrint) {
            handler.addWriter(new Printer(new Simple(consoleSettings.useUnicode, consoleSettings.hasColors()), logWriter))
        } else {
            handler.addWriter(logWriter)
        }

        project.exec(new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec exec) {
                exec.with {
                    executable "/usr/bin/xcrun"
                    args = arguments.get()
                    standardOutput = outStream
                    errorOutput = errStream
                }
            }
        })
    }
}
