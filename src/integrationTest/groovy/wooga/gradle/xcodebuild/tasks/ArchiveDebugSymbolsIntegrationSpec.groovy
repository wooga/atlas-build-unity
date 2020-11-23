package wooga.gradle.xcodebuild.tasks

import net.wooga.test.xcode.XcodeTestProject
import org.junit.ClassRule
import spock.lang.Requires
import spock.lang.Shared
import wooga.gradle.xcodebuild.XcodeBuildIntegrationSpec

@Requires({ os.macOs })
class ArchiveDebugSymbolsIntegrationSpec extends XcodeBuildIntegrationSpec {

    @Shared
    @ClassRule
    XcodeTestProject xcodeProject = new XcodeTestProject()

    def "creates zip archive with dsym files from xcarchive file"() {
        given: "a XcodeArchive task"
        buildFile << """
        task xcodeArchive(type: ${XcodeArchive.name}) {
            scheme = "${xcodeProject.schemeName}"
            baseName = "custom"
            version = "0.1.0"
            buildSettings {
                codeSignIdentity ""
                codeSigningRequired false
                codeSigningAllowed false
            }
            projectPath = new File("${xcodeProject.xcodeProject}")
        } 
        """.stripIndent()

        and: "the generated ArchiveDsym task"
        buildFile << """
        xcodeArchiveArchiveDSYMs {
            baseName = "custom"
            version = "0.1.0"
        }
        """

        and: "a future dsym archive"
        def dsymArchive = new File(projectDir, "build/symbols/custom-0.1.0-dSYM.zip")
        assert !dsymArchive.exists()

        when:
        def result = runTasksSuccessfully("xcodeArchiveArchiveDSYMs")

        then:
        result.success
        result.wasExecuted("xcodeArchive")
        dsymArchive.exists()
    }
}
