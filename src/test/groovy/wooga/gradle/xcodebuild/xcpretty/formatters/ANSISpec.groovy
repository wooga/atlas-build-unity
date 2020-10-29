package wooga.gradle.xcodebuild.xcpretty.formatters

import spock.lang.Shared
import spock.lang.Specification

@Mixin(ANSI)
class ANSISpec extends Specification {
    @Shared
    def text = "This is the PARTY"
    
    def setup() {
        colorize = true
    }

    def "colors text red"() {
        expect:
        red(text) == "\\e[31m${text}\\e[0m"
    }

    def "formats text bold"() {
        expect:
        white(text) == "\\e[39;1m${text}\\e[0m"
    }

    def "colors text green"() {
        expect:
        green(text) == "\\e[32;1m${text}\\e[0m"
    }

    def "colors text cyan"() {
        expect:
        cyan(text) == "\\e[36m${text}\\e[0m"
    }

    def "can mix random known colors"() {
        expect:
        ansiParse(text, ANSI.Color.yellow, ANSI.Effect.underline) == "\\e[33;4m${text}\\e[0m"
    }
   
}
