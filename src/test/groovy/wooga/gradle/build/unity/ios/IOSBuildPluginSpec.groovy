package wooga.gradle.build.unity.ios

import nebula.test.ProjectSpec
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.build.unity.ios.internal.DefaultIOSBuildPluginExtension
import wooga.gradle.build.unity.ios.tasks.*
import wooga.gradle.fastlane.tasks.PilotUpload
import wooga.gradle.fastlane.tasks.SighRenew
import wooga.gradle.xcodebuild.tasks.ArchiveDebugSymbols
import wooga.gradle.xcodebuild.tasks.ExportArchive
import wooga.gradle.xcodebuild.tasks.XcodeArchive

@Requires({ os.macOs })
class IOSBuildPluginSpec extends ProjectSpec {
    public static final String PLUGIN_NAME = 'net.wooga.build-unity-ios'

    def 'Creates the [iosBuild] extension'() {
        given:
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        assert !project.extensions.findByName(IOSBuildPlugin.EXTENSION_NAME)

        when:
        project.plugins.apply(PLUGIN_NAME)

        then:
        def extension = project.extensions.findByName(IOSBuildPlugin.EXTENSION_NAME)
        extension instanceof DefaultIOSBuildPluginExtension
    }

    @Unroll("creates the task #taskName")
    def 'Creates needed tasks'(String taskName, Class taskType) {
        given:
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        assert !project.tasks.findByName(taskName)

        when:
        project.plugins.apply(PLUGIN_NAME)
        def task
        project.afterEvaluate {
            task = project.tasks.findByName(taskName)
        }

        then:
        project.evaluate()
        taskType.isInstance(task)

        where:
        taskName   | taskType
        "publish"  | DefaultTask
        "assemble" | DefaultTask
        "build"    | DefaultTask
        "check"    | DefaultTask
    }

    /*
    xcProject = new File(projectDir, "test.xcodeproj")
        xcProject.mkdirs()
        xcProjectConfig = new File(xcProject, "project.pbxproj")
        xcProjectConfig << ""
     */

    @Shared
    File xcProject

    @Shared
    File xcProjectConfig

    @Unroll()
    def 'Creates xcode task #taskName when project contains single xcode project'(String taskName, Class taskType) {
        given:
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        assert !project.tasks.findByName(taskName)

        and: "a dummpy xcode project"
        xcProject = new File(projectDir, "test.xcodeproj")
        xcProject.mkdirs()
        xcProjectConfig = new File(xcProject, "project.pbxproj")
        xcProjectConfig << ""

        when:
        project.plugins.apply(PLUGIN_NAME)
        def task
        project.afterEvaluate {
            task = project.tasks.findByName(taskName)
        }

        then:
        project.evaluate()
        taskType.isInstance(task)

        where:
        taskName                     | taskType
        "buildKeychain"              | KeychainTask
        "unlockKeychain"             | LockKeychainTask
        "lockKeychain"               | LockKeychainTask
        "resetKeychains"             | ListKeychainTask
        "addKeychain"                | ListKeychainTask
        "removeKeychain"             | ListKeychainTask
        "importProvisioningProfiles" | SighRenew
        "xcodeArchive"               | XcodeArchive
        "xcodeArchiveExport"         | ExportArchive
        "xcodeArchiveDSYMs"          | ArchiveDebugSymbols
        "publishTestFlight"          | PilotUpload
    }

    @Unroll()
    def 'Creates xcode tasks #taskNames when project contains multiple xcode projects'() {
        given: "a dummpy xcode project"
        xcodeProjectNames.each {
            xcProject = new File(projectDir, "${it}.xcodeproj")
            xcProject.mkdirs()
            xcProjectConfig = new File(xcProject, "project.pbxproj")
            xcProjectConfig << ""
        }

        when:
        project.plugins.apply(PLUGIN_NAME)
        List<Task> tasks
        project.afterEvaluate {
            tasks = taskNames.collect { project.tasks.findByName(it) }
        }

        then:
        project.evaluate()
        tasks.every { taskType.isInstance(it) }

        where:
        taskName                     | taskType
        "buildKeychain"              | KeychainTask
        "unlockKeychain"             | LockKeychainTask
        "lockKeychain"               | LockKeychainTask
        "resetKeychains"             | ListKeychainTask
        "addKeychain"                | ListKeychainTask
        "removeKeychain"             | ListKeychainTask
        "importProvisioningProfiles" | SighRenew
        "xcodeArchive"               | XcodeArchive
        "xcodeArchiveExport"         | ExportArchive
        "xcodeArchiveDSYMs"          | ArchiveDebugSymbols
        "publishTestFlight"          | PilotUpload

        xcodeProjectNames = ["first", "second", "third"]
        taskNames = ["first", "second", "third"].collect { it + taskName.capitalize() }
    }

    @Unroll()
    def "task #taskName #message on task #dependedTask when publishToTestflight is #publishToTestflight"() {
        given: "a dummpy xcode project"
        xcProject = new File(projectDir, "test.xcodeproj")
        xcProject.mkdirs()
        xcProjectConfig = new File(xcProject, "project.pbxproj")
        xcProjectConfig << ""

        and: "a project with property set"
        project.plugins.apply(PLUGIN_NAME)
        IOSBuildPluginExtension extension = project.extensions.findByName(IOSBuildPlugin.EXTENSION_NAME) as IOSBuildPluginExtension
        extension.setPublishToTestFlight(publishToTestflight)

        and: "a dummpy xcode project"
        xcProject = new File(projectDir, "test.xcodeproj")
        xcProject.mkdirs()
        xcProjectConfig = new File(xcProject, "project.pbxproj")
        xcProjectConfig << ""

        expect:
        project.evaluate()
        def task1 = project.tasks.getByName(taskName)
        def task2 = project.tasks.getByName(dependedTask)
        task1.dependsOn.contains(task2) == dependsOnTask

        where:
        taskName  | dependedTask        | publishToTestflight | dependsOnTask
        "publish" | "publishTestFlight" | true                | true
        "publish" | "publishTestFlight" | false               | false
        message = (dependsOnTask) ? "depends" : "depends not"
    }


}
