package wooga.gradle.xcodebuild.tasks

import net.wooga.test.xcode.XcodeTestProject
import org.junit.ClassRule
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.xcodebuild.config.BuildSettings

@Requires({ os.macOs })
class ExportArchiveIntegrationSpec extends AbstractXcodeArchiveTaskIntegrationSpec {

    @Shared
    @ClassRule
    XcodeTestProject xcodeProject = new XcodeTestProject()

    Class taskType = ExportArchive

    String testTaskName = "customExportArchive"

    String workingXcodebuildTaskConfig = """
    task exportArchive(type: ${XcodeArchive.name}) {
        scheme = "${xcodeProject.schemeName}"
        baseName = "custom"
        version = "0.1.0"
        buildSettings {
            codeSignIdentity ""
            codeSigningRequired false
            codeSigningAllowed false
            ${System.getenv("TEST_TEAM_ID") ? "developmentTeam = '${System.getenv("TEST_TEAM_ID")}'" : ""}
        }
        
        buildArgument('-allowProvisioningUpdates')
        clean = true
        projectPath = new File("${xcodeProject.xcodeProject}")
    }

    task ${testTaskName}(type: ${taskType.name}) {
        dependsOn exportArchive
        baseName = "custom"
        version = "0.1.0"
        xcArchivePath = exportArchive.xcArchivePath
        exportOptionsPlist = file("exportOptions.plist")
    }
    """.stripIndent()

    @Override
    String getExpectedPrettyColoredUnicodeLogOutput() {
        // we can't test the success case without a valid team id.
        if (System.getenv("TEST_TEAM_ID")) {
            return "▸ \u001B[39;1mExport\u001B[0m Succeeded"
        }

        return """error: exportArchive: No signing certificate "iOS Development" found"""
    }

    @Override
    String getExpectedPrettyLogOutput() {
        // we can't test the success case without a valid team id.
        if (System.getenv("TEST_TEAM_ID")) {
            return "> Export Succeeded"
        }
        return """error: exportArchive: No signing certificate "iOS Development" found"""
    }

    @Override
    String getExpectedPrettyUnicodeLogOutput() {
        // we can't test the success case without a valid team id.
        if (System.getenv("TEST_TEAM_ID")) {
            return "▸ Export Succeeded"
        }
        return """error: exportArchive: No signing certificate "iOS Development" found"""
    }

    File exportOptions

    def setup() {
        exportOptions = createFile("exportOptions.plist")
        exportOptions << """
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
        <plist version="1.0">
        <dict>
            <key>teamID</key>
            <string>${System.getenv("TEST_TEAM_ID")}</string>
            <key>method</key>
            <string>development</string>
            
            <key>provisioningProfiles</key>
            <dict>
                <key>net.wooga.xcodebuildPluginTest</key>
                <string>xcodebuildPluginTest</string>
            </dict>
        </dict>
        </plist>
        """.stripIndent().trim()
    }

    @Unroll("property #property sets flag #expectedCommandlineFlag")
    def "constructs build arguments"() {
        given:
        buildFile << """
        task("${testTaskName}", type: ${taskType.name}) {
            exportOptionsPlist = file("/some/path/exportOptions1.plist")
            xcArchivePath = file("/some/path/test1.xcarchive")
        }
        """.stripIndent()

        and: "a task to read the build arguments"
        buildFile << """
            task("readValue") {
                doLast {
                    println("arguments: " + ${testTaskName}.buildArguments.get().join(" "))
                }
            }
        """.stripIndent()

        and: "a set property"
        buildFile << """
            ${testTaskName}.${method}($value)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        outputContains(result, expectedCommandlineFlag)

        where:
        property             | method                   | rawValue                          | type   | expectedCommandlineFlag
        "exportOptionsPlist" | "exportOptionsPlist.set" | "/some/path/exportOptions1.plist" | "File" | "-exportOptionsPlist /some/path/exportOptions1.plist"
        "xcArchivePath"      | "xcArchivePath.set"      | "/some/path/test1.xcarchive"      | "File" | "-archivePath /some/path/test1.xcarchive"

        value = wrapValueBasedOnType(rawValue, type)
    }

    @Unroll("can set property #property with #method and type #type")
    def "can set property ExportArchive"() {
        given: "a custom archive task"
        buildFile << """
            task("${testTaskName}", type: ${taskType.name})
        """.stripIndent()

        and: "a task to read back the value"
        buildFile << """
            task("readValue") {
                doLast {
                    println("property: " + ${testTaskName}.${property}.get())
                }
            }
        """.stripIndent()

        and: "a set property"
        buildFile << """
            ${testTaskName}.${method}($value)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        outputContains(result, "property: " + expectedValue.toString())

        where:
        property             | method                   | rawValue                          | type
        "exportOptionsPlist" | "exportOptionsPlist"     | "/some/path/exportOptions1.plist" | "File"
        "exportOptionsPlist" | "exportOptionsPlist"     | "/some/path/exportOptions2.plist" | "Provider<RegularFile>"
        "exportOptionsPlist" | "exportOptionsPlist.set" | "/some/path/exportOptions3.plist" | "File"
        "exportOptionsPlist" | "exportOptionsPlist.set" | "/some/path/exportOptions4.plist" | "Provider<RegularFile>"
        "exportOptionsPlist" | "setExportOptionsPlist"  | "/some/path/exportOptions5.plist" | "File"
        "exportOptionsPlist" | "setExportOptionsPlist"  | "/some/path/exportOptions8.plist" | "Provider<RegularFile>"

        "xcArchivePath"      | "xcArchivePath"          | "/some/path/test1.xcarchive"      | "File"
        "xcArchivePath"      | "xcArchivePath"          | "/some/path/test2.xcarchive"      | "Provider<Directory>"
        "xcArchivePath"      | "xcArchivePath.set"      | "/some/path/test3.xcarchive"      | "File"
        "xcArchivePath"      | "xcArchivePath.set"      | "/some/path/test4.xcarchive"      | "Provider<Directory>"
        "xcArchivePath"      | "setXcArchivePath"       | "/some/path/test5.xcarchive"      | "File"
        "xcArchivePath"      | "setXcArchivePath"       | "/some/path/test6.xcarchive"      | "Provider<Directory>"

        value = wrapValueBasedOnType(rawValue, type) { type ->
            switch (type) {
                case BuildSettings.class.simpleName:
                    return "new ${BuildSettings.class.name}()" + rawValue.replaceAll(/(\[|\])/, '').split(',').collect({
                        List<String> parts = it.split("=")
                        ".put('${parts[0].trim()}', '${parts[1].trim()}')"
                    }).join("")
                default:
                    return rawValue
            }
        }
        expectedValue = rawValue
    }

    @Requires({ env.TEST_TEAM_ID })
    def "can export ipa from xcarchive"() {
        given:
        buildFile << workingXcodebuildTaskConfig

        and: "a future ipa file"
        def archive = new File(projectDir, "build/archives/custom-0.1.0.ipa")
        assert !archive.exists()

        when:
        def result = runTasks(testTaskName)

        then:
        result.success
        archive.exists()
        archive.isFile()
    }
}
