package wooga.gradle.build.unity.ios.tasks

import spock.lang.Requires

/**
 * The test examples in this class are not 100% integration/functional tests.
 *
 * We can't run the real pod install as this would bring to much overhead at the moment.
 * We only test the invocation of pod and its parameters.
 */
@Requires({ os.macOs })
class PodInstallTaskSpec extends CocoaPodSpec {
    def setup() {
        def xcodeProject = new File("test.xcodeproj")
        xcodeProject.mkdirs()

        buildFile << """
            task podInstall(type: wooga.gradle.build.unity.ios.tasks.PodInstallTask) {
                projectPath = "${xcodeProject.path}"
            }
        """.stripIndent()
    }

    def "task skips when no Pod file exist in project"() {
        given: "a sample Pod file"
        def podFile = createFile("Podfile")

        when:
        def result = runTasksSuccessfully(taskName)

        then:
        result.wasExecuted(taskName)

        when:
        podFile.delete()
        result = runTasksSuccessfully(taskName)

        then:
        outputContains(result, "Task :${taskName} NO-SOURCE")

        where:
        taskName = "podInstall"
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
        outputContains(result,"Input property 'inputFiles' file ${podFile.path} has changed.")

        when:
        lockFile << "a change"
        result = runTasksSuccessfully(taskName)

        then:
        !result.wasUpToDate(taskName)
        outputContains(result,"Input property 'inputFiles' file ${lockFile.path} has changed.")

        where:
        taskName = "podInstall"
    }
}
