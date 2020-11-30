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

@Requires({ os.macOs })
class ArchiveDebugSymbolsIntegrationSpec extends XcodeBuildIntegrationSpec {

    @Shared
    @ClassRule
    XcodeTestProject xcodeProject = new XcodeTestProject()

    String archiveTaskName = "xcodeArchive"
    String testTaskName = archiveTaskName + "DSYMs"

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

        buildArgument('-allowProvisioningUpdates')
        clean = true
        projectPath = new File("${xcodeProject.xcodeProject}")
    }

    ${testTaskName} {
        baseName = "custom"
        version = "0.1.0"
    }
    """.stripIndent()

    def "creates zip archive with dsym files from xcarchive file"() {
        given: "a XcodeArchive task"
        buildFile << workingXcodebuildTaskConfig

        and: "a future dsym archive"
        def dsymArchive = new File(projectDir, "build/symbols/custom-0.1.0-dSYM.zip")
        assert !dsymArchive.exists()

        when:
        def result = runTasksSuccessfully("xcodeArchiveDSYMs")

        then:
        result.success
        result.wasExecuted("xcodeArchive")
        dsymArchive.exists()
    }

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
            configurations.maybeCreate('archives')

            dependencies {
                archives project(':${subProjectName}')
            }

            task run (type: Copy) {
                from(configurations.archives)
                into("${projectDir}/build/outputs")
            }
        """.stripIndent()

        when:
        def result = runTasks("run")


        then:
        result.wasExecuted(":${subProjectName}:${testTaskName}")

        where:
        subProjectName = "xcodeProject"
    }
}
