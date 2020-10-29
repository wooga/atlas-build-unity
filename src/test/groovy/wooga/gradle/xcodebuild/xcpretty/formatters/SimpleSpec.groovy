package wooga.gradle.xcodebuild.xcpretty.formatters

import spock.lang.Requires
import spock.lang.Unroll

@Requires({ os.macOs })
class SimpleSpec extends FormatterSpec<Simple> {

    @Override
    Class<Simple> getFormatterClass() {
        Simple.class
    }

    def "formats analyzing"() {
        expect:
        formatter.formatAnalyze("CCChip8DisplayView.m", 'path/to/file') == "> Analyzing CCChip8DisplayView.m"
    }

    def "formats build target/project/configuration with target"() {
        expect:
        formatter.formatBuildTarget("The Spacer", "Pods", "Debug") == "> Building Pods/The Spacer [Debug]"
    }

    def "formats build target/project/configuration with target_"() {
        expect:
        formatter.formatAggregateTarget("Be Aggro", "AggregateExample", "Debug") == "> Aggregate AggregateExample/Be Aggro [Debug]"
    }

    def "formats analyze target/project/configuration with target"() {
        expect:
        formatter.formatAnalyzeTarget("The Spacer", "Pods", "Debug") == "> Analyzing Pods/The Spacer [Debug]"
    }

    def "formats clean target/project/configuration"() {
        expect:
        formatter.formatCleanTarget("Pods-ObjectiveSugar", "Pods", "Debug") == "> Cleaning Pods/Pods-ObjectiveSugar [Debug]"
    }

    def 'formats compiler warnings'() {
        given:
        def warning = 'warning: stuff is broken'

        expect:
        formatter.formatWarning(warning) == '    ' + warning
    }

    def "formats compiling output"() {
        expect:
        formatter.formatCompile("NSMutableArray+ObjectiveSugar.m", 'path/to/file') == "> Compiling NSMutableArray+ObjectiveSugar.m"
    }

    def "formats compiling xib output"() {
        expect:
        formatter.formatCompileXib("MainMenu.xib", 'path/to/file') == "> Compiling MainMenu.xib"
    }

    def "formats compiling storyboard output"() {
        expect:
        formatter.formatCompileXib("Main.storyboard", 'path/to/file') == "> Compiling Main.storyboard"
    }

    def 'formats copying header files'() {
        expect:
        formatter.formatCopyHeaderFile('Source.h', 'dir/Destination.h') == '> Copying Source.h'
    }

    def 'formats copying plist files'() {
        expect:
        formatter.formatCopyPlistFile("Source.plist", 'dir/Destination.plist') == '> Copying Source.plist'
    }

    def "formats copy resource"() {
        expect:
        formatter.formatCpresource("ObjectiveSugar/Default-568h@2x.png") == "> Copying ObjectiveSugar/Default-568h@2x.png"
    }

    def "formats Copy strings file"() {
        expect:
        formatter.formatCopyStringsFile("InfoPlist.strings") == "> Copying InfoPlist.strings"
    }

    def "formats GenerateDSYMFile"() {
        expect:
        formatter.formatGenerateDsym("ObjectiveSugarTests.octest.dSYM") == "> Generating 'ObjectiveSugarTests.octest.dSYM'"
    }

    def "formats info.plist processing"() {
        expect:
        formatter.formatProcessInfoPlist("The Spacer-Info.plist", "The Spacer/The Spacer-Info.plist") == "> Processing The Spacer-Info.plist"
    }

    def "formats Linking"() {
        expect:
        formatter.formatLinking("ObjectiveSugar", 'normal', 'i386') == "> Linking ObjectiveSugar"
    }

    def "formats Libtool"() {
        expect:
        formatter.formatLibtool("libPods-ObjectiveSugarTests-Kiwi.a") == "> Building library libPods-ObjectiveSugarTests-Kiwi.a"
    }

    def "formats failing tests"() {
        expect:
        formatter.formatFailingTest("RACCommandSpec", "enabled_signal_should_send_YES_while_executing_is_YES_and_allowsConcurrentExecution_is_YES", "expected: 1, got: 0", 'path/to/file') == "    x enabled_signal_should_send_YES_while_executing_is_YES_and_allowsConcurrentExecution_is_YES, expected: 1, got: 0"
    }

    def "formats passing tests"() {
        expect:
        formatter.formatPassingTest("RACCommandSpec", "_tupleByAddingObject__should_add_a_non_nil_object", "0.001") == "    . _tupleByAddingObject__should_add_a_non_nil_object (0.001 seconds)"
    }

    def "formats pending tests"() {
        expect:
        formatter.formatPendingTest("RACCommandSpec", "_tupleByAddingObject__should_add_a_non_nil_object") == "    P _tupleByAddingObject__should_add_a_non_nil_object [PENDING]"
    }

    def "formats measuring tests"() {
        expect:
        formatter.formatMeasuringTest("RACCommandSpec", "_tupleByAddingObject__should_add_a_non_nil_object", "0.001") == "    T _tupleByAddingObject__should_add_a_non_nil_object measured (0.001 seconds)"
    }

    def "formats build success output"() {
        expect:
        formatter.formatPhaseSuccess("BUILD") == "> Build Succeeded"
    }

    def "formats clean success output"() {
        expect:
        formatter.formatPhaseSuccess("CLEAN") == "> Clean Succeeded"
    }

    def "formats Phase Script Execution"() {
        expect:
        formatter.formatPhaseScriptExecution("Check Pods Manifest.lock") == "> Running script 'Check Pods Manifest.lock'"
    }

    def "formats precompiling output"() {
        expect:
        formatter.formatProcessPch("Pods-CocoaLumberjack-prefix.pch") == "> Precompiling Pods-CocoaLumberjack-prefix.pch"
    }

    def "formats code signing"() {
        expect:
        formatter.formatCodesign("build/Release/CocoaChip.app") == "> Signing build/Release/CocoaChip.app"
    }

    def "formats preprocessing a file"() {
        expect:
        formatter.formatPreprocess("CocoaChip/CocoaChip-Info.plist") == "> Preprocessing CocoaChip/CocoaChip-Info.plist"
    }

    def "formats PBXCp"() {
        expect:
        formatter.formatPbxcp("build/Release/CocoaChipCore.framework") == "> Copying build/Release/CocoaChipCore.framework"
    }

    def "formats test run start"() {
        expect:
        formatter.formatTestRunStarted("ReactiveCocoaTests.octest(Tests)") == "Test Suite ReactiveCocoaTests.octest(Tests) started"
    }

    def "formats tests suite started"() {
        expect:
        formatter.formatTestSuiteStarted("RACKVOWrapperSpec") == "RACKVOWrapperSpec"
    }

    def "formats Touch"() {
        expect:
        formatter.formatTouch("/path/to/SomeFile.txt", "SomeFile.txt") == "> Touching SomeFile.txt"
    }

    def "formats TiffUtil"() {
        expect:
        formatter.formatTiffutil("unbelievable.tiff") == "> Validating unbelievable.tiff"
    }

    def 'formats Check Dependencies'() {
        expect:
        formatter.formatCheckDependencies() == '> Check Dependencies'
    }

    @Unroll("formatter not implemented for format method #method")
    def "formatter not implemented"() {
        expect:
        formatter.invokeMethod(method, arguments) == ""

        where:
        method                      | arguments
        'formatClean'               | ["/path/to/project.xcodeprj", "target", "Debug"]
        'formatCleanRemove'         | null
        'formatCompileCommand'      | ["Command", "/path/to/file"]
        'formatProcessPchCommand'   | ["/path/to/file"]
        'formatShellCommand'        | ["sh", "-v --help"]
        'formatTestRunFinished'     | ["Test", "0.002"]
        'formatWriteFile'           | ["/some/file"]
        'formatWriteAuxiliaryFiles' | null
    }

}
