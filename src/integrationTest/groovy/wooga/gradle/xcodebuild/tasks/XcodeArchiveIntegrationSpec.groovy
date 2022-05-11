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

import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetInvocation
import com.wooga.gradle.test.writers.PropertySetterWriter
import net.wooga.test.xcode.XcodeTestProject
import org.junit.ClassRule
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Unroll

import static com.wooga.gradle.test.PropertyUtils.toSetter

class XcodeArchiveIntegrationSpec extends AbstractXcodeTaskIntegrationSpec<XcodeArchive> {

    @Shared
    @ClassRule
    XcodeTestProject xcodeProject = new XcodeTestProject()

    String workingXcodebuildTaskConfig = """
    task ${subjectUnderTestName}(type: ${subjectUnderTestTypeName}) {
        scheme = "${xcodeProject.schemeName}"
        baseName = "custom"
        version = "0.1.0"
        buildSettings {
            codeSignIdentity ""
            codeSigningRequired false
            codeSigningAllowed false
        }
        projectPath = ${wrapValueBasedOnType(xcodeProject.xcodeProject, File)}
    }
    """.stripIndent()

    String expectedPrettyColoredUnicodeLogOutput = """
        ▸ \u001B[39;1mLinking\u001B[0m xcodebuildPluginTest
    """.stripIndent().trim()

    String expectedPrettyUnicodeLogOutput = """
        ▸ Linking xcodebuildPluginTest
        """.stripIndent().trim()

    String expectedPrettyLogOutput = """
        > Linking xcodebuildPluginTest
        """.stripIndent().trim()

    @Requires({ os.macOs })
    @Unroll
    def "creates archive from #type"() {
        given:
        buildFile << workingXcodebuildTaskConfig
        buildFile << """
        ${subjectUnderTestName}.projectPath = new File("${path}")
        """.stripIndent()

        and: "a future xcarchive"
        def archive = new File(projectDir, "build/archives/custom-0.1.0.xcarchive")
        assert !archive.exists()

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        result.success
        archive.exists()
        archive.isDirectory()

        where:
        type          | path
        "xcodeprj"    | xcodeProject.xcodeProject.path
        "xcworkspace" | xcodeProject.xcodeWorkspace.path
    }

    @Requires({ os.macOs })
    def "fails when project is not a valid .xcodeprj or .xcworkspace"() {
        given: "temp fake project"
        def prj = File.createTempDir("someProject", ".project")
        def someProjectFile = new File(prj, "foo")
        someProjectFile.text = "temp"

        and:
        buildFile << workingXcodebuildTaskConfig
        buildFile << """
        ${subjectUnderTestName}.projectPath = file("${prj.absolutePath}")
        """.stripIndent()

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        outputContains(result, "xcode project path must be a valid .xcodeproj or .xcworkspace")
    }

    @Unroll("property #property sets flag #flag with value #rawValue")
    def "constructs build arguments"() {
        given:
        // Project path must be always set or it throws exception
        addMockTask(true, """
            projectPath = ${wrapValueBasedOnType(xcodeProject.xcodeProject.path, File)}
            scheme = "${xcodeProject.schemeName}"
        """.stripIndent())

        when:
        def query = runPropertyQuery(getter, setter)

        then:
        query.contains(flag)
        if (type != "Boolean") {
            query.contains(rawValue)
        }

        where:
        property          | flag                               | method                            | rawValue                            | type
        "configuration"   | "-configuration"                   | PropertySetInvocation.providerSet | "test"                              | "String"
        "clean"           | "clean"                            | PropertySetInvocation.providerSet | true                                | "Boolean"
        "scheme"          | "-scheme"                          | PropertySetInvocation.providerSet | "test"                              | "String"
        "teamId"          | "DEVELOPMENT_TEAM"                 | PropertySetInvocation.providerSet | "x123y"                             | "String"
        "buildKeychain"   | "OTHER_CODE_SIGN_FLAGS=--keychain" | PropertySetInvocation.providerSet | osPath("/some/path")                | "File"
        "derivedDataPath" | "derivedDataPath"                  | PropertySetInvocation.providerSet | osPath("/some/path")                | "File"
        "projectPath"     | "-project"                         | PropertySetInvocation.providerSet | osPath("/some/project.xcodeproj")   | "File"
        "projectPath"     | "-workspace"                       | PropertySetInvocation.providerSet | osPath("/some/project.xcworkspace") | "File"

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(rawValue, type)
            .use(method)
            .serialize(wrapValueFallback)
        getter = new PropertyGetterTaskWriter(subjectUnderTestName + ".arguments")
    }

    @Unroll("can set property #property with #method and type #type")
    def "can set property XcodeArchive"() {

        given: "a custom archive task"
        addMockTask(true)

        when:
        def query = runPropertyQuery(getter, setter)

        then:
        query.matches(rawValue)

        where:
        property          | method                            | rawValue             | type
        "configuration"   | PropertySetInvocation.method      | "Test1"              | "String"
        "configuration"   | PropertySetInvocation.method      | "Test2"              | "Provider<String>"
        "configuration"   | PropertySetInvocation.providerSet | "Test1"              | "String"
        "configuration"   | PropertySetInvocation.providerSet | "Test2"              | "Provider<String>"
        "configuration"   | PropertySetInvocation.setter      | "Test3"              | "String"
        "configuration"   | PropertySetInvocation.setter      | "Test4"              | "Provider<String>"

        "clean"           | PropertySetInvocation.method      | true                 | "Boolean"
        "clean"           | PropertySetInvocation.method      | true                 | "Provider<Boolean>"
        "clean"           | PropertySetInvocation.providerSet | true                 | "Boolean"
        "clean"           | PropertySetInvocation.providerSet | true                 | "Provider<Boolean>"
        "clean"           | PropertySetInvocation.setter      | true                 | "Boolean"
        "clean"           | PropertySetInvocation.setter      | true                 | "Provider<Boolean>"

        "scheme"          | PropertySetInvocation.method      | "Test1"              | "String"
        "scheme"          | PropertySetInvocation.method      | "Test2"              | "Provider<String>"
        "scheme"          | PropertySetInvocation.providerSet | "Test1"              | "String"
        "scheme"          | PropertySetInvocation.providerSet | "Test2"              | "Provider<String>"
        "scheme"          | PropertySetInvocation.setter      | "Test3"              | "String"
        "scheme"          | PropertySetInvocation.setter      | "Test4"              | "Provider<String>"

        "teamId"          | PropertySetInvocation.method      | "Test1"              | "String"
        "teamId"          | PropertySetInvocation.method      | "Test2"              | "Provider<String>"
        "teamId"          | PropertySetInvocation.providerSet | "Test1"              | "String"
        "teamId"          | PropertySetInvocation.providerSet | "Test2"              | "Provider<String>"
        "teamId"          | PropertySetInvocation.setter      | "Test3"              | "String"
        "teamId"          | PropertySetInvocation.setter      | "Test4"              | "Provider<String>"

        "derivedDataPath" | PropertySetInvocation.method      | osPath("/some/path") | "File"
        "derivedDataPath" | PropertySetInvocation.method      | osPath("/some/path") | "Provider<Directory>"
        "derivedDataPath" | PropertySetInvocation.providerSet | osPath("/some/path") | "File"
        "derivedDataPath" | PropertySetInvocation.providerSet | osPath("/some/path") | "Provider<Directory>"
        "derivedDataPath" | PropertySetInvocation.setter      | osPath("/some/path") | "File"
        "derivedDataPath" | PropertySetInvocation.setter      | osPath("/some/path") | "Provider<Directory>"

        "buildKeychain"   | PropertySetInvocation.method      | osPath("/some/path") | "File"
        "buildKeychain"   | PropertySetInvocation.method      | osPath("/some/path") | "Provider<RegularFile>"
        "buildKeychain"   | PropertySetInvocation.providerSet | osPath("/some/path") | "File"
        "buildKeychain"   | PropertySetInvocation.providerSet | osPath("/some/path") | "Provider<RegularFile>"
        "buildKeychain"   | PropertySetInvocation.setter      | osPath("/some/path") | "File"
        "buildKeychain"   | PropertySetInvocation.setter      | osPath("/some/path") | "Provider<RegularFile>"

        "projectPath"     | PropertySetInvocation.method      | osPath("/some/path") | "File"
        "projectPath"     | PropertySetInvocation.method      | osPath("/some/path") | "Provider<Directory>"
        "projectPath"     | PropertySetInvocation.providerSet | osPath("/some/path") | "File"
        "projectPath"     | PropertySetInvocation.providerSet | osPath("/some/path") | "Provider<Directory>"
        "projectPath"     | PropertySetInvocation.setter      | osPath("/some/path") | "File"
        "projectPath"     | PropertySetInvocation.setter      | osPath("/some/path") | "Provider<Directory>"

        "archiveName"     | PropertySetInvocation.method      | "Test1"              | "String"
        "archiveName"     | PropertySetInvocation.method      | "Test2"              | "Provider<String>"
        "archiveName"     | PropertySetInvocation.providerSet | "Test1"              | "String"
        "archiveName"     | PropertySetInvocation.providerSet | "Test2"              | "Provider<String>"
        "archiveName"     | PropertySetInvocation.setter      | "Test3"              | "String"
        "archiveName"     | PropertySetInvocation.setter      | "Test4"              | "Provider<String>"

        "baseName"        | PropertySetInvocation.method      | "Test1"              | "String"
        "baseName"        | PropertySetInvocation.method      | "Test2"              | "Provider<String>"
        "baseName"        | PropertySetInvocation.providerSet | "Test1"              | "String"
        "baseName"        | PropertySetInvocation.providerSet | "Test2"              | "Provider<String>"
        "baseName"        | PropertySetInvocation.setter      | "Test3"              | "String"
        "baseName"        | PropertySetInvocation.setter      | "Test4"              | "Provider<String>"

        "appendix"        | PropertySetInvocation.method      | "Test1"              | "String"
        "appendix"        | PropertySetInvocation.method      | "Test2"              | "Provider<String>"
        "appendix"        | PropertySetInvocation.providerSet | "Test1"              | "String"
        "appendix"        | PropertySetInvocation.providerSet | "Test2"              | "Provider<String>"
        "appendix"        | PropertySetInvocation.setter      | "Test3"              | "String"
        "appendix"        | PropertySetInvocation.setter      | "Test4"              | "Provider<String>"

        "version"         | PropertySetInvocation.method      | "Test1"              | "String"
        "version"         | PropertySetInvocation.method      | "Test2"              | "Provider<String>"
        "version"         | PropertySetInvocation.providerSet | "Test1"              | "String"
        "version"         | PropertySetInvocation.providerSet | "Test2"              | "Provider<String>"
        "version"         | PropertySetInvocation.setter      | "Test3"              | "String"
        "version"         | PropertySetInvocation.setter      | "Test4"              | "Provider<String>"

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(rawValue, type)
            .serialize(wrapValueFallback)
        getter = new PropertyGetterTaskWriter(setter)
    }

    @Unroll
    def "creates property #property from destination and archive name"() {
        given: "a custom archive task"
        buildFile << """
            task("${subjectUnderTestName}", type: ${XcodeArchive.name}) {
                archiveName.set(${wrapValueBasedOnType(archiveName, String)})
                destinationDir.set(${wrapValueBasedOnType(destinationDir, File)})
            }
        """.stripIndent()

        when:
        def query = runPropertyQuery(getter)

        then:
        query.matches(expectedXcArchivePath)

        where:
        property        | archiveName            | destinationDir       | expectedXcArchivePath
        "xcArchivePath" | "test-0.0.0.xcarchive" | osPath("/some/path") | new File(destinationDir, archiveName).path
        getter = new PropertyGetterTaskWriter(subjectUnderTestName + ".${property}")

    }

    def "multiple calls to arguments will not add build keychain path multiple times"() {
        given:
        buildFile << """
        task("customXcodeArchive", type: ${subjectUnderTestTypeName}) {
            scheme = "${xcodeProject.schemeName}"
            projectPath = ${wrapValueBasedOnType(xcodeProject.xcodeProject.path, File)}
        }
        """.stripIndent()

        and: "a set property"
        buildFile << """
            customXcodeArchive.buildKeychain.set(${wrapValueBasedOnType(keychainPath, File)})
        """.stripIndent()

        and: "a task to read the build arguments"
        buildFile << """
            task("readValue") {
                doLast {
                    customXcodeArchive.arguments.get()
                    customXcodeArchive.arguments.get()
                    println("arguments: " + customXcodeArchive.arguments.get().join(" "))
                }
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        !outputContains(result, "OTHER_CODE_SIGN_FLAGS=--keychain ${keychainPath} --keychain ${keychainPath} --keychain ${keychainPath}")
        !outputContains(result, "OTHER_CODE_SIGN_FLAGS=--keychain ${keychainPath} --keychain ${keychainPath}")
        outputContains(result, "OTHER_CODE_SIGN_FLAGS=--keychain ${keychainPath}")

        where:
        keychainPath = osPath("/path/to/keychain")
    }

    // NOTE: This task was moved from the abstract superclass since it was testing XcodeArchive only
    @Unroll("constructs archive name #expectedValue from baseName: #baseName, appendix: #appendix version: #version classifier: #classifier extension: extension archiveName: #archiveName")
    def "set archive name"() {
        given: "a custom archive task"
        addMockTask(true)

        and: "a custom project name"
        settingsFile.text = """
        rootProject.name='${defaultBaseName}'
        """.trim().stripIndent()

        and: "a custom project version"
        buildFile << """
            version = '${defaultVersion}'
        """.stripIndent()

        and: "a set properties"
        archiveNameParts.each {
            if (it.value != _) {
                if (it.value == null) {
                    buildFile << """
                    ${subjectUnderTestName}.${toSetter(it.key)}(null)
                    """.stripIndent()
                } else {
                    buildFile << """
                    ${subjectUnderTestName}.${toSetter(it.key)}(${wrapValueBasedOnType(it.value, "String")})
                    """.stripIndent()
                }
            }
        }

        when:
        def query = runPropertyQuery(getter)

        then:
        query.matches(expectedValue)

        where:
        baseName | appendix | version | classifier | extension | archiveName  | expectedArchivePattern
        _        | null     | _       | null       | _         | _            | "#baseName-#version.#extension"
        null     | null     | _       | null       | _         | _            | "#version.#extension"
        'test'   | 'suite'  | '0.1.0' | 'case'     | 'xml'     | _            | "#baseName-#appendix-#version-#classifier.#extension"
        'test'   | 'suite'  | '0.1.0' | 'case'     | null      | _            | "#baseName-#appendix-#version-#classifier.#extension"
        'test'   | 'suite'  | '0.1.0' | 'case'     | null      | _            | "#baseName-#appendix-#version-#classifier.#extension"
        'test'   | 'suite'  | '0.1.0' | null       | 'xml'     | _            | "#baseName-#appendix-#version.#extension"
        'test'   | 'suite'  | '0.1.0' | null       | null      | _            | "#baseName-#appendix-#version.#extension"
        'test'   | 'suite'  | null    | 'case'     | 'xml'     | _            | "#baseName-#appendix-#classifier.#extension"
        'test'   | 'suite'  | null    | null       | 'xml'     | _            | "#baseName-#appendix.#extension"
        'test'   | 'suite'  | null    | null       | null      | _            | "#baseName-#appendix.#extension"
        'test'   | null     | '0.1.0' | 'case'     | 'xml'     | _            | "#baseName-#version-#classifier.#extension"
        'test'   | null     | '0.1.0' | null       | 'xml'     | _            | "#baseName-#version.#extension"
        'test'   | null     | '0.1.0' | null       | null      | _            | "#baseName-#version.#extension"
        'test'   | null     | null    | 'case'     | 'xml'     | _            | "#baseName-#classifier.#extension"
        'test'   | null     | null    | null       | 'xml'     | _            | "#baseName.#extension"
        'test'   | null     | null    | null       | null      | _            | "#baseName.#extension"
        null     | 'suite'  | '0.1.0' | 'case'     | 'xml'     | _            | "#appendix-#version-#classifier.#extension"
        null     | 'suite'  | null    | 'case'     | 'xml'     | _            | "#appendix-#classifier.#extension"
        null     | 'suite'  | null    | 'case'     | null      | _            | "#appendix-#classifier.#extension"
        null     | 'suite'  | null    | null       | 'xml'     | _            | "#appendix.#extension"
        null     | 'suite'  | null    | null       | null      | _            | "#appendix.#extension"
        null     | null     | '0.1.0' | 'case'     | 'xml'     | _            | "#version-#classifier.#extension"
        null     | null     | '0.1.0' | null       | 'xml'     | _            | "#version.#extension"
        null     | null     | '0.1.0' | null       | null      | _            | "#version.#extension"
        null     | null     | null    | 'case'     | 'xml'     | _            | "#classifier.#extension"
        null     | null     | null    | 'case'     | null      | _            | "#classifier.#extension"
        null     | null     | null    | null       | 'xml'     | _            | ".#extension"
        null     | null     | null    | null       | null      | _            | ".#extension"
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
            .replace('#extension', (extension == _ ? defaultExtension : extension ?: defaultExtension).toString())
            .replace('#appendix', (appendix ?: '').toString())
            .replace('#classifier', (classifier ?: '').toString())

        archiveNameParts = ['baseName': baseName, 'version': version, 'appendix': appendix, 'extension': extension, 'classifier': classifier, 'archiveName': archiveName]

        getter = new PropertyGetterTaskWriter(subjectUnderTestName + ".archiveName")

    }
}
