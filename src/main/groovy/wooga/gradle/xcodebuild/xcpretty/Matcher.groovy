/*
 * Copyright 2018-2020 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http: *www.apache.org/licenses/LICENSE-2.0
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

import java.util.regex.Pattern

class Matcher {
    /**
     * @regex Captured groups
     * $1 file_path
     * $2 file_name 
     * */
    static String ANALYZE_MATCHER = /^Analyze(?:Shallow)?\s(.*\/(.*\.(?:m|mm|cc|cpp|c|cxx)))\s*/

    /**
     * @regex Captured groups
     * $1 target
     * $2 project
     * $3 configuration
     * */
    static String BUILD_TARGET_MATCHER = /^=== BUILD TARGET\s(.*)\sOF PROJECT\s(.*)\sWITH.*CONFIGURATION\s(.*)\s===/

    /**
     * @regex Captured groups
     * $1 target
     * $2 project
     * $3 configuration
     * */
    static String AGGREGATE_TARGET_MATCHER = /^=== BUILD AGGREGATE TARGET\s(.*)\sOF PROJECT\s(.*)\sWITH.*CONFIGURATION\s(.*)\s===/

    /**
     * @regex Captured groups
     * $1 target
     * $2 project
     * $3 configuration
     * */
    static String ANALYZE_TARGET_MATCHER = /^=== ANALYZE TARGET\s(.*)\sOF PROJECT\s(.*)\sWITH.*CONFIGURATION\s(.*)\s===/

    /**
     * @regex Nothing returned here for now
     * */
    static String CHECK_DEPENDENCIES_MATCHER = /^Check dependencies/

    /**
     * @regex Captured groups
     * $1 command path
     * $2 arguments
     * */
    static String SHELL_COMMAND_MATCHER = /^\s{4}(cd|setenv|(?:[\w\/:\\\s\-.]+?\/)?[\w\-]+)\s(.*)$/

    /**
     * @regex Nothing returned here for now
     * */
    static String CLEAN_REMOVE_MATCHER = /^Clean.Remove/

    /**
     * @regex Captured groups
     * $1 target
     * $2 project
     * $3 configuration
     * */
    static String CLEAN_TARGET_MATCHER = /^=== CLEAN TARGET\s(.*)\sOF PROJECT\s(.*)\sWITH CONFIGURATION\s(.*)\s===/

    /**
     * @regex Captured groups
     * $1 = file
     * */
    static String CODESIGN_MATCHER = /(?m)^CodeSign\s((?:\\ |[^ ])*)$/

    /**
     * @regex Captured groups
     * $1 = file
     * */
    static String CODESIGN_FRAMEWORK_MATCHER = /^CodeSign\s((?:\\ |[^ ])*.framework)\/Versions/

    /**
     * @regex Captured groups
     * $1 file_path
     * $2 file_name (e.g. KWNull.m)
     * */
    static String COMPILE_MATCHER = /^Compile[\w]+\s.+?\s((?:\\.|[^ ])+\/((?:\\.|[^ ])+\.(?:m|mm|c|cc|cpp|cxx|swift)))\s.*/

    /**
     * @regex Captured groups
     * $1 compiler_command
     * $2 file_path
     * */
    static String COMPILE_COMMAND_MATCHER = /^\s*(.*clang\s.*\s\-c\s(.*\.(?:m|mm|c|cc|cpp|cxx))\s.*\.o)$/

    /**
     * @regex Captured groups
     * $1 file_path
     * $2 file_name (e.g. MainMenu.xib)
     * */
    static String COMPILE_XIB_MATCHER = /^CompileXIB\s(.*\/(.*\.xib))/

    /**
     * @regex Captured groups
     * $1 file_path
     * $2 file_name (e.g. Main.storyboard)
     * */
    static String COMPILE_STORYBOARD_MATCHER = /^CompileStoryboard\s(.*\/([^\/].*\.storyboard))/

    /**
     * @regex Captured groups
     * $1 source file
     * $2 target file
     * */
    static String COPY_HEADER_MATCHER = /^CpHeader\s(.*\.h)\s(.*\.h)/

    /**
     * @regex Captured groups
     * $1 source file
     * $2 target file
     * */
    static String COPY_PLIST_MATCHER = /^CopyPlistFile\s(.*\.plist)\s(.*\.plist)/

    /**
     * @regex Captured groups
     * $1 file
     * */
    static String COPY_STRINGS_MATCHER = /^CopyStringsFile.*\/(.*.strings)/

    /**
     * @regex Captured groups
     * $1 resource
     * */
    static String CPRESOURCE_MATCHER = /^CpResource\s(.*)\s\//

    static String EXECUTED_MATCHER = /^\s*Executed/

    /**
     * @regex Captured groups
     * $1 = file
     * $2 = test_suite
     * $3 = test_case
     * $4 = reason
     * */
    static String FAILING_TEST_MATCHER = /^\s*(.+:\d+):\serror:\s[\+\-]\[(.*)\s(.*)\]\s:(?:\s'.*'\s\[FAILED\],)?\s(.*)/

    /**
     * @regex Captured groups
     * $1 = file
     * $2 = reason
     * */
    static String UI_FAILING_TEST_MATCHER = /^\s{4}t = \s+\d+\.\d+s\s+Assertion Failure: (.*:\d+): (.*)$/

    /**
     * @regex Captured groups
     * */
    static String RESTARTING_TESTS_MATCHER = /^Restarting after unexpected exit or crash in.+$/

    /**
     * @regex Captured groups
     * $1 = dsym
     * */
    static String GENERATE_DSYM_MATCHER = /^GenerateDSYMFile \/.*\/(.*\.dSYM)/

    /**
     * @regex Captured groups
     * $1 = library
     * */
    static String LIBTOOL_MATCHER = /^Libtool.*\/(.*\.a)/

    /**
     * @regex Captured groups
     * $1 = target
     * $2 = build_variants (normal, profile, debug)
     * $3 = architecture
     * */
    static String LINKING_MATCHER = /^Ld \/?.*\/(.*?) (.*) (.*)/

    /**
     * @regex Captured groups
     * $1 = suite
     * $2 = test_case
     * $3 = time
     * */
    static String TEST_CASE_PASSED_MATCHER = /^\s*Test Case\s'-\[(.*)\s(.*)\]'\spassed\s\((\d*\.\d{3})\sseconds\)/


    /**
     * @regex Captured groups
     * $1 = suite
     * $2 = test_case
     * */
    static String TEST_CASE_STARTED_MATCHER = /^Test Case '-\[(.*) (.*)\]' started.$/

    /**
     * @regex Captured groups
     * $1 = suite
     * $2 = test_case
     * */
    static String TEST_CASE_PENDING_MATCHER = /^Test Case\s'-\[(.*)\s(.*)PENDING\]'\spassed/

    /**
     * @regex Captured groups
     * $1 = suite
     * $2 = test_case
     * $3 = time
     * */
    static String TEST_CASE_MEASURED_MATCHER = /^[^:]*:[^:]*:\sTest Case\s'-\[(.*)\s(.*)\]'\smeasured\s\[Time,\sseconds\]\saverage:\s(\d*\.\d{3}),/

    static String PHASE_SUCCESS_MATCHER = /^\*\*\s(.*)\sSUCCEEDED\s\*\*/

    /**
     * @regex Captured groups
     * $1 = script_name
     * */
    static String PHASE_SCRIPT_EXECUTION_MATCHER = /^PhaseScriptExecution\s((\\\ |\S)*)\s/

    /**
     * @regex Captured groups
     * $1 = file
     * */
    static String PROCESS_PCH_MATCHER = /^ProcessPCH\s.*\s(.*.pch)/

    /**
     * @regex Captured groups
     * $1 file_path
     * */
    static String PROCESS_PCH_COMMAND_MATCHER = /^\s*.*\/usr\/bin\/clang\s.*\s\-c\s(.*)\s\-o\s.*/

    /**
     * @regex Captured groups
     * $1 = file
     * */
    static String PREPROCESS_MATCHER = /(?m)^Preprocess\s(?:(?:\\ |[^ ])*)\s((?:\\ |[^ ])*)$/

    /**
     * @regex Captured groups
     * $1 = file
     * */
    static String PBXCP_MATCHER = /^PBXCp\s((?:\\ |[^ ])*)/

    /**
     * @regex Captured groups
     * $1 = file
     * */
    static String PROCESS_INFO_PLIST_MATCHER = /^ProcessInfoPlistFile\s.*\.plist\s(.*\/+(.*\.plist))/

    /**
     * @regex Captured groups
     * $1 = suite
     * $2 = time
     * */
    static String TESTS_RUN_COMPLETION_MATCHER = /^\s*Test Suite '(?:.*\/)?(.*[ox]ctest.*)' (finished|passed|failed) at (.*)/

    /**
     * @regex Captured groups
     * $1 = suite
     * $2 = time
     * */
    static String TEST_SUITE_STARTED_MATCHER = /^\s*Test Suite '(?:.*\/)?(.*[ox]ctest.*)' started at(.*)/

    /**
     * @regex Captured groups
     * $1 test suite name
     * */
    static String TEST_SUITE_START_MATCHER = /^\s*Test Suite '(.*)' started at/

    /**
     * @regex Captured groups
     * $1 file_name
     * */
    static String TIFFUTIL_MATCHER = /^TiffUtil\s(.*)/

    /**
     * @regex Captured groups
     * $1 file_path
     * $2 file_name
     * */
    static String TOUCH_MATCHER = /^Touch\s(.*\/(.+))/

    /**
     * @regex Captured groups
     * $1 file_path
     * */
    static String WRITE_FILE_MATCHER = /^write-file\s(.*)/

    /**
     * @regex Captured groups
     * */
    static String WRITE_AUXILIARY_FILES = /^Write auxiliary files/

    public class Warnings {
        /**
         * @regex Captured groups
         * $1 = file_path
         * $2 = file_name
         * $3 = reason
         * */
        static String COMPILE_WARNING_MATCHER = /^(\/.+\/(.*):.*:.*):\swarning:\s(.*)$/

        /**
         * @regex Captured groups
         * $1 = ld prefix
         * $2 = warning message
         * */
        static String LD_WARNING_MATCHER = /^(ld: )warning: (.*)/


        /**
         * @regex Captured groups
         * $1 = whole warning
         * */
        static String GENERIC_WARNING_MATCHER = /^warning:\s(.*)$/

        /**
         * @regex Captured groups
         * $1 = whole warning
         * */
        static String WILL_NOT_BE_CODE_SIGNED_MATCHER = /^(.* will not be code signed because .*)$/
    }

    public class Errors {
        /**
         * @regex Captured groups
         * $1 = whole error
         * */
        static String CLANG_ERROR_MATCHER = /^(clang: error:.*)$/

        /**
         * @regex Captured groups
         * $1 = whole error
         * */
        static String CHECK_DEPENDENCIES_ERRORS_MATCHER = /^(Code\s?Sign error:.*|Code signing is required for product type .* in SDK .*|No profile matching .* found:.*|Provisioning profile .* doesn't .*|Swift is unavailable on .*|.?Use Legacy Swift Language Version.*)$/

        /**
         * @regex Captured groups
         * $1 = whole error
         * */
        static String PROVISIONING_PROFILE_REQUIRED_MATCHER = /^(.*requires a provisioning profile.*)$/

        /**
         * @regex Captured groups
         * $1 = whole error
         * */
        static String NO_CERTIFICATE_MATCHER = /^(No certificate matching.*)$/

        /**
         * @regex Captured groups
         * $1 = file_path
         * $2 = file_name
         * $3 = reason
         * */
        static String COMPILE_ERROR_MATCHER = /^(\/.+\/(.*):.*:.*):\s(?:fatal\s)?error:\s(.*)$/

        /**
         * @regex Captured groups
         * $1 cursor (with whitespaces and tildes)
         * */
        static String CURSOR_MATCHER = /^([\s~]*\^[\s~]*)$/

        /**
         * @regex Captured groups
         * $1 = whole error.
         * it varies a lot, not sure if it makes sense to catch everything separately
         * */
        static String FATAL_ERROR_MATCHER = /^(fatal error:.*)$/

        /**
         * @regex Captured groups
         * $1 = whole error.
         * $2 = file path
         * */
        static String FILE_MISSING_ERROR_MATCHER = /^<unknown>:0:\s(error:\s.*)\s'(\/.+\/.*\..*)'$/

        /**
         * @regex Captured groups
         * $1 = whole error
         * */
        static String LD_ERROR_MATCHER = /^(ld:.*)/

        /**
         * @regex Captured groups
         * $1 file path
         * */
        static String LINKER_DUPLICATE_SYMBOLS_LOCATION_MATCHER = /^\s+(\/.*\.o[\)]?)$/

        /**
         * @regex Captured groups
         * $1 reason
         * */
        static String LINKER_DUPLICATE_SYMBOLS_MATCHER = /^(duplicate symbol .*):$/

        /**
         * @regex Captured groups
         * $1 symbol location
         * */
        static String LINKER_UNDEFINED_SYMBOL_LOCATION_MATCHER = /^(.* in .*\.o)$/

        /**
         * @regex Captured groups
         * $1 reason
         * */
        static String LINKER_UNDEFINED_SYMBOLS_MATCHER = /^(Undefined symbols for architecture .*):$/

        /**
         * @regex Captured groups
         * $1 reason
         * */
        static String PODS_ERROR_MATCHER = /^(error:\s.*)/

        /**
         * @regex Captured groups
         * $1 = reference
         * */
        static String SYMBOL_REFERENCED_FROM_MATCHER = /\s+"(.*)", referenced from:$/

        /**
         * @regex Captured groups
         * $1 = error reason
         * */
        static String MODULE_INCLUDES_ERROR_MATCHER = /^\<module-includes\>:.*?:.*?:\s(?:fatal\s)?(error:\s.*)$/
    }
}
