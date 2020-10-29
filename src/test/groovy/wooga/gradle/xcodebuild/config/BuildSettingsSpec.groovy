package wooga.gradle.xcodebuild.config

import org.gradle.internal.impldep.org.apache.maven.lifecycle.MissingProjectException
import spock.lang.Specification
import spock.lang.Unroll

class BuildSettingsSpec extends Specification {

    BuildSettings settings = new BuildSettings()

    def "can set single code sign flag"() {
        given: "some code sign flags"
        settings.otherCodeSignFlags("--verbose")
        settings.otherCodeSignFlags("--version")

        expect:
        settings.toList() == ["OTHER_CODE_SIGN_FLAGS=--verbose --version"]
    }

    def "can set code sign flag with value"() {
        given: "some code sign flags"
        settings.otherCodeSignFlags("--keychain", "some/path")

        expect:
        settings.toList() == ["OTHER_CODE_SIGN_FLAGS=--keychain some/path"]
    }

    def "can set codeSignIdentity"() {
        given: "an identity"
        settings.codeSignIdentity("me")

        expect:
        settings.toList() == ["CODE_SIGN_IDENTITY=me"]
    }

    def "can set empty codeSignIdentity"() {
        given: "an identity"
        settings.codeSignIdentity("")

        expect:
        settings.toList() == ["CODE_SIGN_IDENTITY=''"]
    }

    def "can set developmentTeam"() {
        given: "a team"
        settings.developmentTeam("me")

        expect:
        settings.toList() == ["DEVELOPMENT_TEAM=me"]
    }

    def "can set empty developmentTeam"() {
        given: "a team"
        settings.developmentTeam("")

        expect:
        settings.toList() == ["DEVELOPMENT_TEAM=''"]
    }

    @Unroll
    def "can set codeSigningRequired to #expectedValue"() {
        given:
        settings.codeSigningRequired(required)

        expect:
        settings.toList() == ["CODE_SIGNING_REQUIRED=${expectedValue}".toString()]

        where:
        required | expectedValue
        true     | "YES"
        false    | "NO"
    }

    @Unroll
    def "can set codeSigningAllowed to #expectedValue"() {
        given:
        settings.codeSigningAllowed(required)

        expect:
        settings.toList() == ["CODE_SIGNING_ALLOWED=${expectedValue}"]

        where:
        required | expectedValue
        true     | "YES"
        false    | "NO"
    }

    @Unroll
    def "can put #type to settings"() {
        expect:
        settings.put(key, value).toList() == expectedValue

        where:
        key                   | type           | value                | expectedValue
        "SOME_STRING_SETTING" | "String"       | "value"              | ["SOME_STRING_SETTING=value"]
        "SOME_BOOL_SETTING"   | "true"         | true                 | ["SOME_BOOL_SETTING=YES"]
        "SOME_BOOL_SETTING"   | "false"        | false                | ["SOME_BOOL_SETTING=NO"]
        "SOME_LIST_SETTING"   | "List<String>" | ['value1', 'value2'] | ["SOME_LIST_SETTING=value1 value2"]
    }

    @Unroll("call method #method with value #value creates setting #expectedValue")
    def "converts unknown method names to settings keys"() {
        when:
        settings.invokeMethod(method, value)

        then:
        settings.toList() == expectedValue

        where:
        method                | value                | expectedValue
        "someStringSetting"   | "customValue"        | ["SOME_STRING_SETTING=customValue"]
        "SomeStringSetting"   | "customValue"        | ["SOME_STRING_SETTING=customValue"]
        "SOME_STRING_SETTING" | "customValue"        | ["SOME_STRING_SETTING=customValue"]

        "someBoolSetting"     | true                 | ["SOME_BOOL_SETTING=YES"]
        "someBoolSetting"     | false                | ["SOME_BOOL_SETTING=NO"]
        "SomeBoolSetting"     | true                 | ["SOME_BOOL_SETTING=YES"]
        "SomeBoolSetting"     | false                | ["SOME_BOOL_SETTING=NO"]
        "SOME_BOOL_SETTING"   | true                 | ["SOME_BOOL_SETTING=YES"]
        "SOME_BOOL_SETTING"   | false                | ["SOME_BOOL_SETTING=NO"]

        "someListSetting"     | ['value1', 'value2'] | ["SOME_LIST_SETTING=value1 value2"]
        "SomeListSetting"     | ['value1', 'value2'] | ["SOME_LIST_SETTING=value1 value2"]
        "SOME_LIST_SETTING"   | ['value1', 'value2'] | ["SOME_LIST_SETTING=value1 value2"]
    }

    @Unroll("throws MissingMethodException exception when #reason")
    def "throws MissingMethodException exception"() {
        when:
        settings.invokeMethod("some_value", value)

        then:
        def e =thrown(MissingMethodException)
        e.method == "some_value"

        where:
        value | reason
        [:]   | "value is not of type String, Boolean or List"
    }

    @Unroll("access value #value of setting #key via dynamic property #property")
    def "access values as properties"() {
        given: "add custom value"
        settings.put(key, value)

        expect:
        settings.getProperty(property) == value

        where:
        key                   | property              | value
        "SOME_STRING_SETTING" | "someStringSetting"   | "customValue"
        "SOME_STRING_SETTING" | "SomeStringSetting"   | "customValue"
        "SOME_STRING_SETTING" | "SOME_STRING_SETTING" | "customValue"

        "SOME_BOOL_SETTING"   | "someBoolSetting"     | true
        "SOME_BOOL_SETTING"   | "someBoolSetting"     | false
        "SOME_BOOL_SETTING"   | "SomeBoolSetting"     | true
        "SOME_BOOL_SETTING"   | "SomeBoolSetting"     | false
        "SOME_BOOL_SETTING"   | "SOME_BOOL_SETTING"   | true
        "SOME_BOOL_SETTING"   | "SOME_BOOL_SETTING"   | false

        "SOME_LIST_SETTING"   | "someListSetting"     | ['value1', 'value2']
        "SOME_LIST_SETTING"   | "SomeListSetting"     | ['value1', 'value2']
        "SOME_LIST_SETTING"   | "SOME_LIST_SETTING"   | ['value1', 'value2']
    }

    @Unroll("throws MissingPropertyException exception when #reason")
    def "throws MissingPropertyException exception 2"() {
        when:
        settings.getProperty("some_value")

        then:
        def e =thrown(MissingPropertyException)
        e.property == "some_value"

        where:
        value | reason
        [:]   | "value can't be found"
    }

    @Unroll("set value #value of setting #key via dynamic property #property")
    def "set values as properties"() {
        when:
        settings.setProperty(property, value)

        then:
        settings.getProperty(property) == value

        where:
        key                   | property              | value
        "SOME_STRING_SETTING" | "someStringSetting"   | "customValue"
        "SOME_STRING_SETTING" | "SomeStringSetting"   | "customValue"
        "SOME_STRING_SETTING" | "SOME_STRING_SETTING" | "customValue"

        "SOME_BOOL_SETTING"   | "someBoolSetting"     | true
        "SOME_BOOL_SETTING"   | "someBoolSetting"     | false
        "SOME_BOOL_SETTING"   | "SomeBoolSetting"     | true
        "SOME_BOOL_SETTING"   | "SomeBoolSetting"     | false
        "SOME_BOOL_SETTING"   | "SOME_BOOL_SETTING"   | true
        "SOME_BOOL_SETTING"   | "SOME_BOOL_SETTING"   | false

        "SOME_LIST_SETTING"   | "someListSetting"     | ['value1', 'value2']
        "SOME_LIST_SETTING"   | "SomeListSetting"     | ['value1', 'value2']
        "SOME_LIST_SETTING"   | "SOME_LIST_SETTING"   | ['value1', 'value2']
    }

    @Unroll("throws MissingPropertyException exception when #reason")
    def "throws MissingPropertyException exception"() {
        when:
        settings.setProperty("some_value", value)

        then:
        def e =thrown(MissingPropertyException)
        e.property == "some_value"

        where:
        value | reason
        [:]   | "value is not of type String, Boolean or List"
    }
}


