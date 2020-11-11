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
import wooga.gradle.xcodebuild.XcodeBuildIntegrationSpec
import wooga.gradle.xcodebuild.config.BuildSettings

@Requires({ os.macOs })
class XcodeArchiveIntegrationSpec extends XcodeBuildIntegrationSpec {

    @Shared
    @ClassRule
    XcodeTestProject xcodeProject = new XcodeTestProject()

    @Unroll
    def "creates archive from #type"() {
        given:
        buildFile << """
        task customExport(type: ${XcodeArchive.name}) {
            scheme = "${xcodeProject.schemeName}"
            baseName = "custom"
            version = "0.1.0"
            buildSettings {
                codeSignIdentity ""
                codeSigningRequired false
                codeSigningAllowed false
            }

            projectPath = new File("${path}")
        }
        """.stripIndent()

        and: "a future xcarchive"
        def archive = new File(projectDir, "build/archives/custom-0.1.0.xcarchive")
        assert !archive.exists()

        when:
        def result = runTasks("customExport")

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
        buildFile << """
        task customExport(type: ${XcodeArchive.name}) {
            scheme = "${xcodeProject.schemeName}"
            baseName = "custom"
            version = "0.1.0"
            buildSettings {
                codeSignIdentity ""
                codeSigningRequired false
                codeSigningAllowed false
            }

            projectPath = new File("${File.createTempDir("someProject",".project")}")
        }
        """.stripIndent()

        when:
        def result = runTasksWithFailure("customExport")

        then:
        outputContains(result, "xcode project path must be a valid .xcodeproj or .xcworkspace")
    }

    def "can provide additional build arguments"() {
        given:
        buildFile << """
        task customExport(type: ${XcodeArchive.name}) {
            scheme = "${xcodeProject.schemeName}"
            baseName = "custom"
            version = "0.1.0"
            buildSettings {
                codeSignIdentity ""
                codeSigningRequired false
                codeSigningAllowed false
            }

            projectPath = new File("${xcodeProject.xcodeProject.path}")
        }
        """.stripIndent()

        and: "some custom arguments"
        buildFile << """
        customExport.buildArgument("-quiet")
        customExport.buildArguments("-enableAddressSanitizer", "YES")
        customExport.buildArguments("-enableThreadSanitizer", "NO")
        """.stripIndent()

        when:
        def result = runTasks("customExport")

        then:
        result.success
        outputContains(result, "-quiet")
        outputContains(result, "-enableAddressSanitizer YES")
        outputContains(result, "-enableThreadSanitizer NO")
    }

    @Unroll("property #property sets flag #expectedCommandlineFlag")
    def "constructs build arguments"() {
        given:
        buildFile << """
        task("customXcodeArchive", type: ${XcodeArchive.name}) {
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

    @Unroll
    def "can set property #property with #method and type #type"() {
        given: "a custom archive task"
        buildFile << """
            task("customXcodeArchive", type: ${XcodeArchive.name})
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
        property          | method                | rawValue                                                    | type
        "configuration"   | "configuration"       | "Test1"                                                     | "String"
        "configuration"   | "configuration"       | "Test2"                                                     | "Provider<String>"
        "configuration"   | "configuration.set"   | "Test1"                                                     | "String"
        "configuration"   | "configuration.set"   | "Test2"                                                     | "Provider<String>"
        "configuration"   | "setConfiguration"    | "Test3"                                                     | "String"
        "configuration"   | "setConfiguration"    | "Test4"                                                     | "Provider<String>"

        "clean"           | "clean"               | true                                                        | "Boolean"
        "clean"           | "clean"               | true                                                        | "Provider<Boolean>"
        "clean"           | "clean.set"           | true                                                        | "Boolean"
        "clean"           | "clean.set"           | true                                                        | "Provider<Boolean>"
        "clean"           | "setClean"            | true                                                        | "Boolean"
        "clean"           | "setClean"            | true                                                        | "Provider<Boolean>"

        "scheme"          | "scheme"              | "Test1"                                                     | "String"
        "scheme"          | "scheme"              | "Test2"                                                     | "Provider<String>"
        "scheme"          | "scheme.set"          | "Test1"                                                     | "String"
        "scheme"          | "scheme.set"          | "Test2"                                                     | "Provider<String>"
        "scheme"          | "setScheme"           | "Test3"                                                     | "String"
        "scheme"          | "setScheme"           | "Test4"                                                     | "Provider<String>"

        "teamId"          | "teamId"              | "Test1"                                                     | "String"
        "teamId"          | "teamId"              | "Test2"                                                     | "Provider<String>"
        "teamId"          | "teamId.set"          | "Test1"                                                     | "String"
        "teamId"          | "teamId.set"          | "Test2"                                                     | "Provider<String>"
        "teamId"          | "setTeamId"           | "Test3"                                                     | "String"
        "teamId"          | "setTeamId"           | "Test4"                                                     | "Provider<String>"

        "derivedDataPath" | "derivedDataPath"     | "/some/path"                                                | "File"
        "derivedDataPath" | "derivedDataPath"     | "/some/path"                                                | "Provider<Directory>"
        "derivedDataPath" | "derivedDataPath.set" | "/some/path"                                                | "File"
        "derivedDataPath" | "derivedDataPath.set" | "/some/path"                                                | "Provider<Directory>"
        "derivedDataPath" | "setDerivedDataPath"  | "/some/path"                                                | "File"
        "derivedDataPath" | "setDerivedDataPath"  | "/some/path"                                                | "Provider<Directory>"

        "buildKeychain"   | "buildKeychain"       | "/some/path"                                                | "File"
        "buildKeychain"   | "buildKeychain"       | "/some/path"                                                | "Provider<RegularFile>"
        "buildKeychain"   | "buildKeychain.set"   | "/some/path"                                                | "File"
        "buildKeychain"   | "buildKeychain.set"   | "/some/path"                                                | "Provider<RegularFile>"
        "buildKeychain"   | "setBuildKeychain"    | "/some/path"                                                | "File"
        "buildKeychain"   | "setBuildKeychain"    | "/some/path"                                                | "Provider<RegularFile>"

        "projectPath"     | "projectPath"         | "/some/path"                                                | "File"
        "projectPath"     | "projectPath"         | "/some/path"                                                | "Provider<Directory>"
        "projectPath"     | "projectPath.set"     | "/some/path"                                                | "File"
        "projectPath"     | "projectPath.set"     | "/some/path"                                                | "Provider<Directory>"
        "projectPath"     | "setProjectPath"      | "/some/path"                                                | "File"
        "projectPath"     | "setProjectPath"      | "/some/path"                                                | "Provider<Directory>"

        "archiveName"     | "archiveName"         | "Test1"                                                     | "String"
        "archiveName"     | "archiveName"         | "Test2"                                                     | "Provider<String>"
        "archiveName"     | "archiveName.set"     | "Test1"                                                     | "String"
        "archiveName"     | "archiveName.set"     | "Test2"                                                     | "Provider<String>"
        "archiveName"     | "setArchiveName"      | "Test3"                                                     | "String"
        "archiveName"     | "setArchiveName"      | "Test4"                                                     | "Provider<String>"

        "baseName"        | "baseName"            | "Test1"                                                     | "String"
        "baseName"        | "baseName"            | "Test2"                                                     | "Provider<String>"
        "baseName"        | "baseName.set"        | "Test1"                                                     | "String"
        "baseName"        | "baseName.set"        | "Test2"                                                     | "Provider<String>"
        "baseName"        | "setBaseName"         | "Test3"                                                     | "String"
        "baseName"        | "setBaseName"         | "Test4"                                                     | "Provider<String>"

        "appendix"        | "appendix"            | "Test1"                                                     | "String"
        "appendix"        | "appendix"            | "Test2"                                                     | "Provider<String>"
        "appendix"        | "appendix.set"        | "Test1"                                                     | "String"
        "appendix"        | "appendix.set"        | "Test2"                                                     | "Provider<String>"
        "appendix"        | "setAppendix"         | "Test3"                                                     | "String"
        "appendix"        | "setAppendix"         | "Test4"                                                     | "Provider<String>"

        "version"         | "version"             | "Test1"                                                     | "String"
        "version"         | "version"             | "Test2"                                                     | "Provider<String>"
        "version"         | "version.set"         | "Test1"                                                     | "String"
        "version"         | "version.set"         | "Test2"                                                     | "Provider<String>"
        "version"         | "setVersion"          | "Test3"                                                     | "String"
        "version"         | "setVersion"          | "Test4"                                                     | "Provider<String>"

        "extension"       | "extension"           | "Test1"                                                     | "String"
        "extension"       | "extension"           | "Test2"                                                     | "Provider<String>"
        "extension"       | "extension.set"       | "Test1"                                                     | "String"
        "extension"       | "extension.set"       | "Test2"                                                     | "Provider<String>"
        "extension"       | "setExtension"        | "Test3"                                                     | "String"
        "extension"       | "setExtension"        | "Test4"                                                     | "Provider<String>"

        "classifier"      | "classifier"          | "Test1"                                                     | "String"
        "classifier"      | "classifier"          | "Test2"                                                     | "Provider<String>"
        "classifier"      | "classifier.set"      | "Test1"                                                     | "String"
        "classifier"      | "classifier.set"      | "Test2"                                                     | "Provider<String>"
        "classifier"      | "setClassifier"       | "Test3"                                                     | "String"
        "classifier"      | "setClassifier"       | "Test4"                                                     | "Provider<String>"

        "destinationDir"  | "destinationDir"      | "/some/path"                                                | "File"
        "destinationDir"  | "destinationDir"      | "/some/path"                                                | "Provider<Directory>"
        "destinationDir"  | "destinationDir.set"  | "/some/path"                                                | "File"
        "destinationDir"  | "destinationDir.set"  | "/some/path"                                                | "Provider<Directory>"
        "destinationDir"  | "setDestinationDir"   | "/some/path"                                                | "File"
        "destinationDir"  | "setDestinationDir"   | "/some/path"                                                | "Provider<Directory>"

        "buildSettings"   | "buildSettings"       | '[SOME_SETTING=some/value]'                                 | "BuildSettings"
        "buildSettings"   | "buildSettings"       | '[SOME_SETTING=some/value, MORE_SETTINGS=some/other/value]' | "Provider<BuildSettings>"
        "buildSettings"   | "buildSettings.set"   | '[SOME_SETTING=some/value]'                                 | "BuildSettings"
        "buildSettings"   | "buildSettings.set"   | '[SOME_SETTING=some/value, MORE_SETTINGS=some/other/value]' | "Provider<BuildSettings>"
        "buildSettings"   | "setBuildSettings"    | '[SOME_SETTING=some/value]'                                 | "BuildSettings"
        "buildSettings"   | "setBuildSettings"    | '[SOME_SETTING=some/value, MORE_SETTINGS=some/other/value]' | "Provider<BuildSettings>"

        value = wrapValueBasedOnType(rawValue, type) { type ->
            switch (type) {
                case BuildSettings.class.simpleName:
                    return "new ${BuildSettings.class.name}()" + rawValue.replaceAll(/(\[|\])/, '').split(',').collect({
                        def parts = it.split("=")
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
            task("customXcodeArchive", type: ${XcodeArchive.name}) {
                archiveName("test-0.0.0.xcarchive")
                destinationDir(file("/some/path"))
            }
        """.stripIndent()

        and: "a task to read back the value"
        buildFile << """
            task("readValue") {
                doLast {
                    println("xcArchivePath: '" + customXcodeArchive.${property}.get() + "'")
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

    @Unroll("constructs archive name #expectedValue from baseName: #baseName, appendix: #appendix version: #version classifier: #classifier extension: extension archiveName: #archiveName")
    def "set archive name"() {
        given: "a custom archive task"
        buildFile << """
            task("customXcodeArchive", type: ${XcodeArchive.name})
        """.stripIndent()

        and: "a custom project name"
        settingsFile.text = """
        rootProject.name='${defaultBaseName}'
        """.trim().stripIndent()

        and: "a custom project version"
        buildFile << """
            version = '${defaultVersion}'
        """.stripIndent()

        and: "a task to read back the value"
        buildFile << """
            task("readValue") {
                doLast {
                    println("archiveName: '" + customXcodeArchive.archiveName.get() + "'")
                }
            }
        """.stripIndent()

        and: "a set propertis"
        archiveNameParts.each {
            if (it.value != _) {
                buildFile << """
            customXcodeArchive.${it.key}(${wrapValueBasedOnType(it.value, "String")})
            """.stripIndent()
            }
        }

        when:
        def result = runTasksSuccessfully("readValue")

        then:

        outputContains(result, "archiveName: '" + expectedValue.toString() + "'")

        where:
        baseName | appendix | version | classifier | extension | archiveName  | expectedArchivePattern
        _        | null     | _       | null       | _         | _            | "#baseName-#version.#extension"
        null     | null     | _       | null       | _         | _            | "#version.#extension"
        'test'   | 'suite'  | '0.1.0' | 'case'     | 'xml'     | _            | "#baseName-#appendix-#version-#classifier.#extension"
        'test'   | 'suite'  | '0.1.0' | 'case'     | null      | _            | "#baseName-#appendix-#version-#classifier"
        'test'   | 'suite'  | '0.1.0' | 'case'     | null      | _            | "#baseName-#appendix-#version-#classifier"
        'test'   | 'suite'  | '0.1.0' | null       | 'xml'     | _            | "#baseName-#appendix-#version.#extension"
        'test'   | 'suite'  | '0.1.0' | null       | null      | _            | "#baseName-#appendix-#version"
        'test'   | 'suite'  | null    | 'case'     | 'xml'     | _            | "#baseName-#appendix-#classifier.#extension"
        'test'   | 'suite'  | null    | null       | 'xml'     | _            | "#baseName-#appendix.#extension"
        'test'   | 'suite'  | null    | null       | null      | _            | "#baseName-#appendix"
        'test'   | null     | '0.1.0' | 'case'     | 'xml'     | _            | "#baseName-#version-#classifier.#extension"
        'test'   | null     | '0.1.0' | null       | 'xml'     | _            | "#baseName-#version.#extension"
        'test'   | null     | '0.1.0' | null       | null      | _            | "#baseName-#version"
        'test'   | null     | null    | 'case'     | 'xml'     | _            | "#baseName-#classifier.#extension"
        'test'   | null     | null    | null       | 'xml'     | _            | "#baseName.#extension"
        'test'   | null     | null    | null       | null      | _            | "#baseName"
        null     | 'suite'  | '0.1.0' | 'case'     | 'xml'     | _            | "#appendix-#version-#classifier.#extension"
        null     | 'suite'  | null    | 'case'     | 'xml'     | _            | "#appendix-#classifier.#extension"
        null     | 'suite'  | null    | 'case'     | null      | _            | "#appendix-#classifier"
        null     | 'suite'  | null    | null       | 'xml'     | _            | "#appendix.#extension"
        null     | 'suite'  | null    | null       | null      | _            | "#appendix"
        null     | null     | '0.1.0' | 'case'     | 'xml'     | _            | "#version-#classifier.#extension"
        null     | null     | '0.1.0' | null       | 'xml'     | _            | "#version.#extension"
        null     | null     | '0.1.0' | null       | null      | _            | "#version"
        null     | null     | null    | 'case'     | 'xml'     | _            | "#classifier.#extension"
        null     | null     | null    | 'case'     | null      | _            | "#classifier"
        null     | null     | null    | null       | 'xml'     | _            | ".#extension"
        null     | null     | null    | null       | null      | _            | ""
        _        | null     | _       | null       | _         | 'customName' | 'customName'
        null     | null     | _       | null       | _         | 'customName' | 'customName'
        'test'   | 'suite'  | '0.1.0' | 'case'     | 'xml'     | 'customName' | 'customName'
        'test'   | 'suite'  | '0.1.0' | 'case'     | null      | 'customName' | 'customName'
        'test'   | 'suite'  | '0.1.0' | 'case'     | null      | 'customName' | 'customName'
        'test'   | 'suite'  | '0.1.0' | null       | 'xml'     | 'customName' | 'customName'
        'test'   | 'suite'  | '0.1.0' | null       | null      | 'customName' | 'customName'
        'test'   | 'suite'  | null    | 'case'     | 'xml'     | 'customName' | 'customName'
        'test'   | 'suite'  | null    | null       | 'xml'     | 'customName' | 'customName'
        'test'   | 'suite'  | null    | null       | null      | 'customName' | 'customName'
        'test'   | null     | '0.1.0' | 'case'     | 'xml'     | 'customName' | 'customName'
        'test'   | null     | '0.1.0' | null       | 'xml'     | 'customName' | 'customName'
        'test'   | null     | '0.1.0' | null       | null      | 'customName' | 'customName'
        'test'   | null     | null    | 'case'     | 'xml'     | 'customName' | 'customName'
        'test'   | null     | null    | null       | 'xml'     | 'customName' | 'customName'
        'test'   | null     | null    | null       | null      | 'customName' | 'customName'
        null     | 'suite'  | '0.1.0' | 'case'     | 'xml'     | 'customName' | 'customName'
        null     | 'suite'  | null    | 'case'     | 'xml'     | 'customName' | 'customName'
        null     | 'suite'  | null    | 'case'     | null      | 'customName' | 'customName'
        null     | 'suite'  | null    | null       | 'xml'     | 'customName' | 'customName'
        null     | 'suite'  | null    | null       | null      | 'customName' | 'customName'
        null     | null     | '0.1.0' | 'case'     | 'xml'     | 'customName' | 'customName'
        null     | null     | '0.1.0' | null       | 'xml'     | 'customName' | 'customName'
        null     | null     | '0.1.0' | null       | null      | 'customName' | 'customName'
        null     | null     | null    | 'case'     | 'xml'     | 'customName' | 'customName'
        null     | null     | null    | 'case'     | null      | 'customName' | 'customName'
        null     | null     | null    | null       | 'xml'     | 'customName' | 'customName'
        null     | null     | null    | null       | null      | 'customName' | 'customName'

        defaultVersion = '0.0.0'
        defaultBaseName = 'atlasBuildIos'
        defaultExtension = 'xcarchive'

        expectedValue = expectedArchivePattern.replace('#baseName', (baseName == _ ? defaultBaseName : baseName ?: '').toString())
                .replace('#version', (version == _ ? defaultVersion : version ?: '').toString())
                .replace('#extension', (extension == _ ? defaultExtension : extension ?: '').toString())
                .replace('#appendix', (appendix ?: '').toString())
                .replace('#classifier', (classifier ?: '').toString())

        archiveNameParts = ['baseName': baseName, 'version': version, 'appendix': appendix, 'extension': extension, 'classifier': classifier, 'archiveName': archiveName]
    }
}
