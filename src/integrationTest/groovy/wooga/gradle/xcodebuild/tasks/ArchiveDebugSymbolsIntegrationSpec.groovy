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

import net.wooga.test.xcode.XcodeTestProject
import org.junit.ClassRule
import spock.lang.Requires
import spock.lang.Shared
import wooga.gradle.xcodebuild.XcodeBuildIntegrationSpec
import wooga.gradle.xcodebuild.XcodeBuildPlugin

//@Requires({ os.macOs })
class ArchiveDebugSymbolsIntegrationSpec extends XcodeBuildIntegrationSpec {

    @Shared
    @ClassRule
    XcodeTestProject xcodeProject = new XcodeTestProject()

    String archiveTaskName = "xcodeArchive"
    String testTaskName = archiveTaskName + "DSYMs"
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
        projectPath = new File("${xcodeProject.xcodeProject}")
    }

    ${testTaskName} {
        archiveBaseName = "custom"
        archiveVersion = "0.1.0"
    }
    
    xcodeArchiveExport {
        baseName = "custom"
        version = "0.1.0"
        exportOptionsPlist = file("exportOptions.plist")
    }
    """.stripIndent()

    @Requires({ os.macOs })
    def "creates zip archive with dsym files from xcarchive file"() {
        given: "a XcodeArchive task"
        buildFile << workingXcodebuildTaskConfig

        and: "a future dsym archive"
        def dsymArchive = new File(projectDir, "build/symbols/custom-0.1.0-dSYM.zip")
        assert !dsymArchive.exists()

        when:
        def result = runTasksSuccessfully(testTaskName)

        then:
        result.success
        result.wasExecuted("xcodeArchive")
        result.wasExecuted(testTaskName)
        dsymArchive.exists()
    }

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
        def result = runTasks("run")

        then:
        result.wasExecuted(":${subProjectName}:${testTaskName}")

        where:
        subProjectName = "xcodeProject"
    }
}
