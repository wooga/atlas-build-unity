package wooga.gradle.xcodebuild.xcpretty

import spock.lang.Requires
import spock.lang.Specification
import spock.lang.Unroll
import wooga.gradle.xcodebuild.xcpretty.formatters.Formatter

import static wooga.gradle.xcodebuild.XcodeFixtures.*

@Requires({ os.macOs })
class ParserSpec extends Specification {

    Formatter formatter = Mock()
    Parser parser = new Parser(formatter)

    def "parses analyze"() {
        when:
        parser.parse(SAMPLE_ANALYZE)

        then:
        1 * formatter.formatAnalyze("CCChip8DisplayView.m", "CocoaChip/CCChip8DisplayView.m")
    }

    def "parses analyze shallow"() {
        when:
        parser.parse(SAMPLE_ANALYZE_SHALLOW)

        then:
        1 * formatter.formatAnalyze("CCChip8DisplayView.m", "CocoaChip/CCChip8DisplayView.m")
    }

    def "parses analyze for a C++ target"() {
        when:
        parser.parse(SAMPLE_ANALYZE_CPP)

        then:
        1 * formatter.formatAnalyze("CCChip8DisplayView.cpp", "CocoaChip/CCChip8DisplayView.cpp")
    }

    def "parses build target"() {
        when:
        parser.parse(SAMPLE_BUILD)

        then:
        1 * formatter.formatBuildTarget("The Spacer", "Pods", "Debug")
    }

    def "parses aggregate target"() {
        when:
        parser.parse(SAMPLE_AGGREGATE_TARGET)

        then:
        1 * formatter.formatAggregateTarget("Be Aggro", "AggregateExample", "Debug")
    }

    def "parses analyze target"() {
        when:
        parser.parse(SAMPLE_ANALYZE_TARGET)

        then:
        1 * formatter.formatAnalyzeTarget("The Spacer", "Pods", "Debug")
    }

    def "parses clean remove"() {
        when:
        parser.parse(SAMPLE_CLEAN_REMOVE)

        then:
        1 * formatter.formatCleanRemove()

    }

    def "parses clean target"() {
        when:
        parser.parse(SAMPLE_CLEAN)

        then:
        1 * formatter.formatCleanTarget("Pods-ObjectiveSugar", "Pods", "Debug")
    }

    def "parses clean target withut dash in target name"() {
        when:
        parser.parse(SAMPLE_ANOTHER_CLEAN)

        then:
        1 * formatter.formatCleanTarget("Pods", "Pods", "Debug")
    }

    def "parses check dep}encies"() {
        when:
        parser.parse("Check dependencies")

        then:
        1 * formatter.formatCheckDependencies()

    }

    def "parses code signing"() {
        when:
        parser.parse(SAMPLE_CODESIGN)

        then:
        1 * formatter.formatCodesign("build/Release/CocoaChip.app")
    }

    def "parses code signing a framework"() {
        when:
        parser.parse(SAMPLE_CODESIGN_FRAMEWORK)

        then:
        1 * formatter.formatCodesign("build/Release/CocoaChipCore.framework")
    }

    def "parses compiler_space_in_path"() {
        when:
        parser.parse(SAMPLE_COMPILE_SPACE_IN_PATH)

        then:
        1 * formatter.formatCompile('SASellableItem.m', "SACore/App/Models/Core\\\\ Data/human/SASellableItem.m")
    }

    def "parses compiler commands"() {
        given:
        def compile_statement = SAMPLE_ANOTHER_COMPILE.readLines().last()

        when:
        parser.parse(compile_statement)

        then:
        1 * formatter.formatCompileCommand(compile_statement.trim(), "/Users/musalj/code/OSS/Kiwi/Classes/Core/KWNull.m")
    }

    def "parses compiling categories"() {
        when:
        parser.parse(SAMPLE_COMPILE)

        then:
        1 * formatter.formatCompile("NSMutableArray+ObjectiveSugar.m", "/Users/musalj/code/OSS/ObjectiveSugar/Classes/NSMutableArray+ObjectiveSugar.m")

    }

    def "parses compiling classes"() {
        when:
        parser.parse(SAMPLE_ANOTHER_COMPILE)

        then:
        1 * formatter.formatCompile("KWNull.m", "Classes/Core/KWNull.m")
    }

    def "parses compiling Objective-C++ classes"() {
        when:
        parser.parse(SAMPLE_ANOTHER_COMPILE.replace('.m', '.mm'))

        then:
        1 * formatter.formatCompile("KWNull.mm", "Classes/Core/KWNull.mm")
    }

    def "parses compiling Swift source files"() {
        when:
        parser.parse(SAMPLE_SWIFT_COMPILE)

        then:
        1 * formatter.formatCompile("Resource.swift", "/Users/paul/foo/bar/siesta/Source/Resource.swift")
    }

    @Unroll("parses compiling C and C++ files with #extension")
    def "parses compiling C and C++ files"() {
        when:
        parser.parse(SAMPLE_ANOTHER_COMPILE.replace('.m', fileExtension))

        then:
        1 * formatter.formatCompile("KWNull" + fileExtension, "Classes/Core/KWNull" + fileExtension)

        where:
        fileExtension << ['.c', '.cc', '.cpp', '.cxx']
    }

    def "parses compiling XIBs"() {
        when:
        parser.parse(SAMPLE_COMPILE_XIB)

        then:
        1 * formatter.formatCompileXib("MainMenu.xib", "CocoaChip/en.lproj/MainMenu.xib")
    }

    def "parses compiling storyboards"() {
        when:
        parser.parse(SAMPLE_COMPILE_STORYBOARD)

        then:
        1 * formatter.formatCompileStoryboard("Main.storyboard", "sample/Main.storyboard")
    }

    def "parses CopyPlistFile"() {
        when:
        parser.parse('CopyPlistFile /path/to/Some.plist /some other/File.plist')

        then:
        1 * formatter.formatCopyPlistFile('/path/to/Some.plist', '/some other/File.plist')
    }

    def "parses CopyStringsFile"() {
        when:
        parser.parse(SAMPLE_COPYSTRINGS)

        then:
        1 * formatter.formatCopyStringsFile('InfoPlist.strings')
    }

    def "parses CpHeader"() {
        when:
        parser.parse('CpHeader /path/to/Header.h /some other/path/Header.h')

        then:
        1 * formatter.formatCopyHeaderFile('/path/to/Header.h', '/some other/path/Header.h')
    }

    def "parses CpResource"() {
        when:
        parser.parse(SAMPLE_CPRESOURCE)

        then:
        1 * formatter.formatCpresource('ObjectiveSugar/Default-568h@2x.png')
    }

    def "parses GenerateDSYMFile"() {
        when:
        parser.parse(SAMPLE_DSYM)

        then:
        1 * formatter.formatGenerateDsym('ObjectiveSugarTests.octest.dSYM')
    }

    def "parses info.plist processing"() {
        when:
        parser.parse(SAMPLE_PROCESS_INFOPLIST)

        then:
        1 * formatter.formatProcessInfoPlist('The Spacer-Info.plist', 'The Spacer/The Spacer-Info.plist')
    }

    def "parses Ld"() {
        when:
        parser.parse(SAMPLE_LD)

        then:
        1 * formatter.formatLinking('ObjectiveSugar', 'normal', 'i386')
    }

    def "parses Ld with relative path"() {
        when:
        parser.parse(SAMPLE_LD_RELATIVE)

        then:
        1 * formatter.formatLinking('ObjectiveSugar', 'normal', 'i386')
    }

    def "parses Libtool"() {
        when:
        parser.parse(SAMPLE_LIBTOOL)

        then:
        1 * formatter.formatLibtool('libPods-ObjectiveSugarTests-Kiwi.a')
    }

    def "parses uitest failing tests"() {
        when:
        SAMPLE_UITEST_CASE_WITH_FAILURE.eachLine {
            parser.parse(it)
        }

        then:
        1 * formatter.formatFailingTest(
                "viewUITests.vmtAboutWindow",
                "testConnectToDesktop",
                "UI Testing Failure - Unable to find hit point for element Button 0x608001165880: {{74.0, -54.0}, {44.0, 38.0}}, label: 'Disconnect'",
                "<unknown>:0"
        )
    }

    def "parses specta failing tests"() {
        when:
        SAMPLE_SPECTA_FAILURE.eachLine {
            parser.parse(it)
        }

        then:
        1 * formatter.formatFailingTest(
                "SKWelcomeViewControllerSpecSpec",
                "SKWelcomeViewController_When_a_user_opens_the_app_from_a_clean_installation_displays_the_welcome_screen",
                "The step timed out after 2.00 seconds: Failed to find accessibility element with the label \"The asimplest way to make smarter business decisions\"",
                "/Users/vickeryj/Code/ipad-register/KIFTests/Specs/SKWelcomeViewControllerSpec.m:11")
    }

    def "parses old specta failing tests"() {
        when:
        parser.parse(SAMPLE_OLD_SPECTA_FAILURE)

        then:
        1 * formatter.formatFailingTest("RACCommandSpec",
                "enabled_signal_should_send_YES_while_executing_is_YES_and_allowsConcurrentExecution_is_YES",
                "expected: 1, got: 0",
                "/Users/musalj/code/OSS/ReactiveCocoa/ReactiveCocoaFramework/ReactiveCocoaTests/RACCommandSpec.m:458")
    }

    def "parses ld bitcode errors"() {
        when:
        parser.parse(SAMPLE_BITCODE_LD)

        then:
        1 * formatter.formatError(SAMPLE_BITCODE_LD.trim())
    }

    def "parses passing ocunit tests"() {
        when:
        parser.parse(SAMPLE_OCUNIT_TEST)

        then:
        1 * formatter.formatPassingTest('RACCommandSpec',
                'enabled_signal_should_send_YES_while_executing_is_YES',
                '0.001')
    }

    def "parses passing specta tests"() {
        when:
        parser.parse(SAMPLE_SPECTA_TEST)

        then:
        1 * formatter.formatPassingTest('SKWelcomeActivationViewControllerSpecSpec',
                'SKWelcomeActivationViewController_When_a_user_enters_their_details_lets_them_enter_a_valid_manager_code',
                '0.725')
    }

    def "parses pending tests"() {
        when:
        parser.parse(SAMPLE_PENDING_KIWI_TEST)

        then:
        1 * formatter.formatPendingTest('TAPIConversationSpec',
                'TAPIConversation_createConversation_SendsAPOSTRequestToTheConversationsEndpoint')

    }

    def "parses measuring tests"() {
        when:
        parser.parse(SAMPLE_MEASURING_TEST)

        then:
        1 * formatter.formatMeasuringTest(
                'SecEncodeTransformTests.SecEncodeTransformTests',
                'test_RFC4648_Decode_UsingBase32',
                '0.013')
    }

    def "parses build success indicator"() {
        when:
        parser.parse(SAMPLE_BUILD_SUCCEEDED)

        then:
        1 * formatter.formatPhaseSuccess('BUILD')
    }

    def "parses clean success indicator"() {
        when:
        parser.parse(SAMPLE_CLEAN_SUCCEEDED)

        then:
        1 * formatter.formatPhaseSuccess('CLEAN')
    }

    def "parses PhaseScriptExecution"() {
        when:
        parser.parse(SAMPLE_RUN_SCRIPT)

        then:
        1 * formatter.formatPhaseScriptExecution('Check Pods Manifest.lock')
    }

    def "parses process PCH"() {
        when:
        parser.parse(SAMPLE_PRECOMPILE)

        then:
        1 * formatter.formatProcessPch("Pods-CocoaLumberjack-prefix.pch")
    }

    def "parses process PCH command"() {
        def compileStatement = SAMPLE_PRECOMPILE.readLines().last()
        when:
        parser.parse(compileStatement)

        then:
        1 * formatter.formatProcessPchCommand("/Users/musalj/code/OSS/ObjectiveRecord/Pods/Pods-CocoaLumberjack-prefix.pch")
    }

    def "parses preprocessing"() {
        when:
        parser.parse(SAMPLE_PREPROCESS)

        then:
        1 * formatter.formatPreprocess("CocoaChip/CocoaChip-Info.plist")
    }

    def "parses PBXCp"() {
        when:
        parser.parse(SAMPLE_PBXCP)

        then:
        1 * formatter.formatPbxcp("build/Release/CocoaChipCore.framework")
    }

    def "parses changing directories"() {
        when:
        parser.parse('    cd /some/place/out\\ there')

        then:
        1 * formatter.formatShellCommand('cd',
                '/some/place/out\\ there')
    }

    def "parses any indented command"() {
        when:
        parser.parse('    /bin/rm -rf /bin /usr /Users')

        then:
        1 * formatter.formatShellCommand('/bin/rm', '-rf /bin /usr /Users')
    }

    def "parses Touch"() {
        when:
        parser.parse(SAMPLE_TOUCH)

        then:
        1 * formatter.formatTouch(
                '/Users/musalj/Library/Developer/Xcode/DerivedData/Alcatraz-aobuxcinaqyzjugrnxjjhfzgwaou/Build/Products/Debug/Alcatraz Tests.octest',
                'Alcatraz Tests.octest')
    }

    def "parses write file"() {
        when:
        parser.parse(SAMPLE_WRITE_FILE)

        then:
        1 * formatter.formatWriteFile('/Users/me/myproject/Build/Intermediates/Pods.build/Debug-iphonesimulator/Pods-AFNetworking.build/Objects-normal/x86_64/Pods-AFNetworking.LinkFileList')
    }

    def "parses write auxiliary files"() {
        when:
        parser.parse(SAMPLE_WRITE_AUXILIARY_FILES)

        then:
        1 * formatter.formatWriteAuxiliaryFiles()
    }

    def "parses TiffUtil"() {
        when:
        parser.parse(SAMPLE_TIFFUTIL)

        then:
        1 * formatter.formatTiffutil('eye_icon.tiff')
    }

    def "parses undefined symbols"() {
        when:
        SAMPLE_UNDEFINED_SYMBOLS.eachLine {
            parser.parse(it)
        }

        then:
        1 * formatter.formatUndefinedSymbols("Undefined symbols for architecture x86_64",
                '_OBJC_CLASS_$_CABasicAnimation',
                'objc-class-ref in ATZRadialProgressControl.o')
    }

    def "parses duplicate symbols"() {

        when:
        SAMPLE_DUPLICATE_SYMBOLS.eachLine {
            parser.parse(it)
        }

        then:
        1 * formatter.formatDuplicateSymbols('duplicate symbol _OBJC_IVAR_$ClassName._ivarName in',
                [
                        '/Users/username/Library/Developer/Xcode/DerivedData/App-arcyyktezaigixbocjwfhsjllojz/Build/Intermediates/App.build/Debug-iphonesimulator/App.build/Objects-normal/i386/ClassName.o',
                        '/Users/username/Library/Developer/Xcode/DerivedData/App-arcyyktezaigixbocjwfhsjllojz/Build/Products/Debug-iphonesimulator/libPods.a(DuplicateClassName.o)'
                ])
    }

    def "parses ocunit test run finished"() {
        when:
        parser.parse(SAMPLE_OCUNIT_TEST_RUN_COMPLETION)

        then:
        1 * formatter.formatTestRunFinished('ReactiveCocoaTests.octest(Tests)', '2013-12-10 07:03:03 +0000.')
    }

    def "parses ocunit test run passed"() {
        when:
        parser.parse(SAMPLE_OCUNIT_PASSED_TEST_RUN_COMPLETION)

        then:
        1 * formatter.formatTestRunFinished('Hazelnuts.xctest', '2014-09-24 23:09:20 +0000.')
    }

    def "parses ocunit test run failed"() {
        when:
        parser.parse(SAMPLE_OCUNIT_FAILED_TEST_RUN_COMPLETION)

        then:
        1 * formatter.formatTestRunFinished('Macadamia.octest', '2014-09-24 23:09:20 +0000.')
    }

    def "parses specta test run finished"() {
        when:
        parser.parse(SAMPLE_SPECTA_TEST_RUN_COMPLETION)

        then:
        1 * formatter.formatTestRunFinished('KIFTests.xctest', '2014-02-28 15:44:32 +0000.')
    }

    def "parses ocunit test run started"() {
        when:
        parser.parse(SAMPLE_OCUNIT_TEST_RUN_BEGINNING)

        then:
        1 * formatter.formatTestRunStarted('ReactiveCocoaTests.octest(Tests)')
    }

    def "parses specta test run started"() {
        when:
        parser.parse(SAMPLE_SPECTA_TEST_RUN_BEGINNING)

        then:
        1 * formatter.formatTestRunStarted('KIFTests.xctest')
    }

    def "parses ocunit test suite started"() {
        when:
        parser.parse(SAMPLE_OCUNIT_SUITE_BEGINNING)

        then:
        1 * formatter.formatTestSuiteStarted('RACKVOWrapperSpec')
    }

    def "parses specta test suite started"() {
        when:
        parser.parse(SAMPLE_SPECTA_SUITE_BEGINNING)

        then:
        1 * formatter.formatTestSuiteStarted('All tests')
    }

    def "parses unknown strings"() {
        when:
        parser.parse(SAMPLE_FORMAT_OTHER_UNRECOGNIZED_STRING)

        then:
        1 * formatter.formatOther(SAMPLE_FORMAT_OTHER_UNRECOGNIZED_STRING)
    }

    // Errors

    def "parses clang errors"() {
        when:
        parser.parse(SAMPLE_CLANG_ERROR)

        then:
        1 * formatter.formatError(SAMPLE_CLANG_ERROR)
    }

    def "parses cocoapods errors"() {
        when:
        parser.parse(SAMPLE_PODS_ERROR)

        then:
        1 * formatter.formatError("error: The sandbox is not in sync with the Podfile.lock. Run 'pod install' or update your CocoaPods installation.")
    }

    def "parses compiling errors"() {
        when:
        SAMPLE_COMPILE_ERROR.eachLine {
            parser.parse(it)
        }

        then:
        1 * formatter.formatCompileError("SampleTest.m",
                "/Users/musalj/code/OSS/SampleApp/SampleTest.m:12:59",
                "expected identifier",
                "                [[thread.lastMessage should] equal:thread.];",
                "                                                          ^")
    }


    def 'parses fatal compiling errors'() {
        when:
        SAMPLE_FATAL_COMPILE_ERROR.eachLine {
            parser.parse(it)
        }

        then:
        1 * formatter.formatCompileError(
                'SomeRandomClass.h',
                '/Users/musalj/code/OSS/SampleApp/Pods/Headers/LessCoolPod/SomeRandomClass.h:31:9',
                "'SomeRandomHeader.h' file not found",
                '#import "SomeRandomHeader.h"',
                '        ^'
        )
    }

    def 'parses file missing errors'() {
        when:
        parser.parse(SAMPLE_FILE_MISSING_ERROR)

        then:
        1 * formatter.formatFileMissingError('error: no such file or directory:', '/Users/travis/build/supermarin/project/Classes/Class + Category/Two Words/MissingViewController.swift')
    }

    def 'parses fatal error: on the beginning of the line for corrupted AST files'() {
        when:
        parser.parse(SAMPLE_FATAL_HEADER_ERROR)

        then:
        1 * formatter.formatError("fatal error: malformed or corrupted AST file: 'could not find file '/Users/mpv/dev/project/Crashlytics.framework/Headers/Crashlytics.h' referenced by AST file' note: after modifying system headers, please delete the module cache at '/Users/mpv/Library/Developer/Xcode/DerivedData/ModuleCache/M5WJ0FYE7N06'")
    }

    def 'parses fatal error: on the beginning of the line for cached PCH'() {
        when:
        parser.parse(SAMPLE_FATAL_COMPILE_PCH_ERROR)

        then:
        1 * formatter.formatError("fatal error: file '/path/to/myproject/Pods/Pods-environment.h' has been modified since the precompiled header '/Users/hiroshi/Library/Developer/Xcode/DerivedData/MyProject-gfmuvpipjscewkdnqacgumhfarrd/Build/Intermediates/PrecompiledHeaders/MyProject-Prefix-dwjpvcnrlaydzmegejmcvrtcfkpf/MyProject-Prefix.pch.pch' was built")
    }

    def "parses compiling errors with tildes"() {
        when:
        SAMPLE_COMPILE_ERROR_WITH_TILDES.eachLine {
            parser.parse(it)
        }

        then:
        1 * formatter.formatCompileError(
                'NSSetTests.m',
                '/Users/musalj/code/OSS/ObjectiveSugar/Example/ObjectiveSugarTests/NSSetTests.m:93:16',
                "no visible @interface for 'NSArray' declares the selector 'shoulds'",
                '            }] shoulds] equal:@[ @"F458 Italia", @"Testarossa" ]];',
                '            ~~ ^~~~~~~')
    }

    def "parses code sign error:"() {
        when:
        parser.parse(SAMPLE_CODESIGN_ERROR)

        then:
        1 * formatter.formatError('Code Sign error: No code signing identites found: No valid signing identities (i.e. certificate and private key pair) matching the team ID ‚ÄúCAT6HF57NJ‚Äù were found.')
    }

    def "parses CodeSign error: (no spaces)"() {
        when:
        parser.parse(SAMPLE_CODESIGN_ERROR_NO_SPACES)

        then:
        1 * formatter.formatError("CodeSign error: code signing is required for product type 'Application' in SDK 'iOS 7.0'")
    }

    def "parses No profile matching error:"() {
        when:
        parser.parse(SAMPLE_NO_PROFILE_MATCHING_ERROR)

        then:
        1 * formatter.formatError(SAMPLE_NO_PROFILE_MATCHING_ERROR)
    }

    def "parses requires provision error:"() {
        when:
        parser.parse(SAMPLE_REQUIRES_PROVISION)

        then:
        1 * formatter.formatError('PROJECT_NAME requires a provisioning profile. Select a provisioning profile for the "Debug" build configuration in the project editor.')
    }

    def "parses no certificate error:"() {
        when:
        parser.parse(SAMPLE_NO_CERTIFICATE)

        then:
        1 * formatter.formatError("No certificate matching 'iPhone Distribution: Name (B89SBB0AV9)' for team 'B89SBB0AV9':  Select a different signing certificate for CODE_SIGN_IDENTITY, a team that matches your selected certificate, or switch to automatic provisioning.")
    }

    def "parses swift unavailable error:"() {
        when:
        parser.parse(SAMPLE_SWIFT_UNAVAILABLE)

        then:
        1 * formatter.formatError(SAMPLE_SWIFT_UNAVAILABLE)
    }

    def "parses use legacy swift error:"() {
        when:
        parser.parse(SAMPLE_USE_LEGACY_SWIFT)

        then:
        1 * formatter.formatError(SAMPLE_USE_LEGACY_SWIFT)
    }

    def "parses ld library errors"() {
        when:
        parser.parse(SAMPLE_LD_LIBRARY_ERROR)

        then:
        1 * formatter.formatError(SAMPLE_LD_LIBRARY_ERROR)
    }

    def 'parses ld symbols errors'() {
        when:
        parser.parse(SAMPLE_LD_SYMBOLS_ERROR)

        then:
        1 * formatter.formatError(SAMPLE_LD_SYMBOLS_ERROR)
    }

    def "doesn't print the same error over and over"() {
        given:
        SAMPLE_COMPILE_ERROR.eachLine {
            parser.parse(it)
        }

        when:
        parser.parse("hohohoooo")

        then:
        0 * formatter.formatCompileError(*_)
    }

    def "parses provisioning profile doesn't support capability error"() {
        when:
        parser.parse(SAMPLE_PROFILE_DOESNT_SUPPORT_CAPABILITY_ERROR)

        then:
        1 * formatter.formatError(SAMPLE_PROFILE_DOESNT_SUPPORT_CAPABILITY_ERROR)
    }

    def "parses provisioning profile doesn't include entitlement error"() {
        when:
        parser.parse(SAMPLE_PROFILE_DOESNT_INCLUDE_ENTITLEMENT_ERROR)

        then:
        1 * formatter.formatError(SAMPLE_PROFILE_DOESNT_INCLUDE_ENTITLEMENT_ERROR)
    }

    def "parses code signing is required error"() {
        when:
        parser.parse(SAMPLE_CODE_SIGNING_IS_REQUIRED_ERROR)

        then:
        1 * formatter.formatError(SAMPLE_CODE_SIGNING_IS_REQUIRED_ERROR)
    }

    def "parses module includes error"() {
        when:
        parser.parse(SAMPLE_MODULE_INCLUDES_ERROR)

        then:
        1 * formatter.formatError("error: umbrella header for module 'ModuleName' does not include header 'Header.h'")
    }

    // Warnings

    def 'parses compiler warnings'() {
        when:
        parser.parse("warning: TEST 123")

        then:
        1 * formatter.formatWarning("TEST 123")
    }

    def "parses compiling warnings"() {
        when:
        SAMPLE_FORMAT_WARNING.eachLine {
            parser.parse(it)
        }

        then:
        1 * formatter.formatCompileWarning(
                "AppDelegate.m",
                "/Users/supermarin/code/oss/ObjectiveSugar/Example/ObjectiveSugar/AppDelegate.m:19:31",
                "format specifies type 'id' but the argument has type 'int' [-Wformat]",
                "    NSLog(@\"I HAZ %@ CATS\", 1);",
                "                         ~~   ^")

    }

    def "parses ld warnings"() {
        when:
        parser.parse("ld: warning: embedded dylibs/frameworks only run on iOS 8 or later")

        then:
        1 * formatter.formatLdWarning("ld: embedded dylibs/frameworks only run on iOS 8 or later")
    }

    def "parses will not be code signed warnings"() {
        when:
        parser.parse(SAMPLE_WILL_NOT_BE_CODE_SIGNED)

        then:
        1 * formatter.formatWillNotBeCodeSigned(SAMPLE_WILL_NOT_BE_CODE_SIGNED)
    }

    // summary

    def givenTestsHaveStarted(String reporter = SAMPLE_OCUNIT_TEST_RUN_BEGINNING) {
        parser.parse(reporter)
    }

    def givenTestsAreDone(String reporter = SAMPLE_OCUNIT_TEST_RUN_COMPLETION) {
        parser.parse(reporter)
    }

    def givenKiwiTestsAreDone() {
        parser.parse(SAMPLE_KIWI_TEST_RUN_COMPLETION)
        parser.parse(SAMPLE_EXECUTED_TESTS)
        parser.parse(SAMPLE_KIWI_SUITE_COMPLETION)
    }

    def "returns empty string if the suite is not done"() {
        expect:
        parser.parse(SAMPLE_EXECUTED_TESTS) == ""
    }

    def "knows when the test suite is done for OCunit"() {
        given:
        givenTestsAreDone()

        when:
        parser.parse(SAMPLE_EXECUTED_TESTS)

        then:
        1 * formatter.formatTestSummary(*_)
    }


    def "knows when the test suite is done for Specta"() {
        given:
        givenTestsAreDone()

        when:
        parser.parse(SAMPLE_SPECTA_EXECUTED_TESTS)

        then:
        1 * formatter.formatTestSummary(*_)
    }

    def "doesn't print executed message twice for Kiwi tests"() {
        when:
        givenTestsHaveStarted(SAMPLE_KIWI_TEST_RUN_BEGINNING)
        givenKiwiTestsAreDone()

        then:
        1 * formatter.formatTestSummary(*_)
    }


    def "knows when the test suite is done for XCtest"() {
        when:
        2.times {
            givenTestsAreDone(SAMPLE_KIWI_TEST_RUN_COMPLETION)
            parser.parse(SAMPLE_EXECUTED_TESTS)
        }

        then:
        1 * formatter.formatTestSummary(*_)
    }

    def "prints OCunit / XCTest summary twice if tests executed twice"() {
        when:
        2.times {
            givenTestsHaveStarted()
            givenTestsAreDone()
            parser.parse(SAMPLE_EXECUTED_TESTS)
        }

        then:
        2 * formatter.formatTestSummary(*_)
    }


    def "prints Kiwi summary twice if tests executed twice"() {
        when:
        2.times {
            givenTestsHaveStarted(SAMPLE_KIWI_TEST_RUN_BEGINNING)
            givenKiwiTestsAreDone()
        }

        then:
        2 * formatter.formatTestSummary(*_)
    }
}
