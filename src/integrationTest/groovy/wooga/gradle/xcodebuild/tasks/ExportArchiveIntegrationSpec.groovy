/*
 * Copyright 2018-2020 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.xcodebuild.tasks


import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetInvocation
import com.wooga.gradle.test.writers.PropertySetterWriter
import net.wooga.test.xcode.XcodeTestProject
import org.junit.ClassRule
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.xcodebuild.XcodeBuildPlugin

class ExportArchiveIntegrationSpec extends AbstractXcodeArchiveTaskIntegrationSpec<ExportArchive> {

    @Shared
    @ClassRule
    XcodeTestProject xcodeProject = new XcodeTestProject()

    String archiveTaskName = "xcodeArchive"
    //When using the plugin and creating an XcodeArchive task then
    //a matching export archive task will be created
    String generatedExportTaskName = archiveTaskName + "Export"

    String workingXcodebuildTaskConfig = """
    task ${archiveTaskName}(type: ${XcodeArchive.name}) {
        scheme = "${xcodeProject.schemeName}"
        baseName = "custom"
        version = "0.1.0"
        buildSettings {
            codeSignIdentity ""
            codeSigningRequired false
            codeSigningAllowed false
            ${System.getenv("TEST_TEAM_ID") ? "developmentTeam = '${System.getenv("TEST_TEAM_ID")}'" : ""}
        }
        
        argument('-allowProvisioningUpdates')
        clean = true
        projectPath = ${wrapValueBasedOnType(xcodeProject.xcodeProject, File)}
    }

    ${generatedExportTaskName} {
        baseName = "custom"
        version = "0.1.0"
        exportOptionsPlist = file("exportOptions.plist")
    }

    task ${subjectUnderTestName}(type: ${subjectUnderTestTypeName}) {
        dependsOn(${archiveTaskName})
        xcArchivePath.convention(${archiveTaskName}.xcArchivePath)
        baseName = "custom"
        version = "0.1.0"
        exportOptionsPlist = file("exportOptions.plist")
    }
    """.stripIndent()

    @Override
    String getExpectedPrettyColoredUnicodeLogOutput() {
        // we can't test the success case without a valid team id.
        if (System.getenv("TEST_TEAM_ID")) {
            return "▸ \u001B[39;1mExport\u001B[0m Succeeded"
        }

        "\u001B[31m❌  error: exportArchive: No signing certificate \"iOS Development\" found\u001B[0m"
    }

    @Override
    String getExpectedPrettyLogOutput() {
        // we can't test the success case without a valid team id.
        if (System.getenv("TEST_TEAM_ID")) {
            return "> Export Succeeded"
        }
        "[x] error: exportArchive: No signing certificate \"iOS Development\""

    }

    @Override
    String getExpectedPrettyUnicodeLogOutput() {
        // we can't test the success case without a valid team id.
        if (System.getenv("TEST_TEAM_ID")) {
            return "▸ Export Succeeded"
        }
        return """❌  error: exportArchive: No signing certificate "iOS Development" found"""
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

    @Unroll("property #property sets flag #flag")
    def "constructs build arguments"() {

        when:
        // These 2 properties need to be always set since they are not optional
        addMockTask(true, """
            exportOptionsPlist = file("/foo/bar.plist")
            xcArchivePath = file("/foo/bar.xcarchive")
        """)
        def query = runPropertyQuery(getter, setter)

        then:
        query.contains(flag)
        query.contains(rawValue)

        where:
        property             | flag                  | method                       | rawValue                                  | type
        "exportOptionsPlist" | "-exportOptionsPlist" | PropertySetInvocation.setter | osPath("/some/path/exportOptions1.plist") | "File"
        "xcArchivePath"      | "-archivePath"        | PropertySetInvocation.setter | osPath("/some/path/test1.xcarchive")      | "File"

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(rawValue, type)
            .use(method)
            .serialize(wrapValueFallback)

        getter = new PropertyGetterTaskWriter("${subjectUnderTestName}.arguments")
    }

    @Unroll("can set property #property with #method and type #type")
    def "can set property ExportArchive"() {
        expect:
        addMockTask(true)
        runPropertyQuery(getter, setter).matches(rawValue)

        where:
        property             | method                            | rawValue                                  | type
        "exportOptionsPlist" | PropertySetInvocation.method      | osPath("/some/path/exportOptions1.plist") | "File"
        "exportOptionsPlist" | PropertySetInvocation.method      | osPath("/some/path/exportOptions2.plist") | "Provider<RegularFile>"
        "exportOptionsPlist" | PropertySetInvocation.providerSet | osPath("/some/path/exportOptions3.plist") | "File"
        "exportOptionsPlist" | PropertySetInvocation.providerSet | osPath("/some/path/exportOptions4.plist") | "Provider<RegularFile>"
        "exportOptionsPlist" | PropertySetInvocation.setter      | osPath("/some/path/exportOptions5.plist") | "File"
        "exportOptionsPlist" | PropertySetInvocation.setter      | osPath("/some/path/exportOptions8.plist") | "Provider<RegularFile>"

        "xcArchivePath"      | PropertySetInvocation.method      | osPath("/some/path/test1.xcarchive")      | "File"
        "xcArchivePath"      | PropertySetInvocation.method      | osPath("/some/path/test2.xcarchive")      | "Provider<Directory>"
        "xcArchivePath"      | PropertySetInvocation.providerSet | osPath("/some/path/test3.xcarchive")      | "File"
        "xcArchivePath"      | PropertySetInvocation.providerSet | osPath("/some/path/test4.xcarchive")      | "Provider<Directory>"
        "xcArchivePath"      | PropertySetInvocation.setter      | osPath("/some/path/test5.xcarchive")      | "File"
        "xcArchivePath"      | PropertySetInvocation.setter      | osPath("/some/path/test6.xcarchive")      | "Provider<Directory>"

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(rawValue, type)
            .serialize(wrapValueFallback)
        getter = new PropertyGetterTaskWriter(setter)
    }

    //TODO: Move this test to the main PluginIntergrationSpec
    @Requires({ os.macOs })
    def "is registered as publish artifact"() {
        given: "a subproject with xcode build setup"
        def subProjectDir = addSubproject(subProjectName)
        def subProjectBuildFile = new File(subProjectDir, "build.gradle")
        subProjectBuildFile << """
            ${applyPlugin(XcodeBuildPlugin)}
            ${workingXcodebuildTaskConfig}

            version = '1.0.0'
        """.stripIndent()

        and: "the main project pulling a dependency"
        buildFile << """
            configurations.maybeCreate('test')
    
            dependencies {
                test project(':${subProjectName}')
            }
            
            task run (type: Copy) {
                from(configurations.test)
                into("${projectDir}/build/outputs")
            }
        """.stripIndent()

        and: "the export options plist file in the correct directory"
        def exportPlist = createFile("exportOptions.plist", subProjectDir)
        exportPlist.text = exportOptions.text

        when:
        def result = runTasks(":${subProjectName}:${generatedExportTaskName}")

        then:
        result.wasExecuted(":${subProjectName}:${generatedExportTaskName}")

        where:
        subProjectName = "xcodeProject"
    }

    @Requires({ env.TEST_TEAM_ID })
    def "can export ipa from xcarchive"() {
        given:
        buildFile << workingXcodebuildTaskConfig

        and: "a future ipa file"
        def archive = new File(projectDir, "build/archives/custom-0.1.0.ipa")
        assert !archive.exists()

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        result.success
        result.wasExecuted(archiveTaskName)
        archive.exists()
        archive.isFile()
    }
}
