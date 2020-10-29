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

import wooga.gradle.xcodebuild.xcpretty.Parser
import wooga.gradle.xcodebuild.xcpretty.Snippet

@Mixin(ANSI)
abstract class Formatter {

    final Parser parser
    final Boolean useUnicode

    private static String ERROR = '❌ '
    private static String ASCIIERROR = '[x]'

    private static String WARNING = '⚠️ '
    private static String ASCIIWARNING = '[!]'

    Formatter(Boolean useUnicode = true, Boolean colorize = true) {
        parser = new Parser(this)
        this.useUnicode = useUnicode
        this.colorize = colorize
    }

    abstract String formatAnalyze(String fileName, String filePath)

    abstract String formatBuildTarget(String target, String project, String configuration)

    abstract String formatAggregateTarget(String target, project, String configuration)

    abstract String formatAnalyzeTarget(String target, project, String configuration)

    abstract String formatCheckDependencies()

    abstract String formatClean(String project, String target, String configuration)

    abstract String formatCleanTarget(String target, String project, String configuration)

    abstract String formatCleanRemove()

    abstract String formatCompile(String fileName, String filePath)

    abstract String formatCompileCommand(String compilerCommand, String filePath)

    abstract String formatCompileStoryboard(String fileName, String filePath)

    abstract String formatCompileXib(String fileName, String filePath)

    abstract String formatCopyHeaderFile(String source, String target)

    abstract String formatCopyPlistFile(String source, String target)

    abstract String formatCopyStringsFile(String fileName)

    abstract String formatCpresource(String file)

    abstract String formatGenerateDsym(String dsym)

    abstract String formatLinking(String file, String buildVariant, String arch)

    abstract String formatLibtool(String library)

    abstract String formatPassingTest(String suite, String test, String time)

    abstract String formatPendingTest(String suite, String test)

    abstract String formatMeasuringTest(String suite, String test, String time)

    abstract String formatFailingTest(String suite, String test, String reason, String filePath)

    abstract String formatProcessPch(String file)

    abstract String formatProcessPchCommand(String filePath)

    abstract String formatPhaseSuccess(String phaseName)

    abstract String formatPhaseScriptExecution(String scriptName)

    abstract String formatProcessInfoPlist(String fileName, String filePath)

    abstract String formatCodesign(String file)

    abstract String formatPreprocess(String file)

    abstract String formatPbxcp(String file)

    abstract String formatShellCommand(String command, String arguments)

    abstract String formatTestRunStarted(String name)

    abstract String formatTestRunFinished(String name, String time)

    abstract String formatTestSuiteStarted(String name)

    abstract String formatTouch(String filePath, String fileName)

    abstract String formatTiffutil(String file)

    abstract String formatWriteFile(String file)

    abstract String formatWriteAuxiliaryFiles()

    String formatWarning(String message) {
        message
    }

    String prettyFormat(String line) {
        parser.parse(line)
    }

    String getOptionalNewline() {
        "\n"
    }

    String formatError(String message) {
        """
        ${red(errorSymbol + " " + message)}

        """.stripIndent()
    }

    String formatCompileError(String file, String filePath, String reason, String line, String cursor) {
        """
        ${red(errorSymbol + " ")}${filePath}: ${red(reason)}

        ${line}
        ${cyan(cursor)}

        """.stripIndent()
    }

    String formatFileMissingError(String reason, String filePath) {
        """
        ${red(errorSymbol + " " + reason)} ${filePath}

        """.stripIndent()
    }

    String formatCompileWarning(String file, String filePath, String reason, String line, String cursor) {
        """
        ${yellow(warningSymbol + ' ')}${filePath}: ${yellow(reason)}

        ${line}
        ${cyan(cursor)}

        """.stripIndent()
    }

    String formatLdWarning(String reason) {
        "${yellow(warningSymbol + ' ' + reason)}"
    }

    String formatUndefinedSymbols(String message, String symbol, String reference) {
        """
        ${red(errorSymbol + ' ' + message)}
        > Symbol: ${symbol}
        > Referenced from: ${reference}

        """.stripIndent()
    }

    String formatDuplicateSymbols(String message, List<String> filePaths) {
        def duplicateFiles = filePaths.collect { "> ${it.split('/').last()}" }

        """
        ${red(errorSymbol + ' ' + message)}
        ${duplicateFiles.join("\n        ")}

        """.stripIndent()
    }

    String formatWillNotBeCodeSigned(String message) {
        "${yellow(warningSymbol + ' ' + message)}"
    }

    String formatTestSummary(String message, Map<String, List<Parser.Failure>> failuresPerSuite) {
        def failures = formatFailures(failuresPerSuite)
        def finalMessage = (failures.isEmpty()) ? green(message) : red(message)

        def text = [failures, finalMessage].join("\n\n\n").trim()
        "\n\n${text}"
    }

    String formatOther(String text) {
        ""
    }

    String getErrorSymbol() {
        useUnicode ? ERROR : ASCIIERROR
    }

    String getWarningSymbol() {
        useUnicode ? WARNING : ASCIIWARNING
    }

    protected String formatFailures(Map<String, List<Parser.Failure>> failuresPerSuite) {
        failuresPerSuite.collect { suite, failures ->
            def formattedFailures = failures.collect { failure ->
                formatFailure(failure)
            }.join("\n\n")

            "\n${suite}\n${formattedFailures}"
        }.join("\n")
    }

    protected String formatFailure(Parser.Failure failure) {
        Snippet snippet = Snippet.fromFile(failure.filePath)
        def output = "  ${failure.testCase}, ${red(failure.reason)}"
        output += "\n  ${cyan(failure.filePath)}"
        if (snippet.contents.isEmpty()) {
            return output
        }

        output += "\n  ```\n"
        if (colorize) {
            output += snippet.contents
        } else {
            output += snippet.contents
        }

        output += "  ```"
        output
    }
}
