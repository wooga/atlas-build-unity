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

package wooga.gradle.xcodebuild.xcpretty

import wooga.gradle.xcodebuild.xcpretty.formatters.Formatter

import java.util.regex.Matcher as M

import static wooga.gradle.xcodebuild.xcpretty.Matcher.*

class Parser {


    final Formatter formatter

    private Boolean testsDone
    private Boolean formattedSummery
    private Boolean formattingError
    private Boolean formattingWarning
    private Boolean formattingLinkerFailure
    private Map<String, List<Failure>> failures
    private IssueInfo _currentIssue
    private LinkerFailureInfo _currentLinkerFailure
    private String testSuite
    private String testCase

    Parser(Formatter formatter) {
        this.formatter = formatter
    }


    String parse(String line) {
        updateTestState(line)
        updateErrorState(line)
        updateLinkerFailureState(line)

        if (shouldFormatError) {
            return formatCompileError()
        }

        if (shouldFormatWarning) {
            return formatCompileWarning()
        }

        if (shouldFormatUndefinedSymbols) {
            return formatUndefinedSymbols()
        }

        if (shouldFormatDuplicateSymbols) {
            return formatDuplicateSymbols()
        }

        if (line =~ ANALYZE_MATCHER) {
            return formatter.formatAnalyze(M.lastMatcher[0][2].toString(), M.lastMatcher[0][1].toString())
        } else if (line =~ BUILD_TARGET_MATCHER) {
            return formatter.formatBuildTarget(M.lastMatcher[0][1].toString(), M.lastMatcher[0][2].toString(), M.lastMatcher[0][3].toString())
        } else if (line =~ AGGREGATE_TARGET_MATCHER) {
            return formatter.formatAggregateTarget(M.lastMatcher[0][1].toString(), M.lastMatcher[0][2].toString(), M.lastMatcher[0][3].toString())
        } else if (line =~ ANALYZE_TARGET_MATCHER) {
            return formatter.formatAnalyzeTarget(M.lastMatcher[0][1].toString(), M.lastMatcher[0][2].toString(), M.lastMatcher[0][3].toString())
        } else if (line =~ CLEAN_REMOVE_MATCHER) {
            return formatter.formatCleanRemove()
        } else if (line =~ CLEAN_TARGET_MATCHER) {
            return formatter.formatCleanTarget(M.lastMatcher[0][1].toString(), M.lastMatcher[0][2].toString(), M.lastMatcher[0][3].toString())
        } else if (line =~ COPY_STRINGS_MATCHER) {
            return formatter.formatCopyStringsFile(M.lastMatcher[0][1].toString())
        } else if (line =~ CHECK_DEPENDENCIES_MATCHER) {
            return formatter.formatCheckDependencies()
        } else if (line =~ Matcher.Errors.CLANG_ERROR_MATCHER) {
            return formatter.formatError(M.lastMatcher[0][1].toString())
        } else if (line =~ CODESIGN_FRAMEWORK_MATCHER) {
            return formatter.formatCodesign(M.lastMatcher[0][1].toString())
        } else if (line =~ CODESIGN_MATCHER) {
            return formatter.formatCodesign(M.lastMatcher[0][1].toString())
        } else if (line =~ Matcher.Errors.CHECK_DEPENDENCIES_ERRORS_MATCHER) {
            return formatter.formatError(M.lastMatcher[0][1].toString())
        } else if (line =~ Matcher.Errors.PROVISIONING_PROFILE_REQUIRED_MATCHER) {
            return formatter.formatError(M.lastMatcher[0][1].toString())
        } else if (line =~ Matcher.Errors.NO_CERTIFICATE_MATCHER) {
            return formatter.formatError(M.lastMatcher[0][1].toString())
        } else if (line =~ COMPILE_MATCHER) {
            return formatter.formatCompile(M.lastMatcher[0][2].toString(), M.lastMatcher[0][1].toString())
        } else if (line =~ COMPILE_COMMAND_MATCHER) {
            return formatter.formatCompileCommand(M.lastMatcher[0][1].toString(), M.lastMatcher[0][2].toString())
        } else if (line =~ COMPILE_XIB_MATCHER) {
            return formatter.formatCompileXib(M.lastMatcher[0][2].toString(), M.lastMatcher[0][1].toString())
        } else if (line =~ COMPILE_STORYBOARD_MATCHER) {
            return formatter.formatCompileStoryboard(M.lastMatcher[0][2].toString(), M.lastMatcher[0][1].toString())
        } else if (line =~ COPY_HEADER_MATCHER) {
            return formatter.formatCopyHeaderFile(M.lastMatcher[0][1].toString(), M.lastMatcher[0][2].toString())
        } else if (line =~ COPY_PLIST_MATCHER) {
            return formatter.formatCopyPlistFile(M.lastMatcher[0][1].toString(), M.lastMatcher[0][2].toString())
        } else if (line =~ CPRESOURCE_MATCHER) {
            return formatter.formatCpresource(M.lastMatcher[0][1].toString())
        } else if (line =~ EXECUTED_MATCHER) {
            formatSummaryIfNeeded(line)
        } else if (line =~ RESTARTING_TESTS_MATCHER) {
            return formatter.formatFailingTest(testSuite, testCase, "Test crashed", "n/a")
        } else if (line =~ UI_FAILING_TEST_MATCHER) {
            return formatter.formatFailingTest(testSuite, testCase, M.lastMatcher[0][2].toString(), M.lastMatcher[0][1].toString())
        } else if (line =~ FAILING_TEST_MATCHER) {
            return formatter.formatFailingTest(M.lastMatcher[0][2].toString(), M.lastMatcher[0][3].toString(), M.lastMatcher[0][4].toString(), M.lastMatcher[0][1].toString())
        } else if (line =~ Matcher.Errors.FATAL_ERROR_MATCHER) {
            return formatter.formatError(M.lastMatcher[0][1].toString())
        } else if (line =~ Matcher.Errors.FILE_MISSING_ERROR_MATCHER) {
            return formatter.formatFileMissingError(M.lastMatcher[0][1].toString(), M.lastMatcher[0][2].toString())
        } else if (line =~ GENERATE_DSYM_MATCHER) {
            return formatter.formatGenerateDsym(M.lastMatcher[0][1].toString())
        } else if (line =~ Matcher.Warnings.LD_WARNING_MATCHER) {
            return formatter.formatLdWarning(M.lastMatcher[0][1].toString() + M.lastMatcher[0][2].toString().toString())
        } else if (line =~ Matcher.Errors.LD_ERROR_MATCHER) {
            return formatter.formatError(M.lastMatcher[0][1].toString())
        } else if (line =~ LIBTOOL_MATCHER) {
            return formatter.formatLibtool(M.lastMatcher[0][1].toString())
        } else if (line =~ LINKING_MATCHER) {
            return formatter.formatLinking(M.lastMatcher[0][1].toString(), M.lastMatcher[0][2].toString(), M.lastMatcher[0][3].toString())
        } else if (line =~ Matcher.Errors.MODULE_INCLUDES_ERROR_MATCHER) {
            return formatter.formatError(M.lastMatcher[0][1].toString())
        } else if (line =~ TEST_CASE_MEASURED_MATCHER) {
            return formatter.formatMeasuringTest(M.lastMatcher[0][1].toString(), M.lastMatcher[0][2].toString(), M.lastMatcher[0][3].toString())
        } else if (line =~ TEST_CASE_PENDING_MATCHER) {
            return formatter.formatPendingTest(M.lastMatcher[0][1].toString(), M.lastMatcher[0][2].toString())
        } else if (line =~ TEST_CASE_PASSED_MATCHER) {
            return formatter.formatPassingTest(M.lastMatcher[0][1].toString(), M.lastMatcher[0][2].toString(), M.lastMatcher[0][3].toString())
        } else if (line =~ Matcher.Errors.PODS_ERROR_MATCHER) {
            return formatter.formatError(M.lastMatcher[0][1].toString())
        } else if (line =~ PROCESS_INFO_PLIST_MATCHER) {
            return formatter.formatProcessInfoPlist(unescaped(M.lastMatcher[0][2].toString()), unescaped(M.lastMatcher[0][1].toString()))
        } else if (line =~ PHASE_SCRIPT_EXECUTION_MATCHER) {
            return formatter.formatPhaseScriptExecution(unescaped(M.lastMatcher[0][1].toString().toString()))
        } else if (line =~ PHASE_SUCCESS_MATCHER) {
            return formatter.formatPhaseSuccess(M.lastMatcher[0][1].toString())
        } else if (line =~ PROCESS_PCH_MATCHER) {
            return formatter.formatProcessPch(M.lastMatcher[0][1].toString())
        } else if (line =~ PROCESS_PCH_COMMAND_MATCHER) {
            return formatter.formatProcessPchCommand(M.lastMatcher[0][1].toString())
        } else if (line =~ PREPROCESS_MATCHER) {
            return formatter.formatPreprocess(M.lastMatcher[0][1].toString())
        } else if (line =~ PBXCP_MATCHER) {
            return formatter.formatPbxcp(M.lastMatcher[0][1].toString())
        } else if (line =~ TESTS_RUN_COMPLETION_MATCHER) {
            return formatter.formatTestRunFinished(M.lastMatcher[0][1].toString(), M.lastMatcher[0][3].toString())
        } else if (line =~ TEST_SUITE_STARTED_MATCHER) {
            return formatter.formatTestRunStarted(M.lastMatcher[0][1].toString())
        } else if (line =~ TEST_SUITE_START_MATCHER) {
            return formatter.formatTestSuiteStarted(M.lastMatcher[0][1].toString())
        } else if (line =~ TIFFUTIL_MATCHER) {
            return formatter.formatTiffutil(M.lastMatcher[0][1].toString())
        } else if (line =~ TOUCH_MATCHER) {
            return formatter.formatTouch(M.lastMatcher[0][1].toString(), M.lastMatcher[0][2].toString())
        } else if (line =~ WRITE_FILE_MATCHER) {
            return formatter.formatWriteFile(M.lastMatcher[0][1].toString())
        } else if (line =~ WRITE_AUXILIARY_FILES) {
            return formatter.formatWriteAuxiliaryFiles()
        } else if (line =~ SHELL_COMMAND_MATCHER) {
            return formatter.formatShellCommand(M.lastMatcher[0][1].toString(), M.lastMatcher[0][2].toString())
        } else if (line =~ Matcher.Warnings.GENERIC_WARNING_MATCHER) {
            return formatter.formatWarning(M.lastMatcher[0][1].toString())
        } else if (line =~ Matcher.Warnings.WILL_NOT_BE_CODE_SIGNED_MATCHER) {
            return formatter.formatWillNotBeCodeSigned(M.lastMatcher[0][1].toString())
        } else {
            return formatter.formatOther(line)
        }
    }

    private void updateTestState(line) {

        if (line =~ TEST_SUITE_STARTED_MATCHER) {
            testsDone = false
            formattedSummery = false
            failures = new HashMap()
        } else if (line =~ TEST_CASE_STARTED_MATCHER) {
            testSuite = M.lastMatcher[0][1].toString()
            testCase = M.lastMatcher[0][2].toString()
        } else if (line =~ TESTS_RUN_COMPLETION_MATCHER) {
            testsDone = true
        } else if (line =~ FAILING_TEST_MATCHER) {
            storeFailure(M.lastMatcher[0][1].toString(), M.lastMatcher[0][2].toString(), M.lastMatcher[0][3].toString(), M.lastMatcher[0][4].toString())
        } else if (line =~ UI_FAILING_TEST_MATCHER) {
            storeFailure(M.lastMatcher[0][1].toString(), testSuite, testCase, M.lastMatcher[0][2].toString())
        } else if (line =~ RESTARTING_TESTS_MATCHER) {
            storeFailure("n/a", testSuite, testCase, "Test crashed")
        }

    }

    class IssueInfo {
        String fileName
        String filePath
        String reason
        String cursor
        String line
    }

    static class Failure {
        final String filePath
        final String reason
        final String testCase

        Failure(String filePath, String reason, String testCase) {
            this.filePath = filePath
            this.reason = reason
            this.testCase = testCase
        }
    }

    class LinkerFailureInfo {
        String message
        String symbol
        String reference
        List<String> files = []
    }

    private updateErrorState(String line) {
        def updateError = {
            currentIssue.reason = M.lastMatcher[0][3].toString()
            currentIssue.filePath = M.lastMatcher[0][1].toString()
            currentIssue.fileName = M.lastMatcher[0][2].toString()
        }

        if (line =~ Matcher.Errors.COMPILE_ERROR_MATCHER) {
            formattingError = true
            updateError.call()
        } else if (line =~ Matcher.Warnings.COMPILE_WARNING_MATCHER) {
            formattingWarning = true
            updateError.call()
        } else if (line =~ Matcher.Errors.CURSOR_MATCHER) {
            currentIssue.cursor = M.lastMatcher[0][1].toString().toString()
        } else if (formattingError || formattingWarning) {
            currentIssue.line = line
        }
    }


    private updateLinkerFailureState(String line) {
        if (line =~ Matcher.Errors.LINKER_UNDEFINED_SYMBOLS_MATCHER || line =~ Matcher.Errors.LINKER_DUPLICATE_SYMBOLS_MATCHER) {
            currentLinkerFailure.message = M.lastMatcher[0][1].toString()
            formattingLinkerFailure = true
        }

        if (!formattingLinkerFailure) {
            return
        }


        if (line =~ Matcher.Errors.SYMBOL_REFERENCED_FROM_MATCHER) {
            currentLinkerFailure.symbol = M.lastMatcher[0][1].toString()
        } else if (line =~ Matcher.Errors.LINKER_UNDEFINED_SYMBOL_LOCATION_MATCHER) {
            currentLinkerFailure.reference = M.lastMatcher[0][1].toString().toString().trim()
        } else if (line =~ Matcher.Errors.LINKER_DUPLICATE_SYMBOLS_LOCATION_MATCHER) {
            currentLinkerFailure.files << M.lastMatcher[0][1].toString().toString()
        }

    }

    private IssueInfo getCurrentIssue() {
        if (!_currentIssue) {
            _currentIssue = new IssueInfo()
        }
        _currentIssue
    }

    private LinkerFailureInfo getCurrentLinkerFailure() {
        if (!_currentLinkerFailure) {
            _currentLinkerFailure = new LinkerFailureInfo()
        }
        _currentLinkerFailure
    }

    private Boolean getShouldFormatError() {
        formattingError && errorOrWarningPresent
    }

    private Boolean getShouldFormatWarning() {
        formattingWarning && errorOrWarningPresent
    }

    private Boolean isErrorOrWarningPresent() {
        currentIssue.reason && currentIssue.cursor && currentIssue.line
    }

    private Boolean getShouldFormatUndefinedSymbols() {
        currentLinkerFailure.message && currentLinkerFailure.symbol && currentLinkerFailure.reference
    }

    private Boolean getShouldFormatDuplicateSymbols() {
        currentLinkerFailure.message && currentLinkerFailure.files.size() > 1
    }

    private String formatCompileError() {
        def error = _currentIssue
        _currentIssue = new IssueInfo()
        formattingError = false
        formatter.formatCompileError(error.fileName, error.filePath, error.reason, error.line, error.cursor)
    }

    private String formatCompileWarning() {
        def warning = _currentIssue
        _currentIssue = new IssueInfo()
        formattingError = false
        formatter.formatCompileWarning(warning.fileName, warning.filePath, warning.reason, warning.line, warning.cursor)
    }

    private String formatUndefinedSymbols() {
        def result = formatter.formatUndefinedSymbols(currentLinkerFailure.message, currentLinkerFailure.symbol, currentLinkerFailure.reference)
        resetLinkerFormatState()
        result
    }

    private String formatDuplicateSymbols() {
        def result = formatter.formatDuplicateSymbols(currentLinkerFailure.message, currentLinkerFailure.files)
        resetLinkerFormatState()
        result
    }

    private resetLinkerFormatState() {
        _currentLinkerFailure = null
        formattingLinkerFailure = false
    }

    private storeFailure(String file, String testSuite, String testCase, String reason) {
        if (!failuresPerTestSuit[testSuite]) {
            failuresPerTestSuit[testSuite] = []
        }

        failuresPerTestSuit[testSuite] << new Failure(
                file,
                reason,
                testCase
        )
    }

    private Map<String, List<Failure>> getFailuresPerTestSuit() {
        if (!failures) {
            failures = new HashMap()
        }
        failures
    }

    String formatSummaryIfNeeded(String executedMessage) {
        if (shouldFormatSummary) {
            formattedSummery = true
            formatter.formatTestSummary(executedMessage, failuresPerTestSuit)
        }
        return ""
    }


    private Boolean getShouldFormatSummary() {
        testsDone && !formattedSummery
    }


    private String unescaped(String escapedValues) {
        escapedValues.replace('\\', '')
    }
}
