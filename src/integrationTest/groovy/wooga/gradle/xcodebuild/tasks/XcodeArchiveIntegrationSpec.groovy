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
 *
 *
 *
 */

package wooga.gradle.xcodebuild.tasks

import net.wooga.test.xcode.XcodeTestProject
import org.junit.ClassRule
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.xcodebuild.config.BuildSettings

@Requires({ os.macOs })
class XcodeArchiveIntegrationSpec extends AbstractXcodeTaskIntegrationSpec {

    @Shared
    @ClassRule
    XcodeTestProject xcodeProject = new XcodeTestProject()

    Class taskType = XcodeArchive

    String testTaskName = "customExport"

    String workingXcodebuildTaskConfig = """
    task ${testTaskName}(type: ${taskType.name}) {
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


    String expectedPrettyColoredUnicodeLogOutput = """
        ▸ \u001B[39;1mLinking\u001B[0m xcodebuildPluginTest
        ▸ \u001B[39;1mProcessing\u001B[0m Info.plist
        ▸ \u001B[39;1mGenerating 'xcodebuildPluginTest.app.dSYM'\u001B[0m
        ▸ \u001B[39;1mTouching\u001B[0m xcodebuildPluginTest.app (in target 'xcodebuildPluginTest' from project 'xcodebuildPluginTest')
        ▸ \u001B[39;1mArchive\u001B[0m Succeeded
    """.stripIndent().trim()

    String expectedPrettyUnicodeLogOutput = """
        ▸ Linking xcodebuildPluginTest
        ▸ Processing Info.plist
        ▸ Generating 'xcodebuildPluginTest.app.dSYM'
        ▸ Touching xcodebuildPluginTest.app (in target 'xcodebuildPluginTest' from project 'xcodebuildPluginTest')
        ▸ Archive Succeeded
        """.stripIndent().trim()

    String expectedPrettyLogOutput = """
        > Linking xcodebuildPluginTest
        > Processing Info.plist
        > Generating 'xcodebuildPluginTest.app.dSYM'
        > Touching xcodebuildPluginTest.app (in target 'xcodebuildPluginTest' from project 'xcodebuildPluginTest')
        > Archive Succeeded
        """.stripIndent().trim()

    @Unroll
    def "creates archive from #type"() {
        given:
        buildFile << workingXcodebuildTaskConfig
        buildFile << """
        ${testTaskName}.projectPath = new File("${path}")
        """.stripIndent()

        and: "a future xcarchive"
        def archive = new File(projectDir, "build/archives/custom-0.1.0.xcarchive")
        assert !archive.exists()

        when:
        def result = runTasks(testTaskName)

        then:
        result.success
        archive.exists()
        archive.isDirectory()

        where:
        type          | path
        "xcodeprj"    | xcodeProject.xcodeProject.path
        "xcworkspace" | xcodeProject.xcodeWorkspace.path
    }

    def "fails when project is not a valid .xcodeprj or .xcworkspace"() {
        given:
        buildFile << workingXcodebuildTaskConfig
        buildFile << """
        ${testTaskName}.projectPath = new File("${File.createTempDir("someProject", ".project")}")
        """.stripIndent()

        when:
        def result = runTasksWithFailure(testTaskName)

        then:
        outputContains(result, "xcode project path must be a valid .xcodeproj or .xcworkspace")
    }

    @Unroll("property #property sets flag #expectedCommandlineFlag")
    def "constructs build arguments"() {
        given:
        buildFile << """
        task("customXcodeArchive", type: ${taskType.name}) {
            scheme = "${xcodeProject.schemeName}"
            projectPath = new File("${xcodeProject.xcodeProject.path}")
        }
        """.stripIndent()

        and: "a task to read the build arguments"
        buildFile << """
            task("readValue") {
                doLast {
                    println("arguments: " + customXcodeArchive.buildArguments.get().join(" "))
                }
            }
        """.stripIndent()

        and: "a set property"
        buildFile << """
            customXcodeArchive.${method}($value)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        outputContains(result, expectedCommandlineFlag)

        where:
        property          | method                | rawValue                    | type      | expectedCommandlineFlag
        "configuration"   | "configuration.set"   | "test"                      | "String"  | "-configuration test"
        "clean"           | "clean.set"           | true                        | "Boolean" | "clean"
        "scheme"          | "scheme.set"          | "test"                      | "String"  | "-scheme test"
        "teamId"          | "teamId.set"          | "x123y"                     | "String"  | "DEVELOPMENT_TEAM=x123y"
        "buildKeychain"   | "buildKeychain.set"   | "/some/path"                | "File"    | "OTHER_CODE_SIGN_FLAGS=--keychain /some/path"
        "derivedDataPath" | "derivedDataPath.set" | "/some/path"                | "File"    | "-derivedDataPath /some/path"
        "projectPath"     | "projectPath.set"     | "/some/project.xcodeproj"   | "File"    | "-project /some/project.xcodeproj"
        "projectPath"     | "projectPath.set"     | "/some/project.xcworkspace" | "File"    | "-workspace /some/project.xcworkspace"

        value = wrapValueBasedOnType(rawValue, type)
    }

    @Unroll("can set property #property with #method and type #type")
    def "can set property XcodeArchive"() {
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
        property          | method                | rawValue     | type
        "configuration"   | "configuration"       | "Test1"      | "String"
        "configuration"   | "configuration"       | "Test2"      | "Provider<String>"
        "configuration"   | "configuration.set"   | "Test1"      | "String"
        "configuration"   | "configuration.set"   | "Test2"      | "Provider<String>"
        "configuration"   | "setConfiguration"    | "Test3"      | "String"
        "configuration"   | "setConfiguration"    | "Test4"      | "Provider<String>"

        "clean"           | "clean"               | true         | "Boolean"
        "clean"           | "clean"               | true         | "Provider<Boolean>"
        "clean"           | "clean.set"           | true         | "Boolean"
        "clean"           | "clean.set"           | true         | "Provider<Boolean>"
        "clean"           | "setClean"            | true         | "Boolean"
        "clean"           | "setClean"            | true         | "Provider<Boolean>"

        "scheme"          | "scheme"              | "Test1"      | "String"
        "scheme"          | "scheme"              | "Test2"      | "Provider<String>"
        "scheme"          | "scheme.set"          | "Test1"      | "String"
        "scheme"          | "scheme.set"          | "Test2"      | "Provider<String>"
        "scheme"          | "setScheme"           | "Test3"      | "String"
        "scheme"          | "setScheme"           | "Test4"      | "Provider<String>"

        "teamId"          | "teamId"              | "Test1"      | "String"
        "teamId"          | "teamId"              | "Test2"      | "Provider<String>"
        "teamId"          | "teamId.set"          | "Test1"      | "String"
        "teamId"          | "teamId.set"          | "Test2"      | "Provider<String>"
        "teamId"          | "setTeamId"           | "Test3"      | "String"
        "teamId"          | "setTeamId"           | "Test4"      | "Provider<String>"

        "derivedDataPath" | "derivedDataPath"     | "/some/path" | "File"
        "derivedDataPath" | "derivedDataPath"     | "/some/path" | "Provider<Directory>"
        "derivedDataPath" | "derivedDataPath.set" | "/some/path" | "File"
        "derivedDataPath" | "derivedDataPath.set" | "/some/path" | "Provider<Directory>"
        "derivedDataPath" | "setDerivedDataPath"  | "/some/path" | "File"
        "derivedDataPath" | "setDerivedDataPath"  | "/some/path" | "Provider<Directory>"

        "buildKeychain"   | "buildKeychain"       | "/some/path" | "File"
        "buildKeychain"   | "buildKeychain"       | "/some/path" | "Provider<RegularFile>"
        "buildKeychain"   | "buildKeychain.set"   | "/some/path" | "File"
        "buildKeychain"   | "buildKeychain.set"   | "/some/path" | "Provider<RegularFile>"
        "buildKeychain"   | "setBuildKeychain"    | "/some/path" | "File"
        "buildKeychain"   | "setBuildKeychain"    | "/some/path" | "Provider<RegularFile>"

        "projectPath"     | "projectPath"         | "/some/path" | "File"
        "projectPath"     | "projectPath"         | "/some/path" | "Provider<Directory>"
        "projectPath"     | "projectPath.set"     | "/some/path" | "File"
        "projectPath"     | "projectPath.set"     | "/some/path" | "Provider<Directory>"
        "projectPath"     | "setProjectPath"      | "/some/path" | "File"
        "projectPath"     | "setProjectPath"      | "/some/path" | "Provider<Directory>"

        "archiveName"     | "archiveName"         | "Test1"      | "String"
        "archiveName"     | "archiveName"         | "Test2"      | "Provider<String>"
        "archiveName"     | "archiveName.set"     | "Test1"      | "String"
        "archiveName"     | "archiveName.set"     | "Test2"      | "Provider<String>"
        "archiveName"     | "setArchiveName"      | "Test3"      | "String"
        "archiveName"     | "setArchiveName"      | "Test4"      | "Provider<String>"

        "baseName"        | "baseName"            | "Test1"      | "String"
        "baseName"        | "baseName"            | "Test2"      | "Provider<String>"
        "baseName"        | "baseName.set"        | "Test1"      | "String"
        "baseName"        | "baseName.set"        | "Test2"      | "Provider<String>"
        "baseName"        | "setBaseName"         | "Test3"      | "String"
        "baseName"        | "setBaseName"         | "Test4"      | "Provider<String>"

        "appendix"        | "appendix"            | "Test1"      | "String"
        "appendix"        | "appendix"            | "Test2"      | "Provider<String>"
        "appendix"        | "appendix.set"        | "Test1"      | "String"
        "appendix"        | "appendix.set"        | "Test2"      | "Provider<String>"
        "appendix"        | "setAppendix"         | "Test3"      | "String"
        "appendix"        | "setAppendix"         | "Test4"      | "Provider<String>"

        "version"         | "version"             | "Test1"      | "String"
        "version"         | "version"             | "Test2"      | "Provider<String>"
        "version"         | "version.set"         | "Test1"      | "String"
        "version"         | "version.set"         | "Test2"      | "Provider<String>"
        "version"         | "setVersion"          | "Test3"      | "String"
        "version"         | "setVersion"          | "Test4"      | "Provider<String>"

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

    @Unroll
    def "can configure buildArguments with #method #message"() {
        given: "a custom archive task"
        buildFile << """
            task("customXcodeArchive", type: ${XcodeArchive.name}) {
                buildArguments(["--test", "value"])
            }
        """.stripIndent()

        and: "a task to read back the value"
        buildFile << """
            task("readValue") {
                doLast {
                    println("property: " + customXcodeArchive.${property}.get())
                }
            }
        """.stripIndent()

        and: "a set property"
        buildFile << """
            customXcodeArchive.${method}($value)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        outputContains(result, "property: " + expectedValue.toString())

        where:
        method                         | rawValue         | type                      | append | expectedValue
        "buildArgument"                | "--foo"          | "String"                  | true   | ["--test", "value", "--foo"]
        "buildArguments"               | ["--foo", "bar"] | "List<String>"            | true   | ["--test", "value", "--foo", "bar"]
        "buildArguments"               | ["--foo", "bar"] | "String[]"                | true   | ["--test", "value", "--foo", "bar"]
        "setAdditionalBuildArguments"  | ["--foo", "bar"] | "List<String>"            | false  | ["--foo", "bar"]
        "setAdditionalBuildArguments"  | ["--foo", "bar"] | "Provider<List<String>>"  | false  | ["--foo", "bar"]
        "additionalBuildArguments.set" | ["--foo", "bar"] | "List<String>"            | false  | ["--foo", "bar"]
        "additionalBuildArguments.set" | ["--foo", "bar"] | "Provider<List<String>>>" | false  | ["--foo", "bar"]

        property = "additionalBuildArguments"
        value = wrapValueBasedOnType(rawValue, type)
        message = (append) ? "which appends arguments" : "which replaces arguments"
    }

    def "creates property #property from destination and archive name"() {
        given: "a custom archive task"
        buildFile << """
            task("${testTaskName}", type: ${XcodeArchive.name}) {
                archiveName("test-0.0.0.xcarchive")
                destinationDir(file("/some/path"))
            }
        """.stripIndent()

        and: "a task to read back the value"
        buildFile << """
            task("readValue") {
                doLast {
                    println("xcArchivePath: '" + ${testTaskName}.${property}.get() + "'")
                }
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        outputContains(result, "xcArchivePath: '" + expectedXcArchivePath + "'")

        where:
        property        | archiveName            | destinationDir | expectedXcArchivePath
        "xcArchivePath" | "test-0.0.0.xcarchive" | "/some/path"   | "/some/path/test-0.0.0.xcarchive"
    }
}
