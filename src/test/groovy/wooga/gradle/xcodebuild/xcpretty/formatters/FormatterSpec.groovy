package wooga.gradle.xcodebuild.xcpretty.formatters

import spock.lang.Requires
import spock.lang.Specification
import wooga.gradle.xcodebuild.xcpretty.Parser

import static wooga.gradle.xcodebuild.XcodeFixtures.*

@Requires({ os.macOs })
abstract class FormatterSpec<F extends Formatter> extends Specification {

    abstract Class<F> getFormatterClass()

    F formatter
    F unicodeFormatter

    def setup() {
        formatter = formatterClass.newInstance(false, false)
        unicodeFormatter = formatterClass.newInstance(true, true)
    }

    def "initializes with unicode"() {
        expect:
        unicodeFormatter.useUnicode
        !formatter.useUnicode
    }

    def "initializes with color"() {
        unicodeFormatter.colorize
        !formatter.colorize
    }

    def "outputs to new lines by default"() {
        expect:
        unicodeFormatter.optionalNewline == "\n"
        formatter.optionalNewline == "\n"
    }

    def "formats cocoapods errors"() {
        expect:
        unicodeFormatter.formatError("The sandbox is not in sync...") ==
                "\n${unicodeFormatter.red("❌  The sandbox is not in sync...")}\n\n"
    }

    def "formats compiling errors"() {
        expect:
        def result = unicodeFormatter.formatCompileError("file",
                "path/to/file",
                "expected valid syntax",
                "[a should",
                "         ^")
        result == """
            ${unicodeFormatter.red('❌  ')}path/to/file: ${unicodeFormatter.red("expected valid syntax")}

            [a should
            ${unicodeFormatter.cyan('         ^')}

            """.stripIndent()
    }

    def "formats file missing errors"() {
        expect:
        def result = unicodeFormatter.formatFileMissingError("error: no such file or directory:",
                "/path/to/file.swift")
        result == """
            ${unicodeFormatter.red('❌  error: no such file or directory:')} /path/to/file.swift

            """.stripIndent()
    }

    def "formats compiling warnings"() {
        expect:
        def reason = "format specifies type 'id' but the argument has type 'int' [-Wformat]"

        def result = unicodeFormatter.formatCompileWarning("file", "path/to/file", reason,
                '    NSLog(@"alsdkflsakdj %@", 1);',
                '                         ~~   ^')

        result == """
        ${unicodeFormatter.yellow('⚠️  ')}path/to/file: ${unicodeFormatter.yellow(reason)}

            NSLog(@"alsdkflsakdj %@", 1);
        ${unicodeFormatter.cyan('                         ~~   ^')}

        """.stripIndent()
    }

    def "formats linker warnings"() {
        expect:
        unicodeFormatter.formatLdWarning("ld: embedded dylibs/frameworks only run on iOS 8 or later") ==
                "${unicodeFormatter.yellow("⚠️  ld: embedded dylibs/frameworks only run on iOS 8 or later")}"
    }

    def "formats linker undefined symbols by default"() {
        expect:
        def result = unicodeFormatter.formatUndefinedSymbols("Undefined symbols for architecture x86_64",
                '_OBJC_CLASS_$_CABasicAnimation',
                'objc-class-ref in ATZRadialProgressControl.o')
        result == """
            ${unicodeFormatter.red("❌  Undefined symbols for architecture x86_64")}
            > Symbol: _OBJC_CLASS_\$_CABasicAnimation
            > Referenced from: objc-class-ref in ATZRadialProgressControl.o

            """.stripIndent()
    }

    def "formats linker duplicate symbols by default"() {
        expect:
        def result = unicodeFormatter.formatDuplicateSymbols("duplicate symbol _OBJC_IVAR_\$ClassName._ivarName in",
                ['/Users/username/Library/Developer/Xcode/DerivedData/App-arcyyktezaigixbocjwfhsjllojz/Build/Intermediates/App.build/Debug-iphonesimulator/App.build/Objects-normal/i386/ClassName.o',
                 '/Users/username/Library/Developer/Xcode/DerivedData/App-arcyyktezaigixbocjwfhsjllojz/Build/Products/Debug-iphonesimulator/libPods.a(DuplicateClassName.o)'])
        result == """
            ${unicodeFormatter.red("❌  duplicate symbol _OBJC_IVAR_\$ClassName._ivarName in")}
            > ClassName.o
            > libPods.a(DuplicateClassName.o)

            """.stripIndent()
    }

    def "formats will not be code signed warnings"() {
        expect:
        unicodeFormatter.formatWillNotBeCodeSigned(SAMPLE_WILL_NOT_BE_CODE_SIGNED) ==
                """${unicodeFormatter.yellow("⚠️  FrameworkName will not be code signed because its settings don't specify a development team.")}"""
    }

    def "formats failures per suite"() {
        given:
        def resource = FormatterSpec.class.getResource("/fixtures/NSStringTests.m").path
        def firstPath = "${resource}:46".toString()
        def secondPath = "${resource}:57".toString()

        def failures = [
                'CarSpec'   : [new Parser.Failure(firstPath, "just doesn't work", 'Starting the car')],
                'StringSpec': [new Parser.Failure(secondPath, "doesn't split", 'Splitting the string')],
                'UI spec'   : [new Parser.Failure("<unknown.m>:0", "ui test failed", 'yolo')]
        ]

        expect:
        def result = unicodeFormatter.formatTestSummary(SAMPLE_EXECUTED_TESTS, failures)

        result == """
        
        CarSpec
          Starting the car, ${unicodeFormatter.red("just doesn't work")}
          ${unicodeFormatter.cyan(firstPath)}
          ```
                it(@"converts snake_cased to CamelCased", ^{
                    [[[@"snake_case" camelCase] should] equal:@"SnakeCase"];
                });
          ```

        StringSpec
          Splitting the string, ${unicodeFormatter.red("doesn't split")}
          ${unicodeFormatter.cyan(secondPath)}
          ```
            it(@"-strip strips whitespaces and newlines from both ends", ^{
                [[[@"  Look mo, no empties!   " strip] should] equal:@"Look mo, no empties!"];
            });
          ```

        UI spec
          yolo, ${unicodeFormatter.red("ui test failed")}
          ${unicodeFormatter.cyan("<unknown.m>:0")}
        
        
        ${unicodeFormatter.red(SAMPLE_EXECUTED_TESTS)}""".stripIndent()
    }
}
