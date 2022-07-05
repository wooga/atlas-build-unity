package wooga.gradle.macOS.security.tasks

import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter
import spock.lang.Unroll
import wooga.gradle.macOS.security.SecurityIntegrationSpec
import wooga.gradle.macOS.security.SecurityMultikeychainOperationSpec
import wooga.gradle.macOS.security.SecurityTaskIntegrationSpec

import java.lang.reflect.ParameterizedType

import static com.wooga.gradle.test.queries.TestValue.filePath
import static com.wooga.gradle.test.queries.TestValue.filePaths
import static com.wooga.gradle.test.writers.PropertySetInvocation.*

abstract class MultiKeychainOperationIntegrationSpec<T extends SecurityMultikeychainOperationSpec> extends SecurityTaskIntegrationSpec<T> {

    @Unroll("method #invocation #message")
    def "method alters keychains property"() {
        given: "a set property"
        appendToSubjectTask("keychains = ${wrapValueBasedOnType(baseValue, "List<File>")}")

        and:
        if (appends) {
            rawValue.expectPrepend(baseValue)
        }

        expect:
        runPropertyQuery(get, set).matches(rawValue)

        where:
        invocation               | rawValue                                     | type                       | appends
        customSetter("keychain") | filePath("/some/path/1")                     | "File"                     | true
        customSetter("keychain") | filePath("/some/path/2")                     | "Provider<File>"           | true
        method                   | filePaths(["/some/path/3", "/some/path/4"])  | "Iterable<File>"           | true
        method                   | filePaths(["/some/path/5", "/some/path/6"])  | "Provider<Iterable<File>>" | true
        setter                   | filePaths(["/some/path/7", "/some/path/8"])  | "Iterable<File>"           | false
        setter                   | filePaths(["/some/path/9", "/some/path/10"]) | "Provider<Iterable<File>>" | false

        property = "keychains"
        baseValue = osPath("/some/path/0")
        message = appends ? "appends to keychains collection" : "set keychains collection"
        set = new PropertySetterWriter(subjectUnderTestName, property)
                .set(rawValue, type)
                .toScript(invocation)
        get = new PropertyGetterTaskWriter(subjectUnderTestName + ".keychains.files", "")
    }
}
