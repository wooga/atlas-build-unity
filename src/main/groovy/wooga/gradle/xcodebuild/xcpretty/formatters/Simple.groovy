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

package wooga.gradle.xcodebuild.xcpretty.formatters

import groovy.transform.InheritConstructors

import static ANSI.*

@InheritConstructors
class Simple extends Formatter {
    enum Status {
        pass("✓", "."),
        fail("✗", "x"),
        pending("⧖", "P"),
        completion("▸", ">"),
        measure("◷", "T")

        final String unicodeValue
        final String asciiValue

        Status(String unicodeValue, String asciiValue) {
            this.unicodeValue = unicodeValue
            this.asciiValue = asciiValue
        }

        String toString(Boolean useUniCode) {
            useUniCode ? unicodeValue : asciiValue
        }
    }


    private static String INDENT = "    "

    @Override
    String formatAnalyze(String fileName, String filePath) {
        format("Analyzing", fileName)
    }

    @Override
    String formatBuildTarget(String target, String project, String configuration) {
        format("Building", "${project}/${target} [${configuration}]")
    }

    @Override
    String formatAggregateTarget(String target, Object project, String configuration) {
        format("Aggregate", "${project}/${target} [${configuration}]")
    }

    @Override
    String formatAnalyzeTarget(String target, Object project, String configuration) {
        format("Analyzing", "${project}/${target} [${configuration}]")
    }

    @Override
    String formatCleanTarget(String target, String project, String configuration) {
        format("Cleaning", "${project}/${target} [${configuration}]")
    }

    @Override
    String formatCompile(String fileName, String filePath) {
        format("Compiling", fileName)
    }

    @Override
    String formatCompileXib(String fileName, String filePath) {
        format("Compiling", fileName)
    }

    @Override
    String formatCompileStoryboard(String fileName, String filePath) {
        format("Compiling", fileName)
    }

    @Override
    String formatCopyHeaderFile(String source, String target) {
        format("Copying", new File(source).name)
    }

    @Override
    String formatCopyPlistFile(String source, String target) {
        format("Copying", new File(source).name)
    }

    @Override
    String formatCopyStringsFile(String fileName) {
        format("Copying", fileName)
    }

    @Override
    String formatCpresource(String resource) {
        format("Copying", resource)
    }

    @Override
    String formatGenerateDsym(String dsym) {
        format("Generating '${dsym}'")
    }

    @Override
    String formatLibtool(String library) {
        format("Building library", library)
    }

    @Override
    String formatLinking(String target, String buildVariant, String arch) {
        format("Linking", target)
    }

    @Override
    String formatFailingTest(String suite, String testCase, String reason, String filePath) {
        INDENT + formatTest("${testCase}, ${reason}", Status.fail)
    }

    @Override
    String formatPassingTest(String suite, String testCase, String time) {
        INDENT + formatTest("${testCase} (${coloredTime(time)} seconds)", Status.pass)
    }

    @Override
    String formatPendingTest(String suite, String testCase) {
        INDENT + formatTest("${testCase} [PENDING]", Status.pending)
    }

    @Override
    String formatMeasuringTest(String suite, String testCase, String time) {
        INDENT + formatTest("${testCase} measured (${coloredTime(time)} seconds)", Status.measure)
    }

    @Override
    String formatPhaseSuccess(String phaseName) {
        format(phaseName.toLowerCase().capitalize(), "Succeeded")
    }

    @Override
    String formatPhaseScriptExecution(String scriptName) {
        format("Running script", "'${scriptName}'")
    }

    @Override
    String formatProcessInfoPlist(String fileName, String filePath) {
        format("Processing", fileName)
    }

    @Override
    String formatProcessPch(String file) {
        format("Precompiling", file)
    }

    @Override
    String formatCodesign(String file) {
        format("Signing", file)
    }

    @Override
    String formatPreprocess(String file) {
        format("Preprocessing", file)
    }

    @Override
    String formatPbxcp(String file) {
        format("Copying", file)
    }

    @Override
    String formatTouch(String filePath, String fileName) {
        format("Touching", fileName)
    }

    @Override
    String formatTiffutil(String fileName) {
        format("Validating", fileName)
    }

    @Override
    String formatWarning(String message) {
        INDENT + yellow(message)
    }

    @Override
    String formatCheckDependencies() {
        format('Check Dependencies')
    }

    @Override
    String formatClean(String project, String target, String configuration) {
        ""
    }

    @Override
    String formatCleanRemove() {
        ""
    }

    @Override
    String formatCompileCommand(String compilerCommand, String filePath) {
        ""
    }

    @Override
    String formatProcessPchCommand(String filePath) {
        ""
    }

    @Override
    String formatShellCommand(String command, String arguments) {
        ""
    }

    @Override
    String formatTestRunStarted(String name) {
        heading("Test Suite", name, "started")
    }

    @Override
    String formatTestRunFinished(String name, String time) {
        ""
    }

    @Override
    String formatTestSuiteStarted(String name) {
        heading(name)
    }

    @Override
    String formatWriteFile(String file) {
        ""
    }

    @Override
    String formatWriteAuxiliaryFiles() {
        ""
    }

    private String heading(String text) {
        heading("", text, "")
    }

    private String heading(String prefix, String text, String description) {
        [prefix, white(text), description].join(' ').trim()
    }

    private String format(String command, String argumentText = "", Boolean success = true) {
        def symbol = success ? Status.completion : Status.fail
        [symbol.toString(useUnicode), white(command), argumentText].join(' ').trim()
    }

    private String formatTest(String testCase, Status status) {
        [status.toString(useUnicode), testCase].join(' ').trim()
    }

    private static String coloredTime(String time) {
        def fTime = time.toFloat()
        if(fTime >= 0 && fTime <= 0.025) {
            return time
        } else if(fTime >= 0.026 && fTime < 0.100) {
            return yellow(time)
        } else {
            return red(time)
        }
    }
}
