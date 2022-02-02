package wooga.gradle.build.unity.ios.tasks

import com.wooga.gradle.test.PropertyQueryTaskWriter
import spock.lang.Requires
import spock.lang.Unroll

import static com.wooga.gradle.test.PropertyUtils.toProviderSet
import static com.wooga.gradle.test.PropertyUtils.toSetter

/**
 * The test examples in this class are not 100% integration/functional tests.
 *
 * We can't run the real pod install as this would bring to much overhead at the moment.
 * We only test the invocation of pod and its parameters.
 */
@Requires({ os.macOs })
class PodInstallTaskSpec extends CocoaPodSpec<PodInstallTask> {
    def setup() {
        projectBaseName = "test"
        xcodeProject = new File(projectDir, "${projectBaseName}.xcodeproj")
        xcodeProject.mkdirs()
        xcodeWorkspace = new File(projectDir, "${projectBaseName}.xcworkspace")

        appendToSubjectTask("""
        projectDirectory.set(file("${xcodeProject.parentFile}"))
        xcodeProjectFileName = "test.xcodeproj"
        xcodeWorkspaceFileName = "test.xcworkspace"
        """.stripIndent())
    }

    def "task skips when no Pod file exist in project"() {
        given: "a sample Pod file"
        def podFile = createFile("Podfile")

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.wasExecuted(subjectUnderTestName)

        when:
        podFile.delete()
        result = runTasksSuccessfully(subjectUnderTestName)

        then:
        outputContains(result, "Task :${subjectUnderTestName} NO-SOURCE")
    }

    def "task executes 'repo update' before 'install'"() {
        given: "a sample Pod file"
        def podFile = createFile("Podfile")

        when:
        def result = runTasksSuccessfully(taskName)

        then:
        outputContains(result, "pod repo update")
        outputContains(result, "pod install")

        where:
        taskName = "podInstall"
    }

    def "buildKeychain caches task outputs"() {
        given: "a sample Pod file"
        def podFile = createFile("Podfile")

        and: "a Podfile.lock"
        def lockFile = createFile("Podfile.lock")

        and: "a gradle run with buildKeychain"
        runTasksSuccessfully(taskName)

        when:
        def result = runTasksSuccessfully(taskName)

        then:
        result.wasUpToDate(taskName)

        when:
        podFile << "a change"
        result = runTasksSuccessfully(taskName)

        then:
        !result.wasUpToDate(taskName)
        outputContains(result, "Input property 'inputFiles' file ${podFile.path} has changed.")

        when:
        lockFile << "a change"
        result = runTasksSuccessfully(taskName)

        then:
        !result.wasUpToDate(taskName)
        outputContains(result, "Input property 'inputFiles' file ${lockFile.path} has changed.")

        where:
        taskName = subjectUnderTestName
    }

    @Unroll("can set property #property with #method and type #type")
    def "can set property"() {
        given: "a task to read back the value"
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        query.write(buildFile)

        and: "a set property"
        appendToSubjectTask("${method}($value)")

        when:
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, expectedValue)

        where:
        property                 | method                  | rawValue           | returnValue | type
        "projectDirectory"       | toProviderSet(property) | "/path/to/project" | _           | "File"
        "projectDirectory"       | toProviderSet(property) | "/path/to/project" | _           | "Provider<Directory>"
        "projectDirectory"       | toSetter(property)      | "/path/to/project" | _           | "File"
        "projectDirectory"       | toSetter(property)      | "/path/to/project" | _           | "Provider<Directory>"

        "xcodeProjectFileName"   | toProviderSet(property) | "test.xcodeproj"   | _           | "String"
        "xcodeProjectFileName"   | toProviderSet(property) | "test.xcodeproj"   | _           | "Provider<String>"
        "xcodeProjectFileName"   | toSetter(property)      | "test.xcodeproj"   | _           | "String"
        "xcodeProjectFileName"   | toSetter(property)      | "test.xcodeproj"   | _           | "Provider<String>"

        "xcodeWorkspaceFileName" | toProviderSet(property) | "test.xcworkspace" | _           | "String"
        "xcodeWorkspaceFileName" | toProviderSet(property) | "test.xcworkspace" | _           | "Provider<String>"
        "xcodeWorkspaceFileName" | toSetter(property)      | "test.xcworkspace" | _           | "String"
        "xcodeWorkspaceFileName" | toSetter(property)      | "test.xcworkspace" | _           | "Provider<String>"
        value = wrapValueBasedOnType(rawValue, type, wrapValueFallback)
        expectedValue = returnValue == _ ? rawValue : returnValue
    }
}
