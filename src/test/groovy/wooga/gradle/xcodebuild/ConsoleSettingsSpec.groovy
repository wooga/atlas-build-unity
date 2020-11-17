package wooga.gradle.xcodebuild

import org.gradle.api.logging.configuration.ConsoleOutput
import spock.lang.Specification

class ConsoleSettingsSpec extends Specification {

    def "can create console Settings from gradle console output"() {
        given:
        def consoleSettings = ConsoleSettings.fromGradleOutput(output)

        expect:
        consoleSettings.prettyPrint == expectedPrettyPrint
        consoleSettings.useUnicode == expectedUseColors
        consoleSettings.colorize == expectedColorize
        consoleSettings.hasColors() == expectedHasColors

        where:
        output                | expectedPrettyPrint | expectedUseColors | expectedColorize                   | expectedHasColors
        ConsoleOutput.Rich    | true                | true              | ConsoleSettings.ColorOption.always | true
        ConsoleOutput.Plain   | true                | false             | ConsoleSettings.ColorOption.never  | false
        ConsoleOutput.Verbose | false               | false             | ConsoleSettings.ColorOption.never  | false
        ConsoleOutput.Auto    | true                | true              | ConsoleSettings.ColorOption.auto   | true
    }
}
