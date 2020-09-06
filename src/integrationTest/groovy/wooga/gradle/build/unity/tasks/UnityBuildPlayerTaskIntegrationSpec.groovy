/*
 * Copyright 2018-2020 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.build.unity.tasks

import org.apache.commons.io.FilenameUtils
import org.yaml.snakeyaml.Yaml
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.build.UnityIntegrationSpec
import wooga.gradle.build.unity.secrets.internal.EncryptionSpecHelper
import wooga.gradle.secrets.internal.SecretText
import wooga.gradle.secrets.internal.Secrets
import wooga.gradle.unity.batchMode.BatchModeFlags

import javax.crypto.spec.SecretKeySpec

class UnityBuildPlayerTaskIntegrationSpec extends UnityIntegrationSpec {

    def setup() {
        def assets = new File(projectDir, "Assets")
        def appConfigsDir = new File(assets, "CustomConfigs")
        appConfigsDir.mkdirs()

        def appConfig = ['MonoBehaviour': ['bundleId': 'net.wooga.test', 'batchModeBuildTarget': 'android']]
        ['custom', 'test'].collect { createFile("${it}.asset", appConfigsDir) }.each {
            Yaml yaml = new Yaml()
            it << yaml.dump(appConfig)
        }

        buildFile << """
            task("exportCustom", type: wooga.gradle.build.unity.tasks.UnityBuildPlayerTask) {
                appConfigFile = file('Assets/CustomConfigs/custom.asset')
            }
        """.stripIndent()
    }

    def "uses default settings when not configured"() {
        given: "a custom export task without configuration"

        when:
        def result = runTasksSuccessfully("exportCustom")

        then:
        result.standardOutput.contains("-executeMethod Wooga.UnifiedBuildSystem.Build.Export")
        result.standardOutput.contains("version=unspecified")
        result.standardOutput.contains("outputPath=${new File(projectDir, '/build/export/custom/project').path}")
        !result.standardOutput.contains("toolsVersion=")
    }

    @Issue("https://github.com/wooga/atlas-build-unity/issues/23")
    def "clear buildTarget setting"() {
        given: "a project setting default build target for all Unity tasks"
        buildFile << """
            unity.defaultBuildTarget = "ios"
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("exportCustom")

        then:
        result.standardOutput.contains("-buildTarget android")
        !result.standardOutput.contains("-buildTarget ios")
    }

    @Unroll
    def "can override #methodIsOptional property #property"() {
        given: "a export task with custom configuration"
        buildFile << """
            exportCustom {
                ${property} = ${value}
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("exportCustom")

        then:

        if (expectedToolsVersion) {
            result.standardOutput.contains("toolsVersion=${expectedToolsVersion}")
        }

        if (expectedCommitHash) {
            result.standardOutput.contains("commitHash=${expectedCommitHash}")
        }

        if (expectedVersionCode) {
            result.standardOutput.contains("versionCode=${expectedVersionCode}")
        }

        result.standardOutput.contains("-executeMethod ${expectedExportMethod}")
        result.standardOutput.contains("version=${expectedVersion}")

        result.standardOutput.contains("outputPath=${new File(projectDir, expectedOutputPath).path}")

        where:
        property              | rawValue                          | type     | useSetter
        "exportMethodName"    | "method1"                         | 'String' | true
        "version"             | "1.0.0"                           | 'String' | true
        "versionCode"         | "100000"                          | 'String' | true
        "toolsVersion"        | "1.0.0"                           | 'String' | true
        "commitHash"          | "abcdef123456"                    | 'String' | true
        "outputDirectoryBase" | "build/customExport3"             | 'File'   | true
        "appConfigFile"       | "Assets/CustomConfigs/test.asset" | 'File'   | true

        expectedExportMethod = (property == "exportMethodName") ? rawValue : 'Wooga.UnifiedBuildSystem.Build.Export'

        expectedVersion = (property == "version") ? rawValue : 'unspecified'
        expectedToolsVersion = (property == "toolsVersion") ? rawValue : null
        expectedVersionCode = (property == "versionCode") ? rawValue : null
        expectedCommitHash = (property == "commitHash") ? rawValue : null

        expectedOutputDirectoryBase = (property == 'outputDirectoryBase') ? rawValue : "/build/export"
        expectedAppConfigFile = (property == 'appConfigFile') ? new File(rawValue) : new File("Assets/CustomConfigs/custom.asset")

        expectedOutputPath = "$expectedOutputDirectoryBase/${FilenameUtils.removeExtension(expectedAppConfigFile.name)}/project"

        methodIsOptional = (property == "toolsVersion" || property == "versionCode") ? 'optional' : ''
        value = wrapValueBasedOnType(rawValue, type)
    }

    @Unroll("can append custom arguments with #message with property '#property'")
    def "can provide custom arguments"() {
        given: "a export task with custom configuration"
        buildFile << """
            exportCustom {
                ${property} = ${value}
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("exportCustom")

        then:
        expectedProperties.every { result.standardOutput.contains(it) }

        where:
        property          | rawValue                        | type  | useSetter | message
        "customArguments" | null                            | 'Map' | true      | "null value"
        "customArguments" | [:]                             | 'Map' | true      | "empty map"
        "customArguments" | ['foo': 'bar']                  | 'Map' | true      | "simple map"
        "customArguments" | ['foo': 'bar', 'baz': 'faz']    | 'Map' | true      | "multiple values"
        "customArguments" | ['anInt': 22]                   | 'Map' | true      | "integer values"
        "customArguments" | ['anInt': 22.2]                 | 'Map' | true      | "float values"
        "customArguments" | ['aFile': File.createTempDir()] | 'Map' | true      | "file values"
        expectedProperties = (rawValue) ? rawValue.collect({ key, value -> "${key}=${value};" }) : ""
        value = wrapValueBasedOnType(rawValue, type)
    }

    @Unroll
    def "#message buildTarget from appConfig when value is #valueType"() {
        given: "a custom app config"
        def assets = new File(projectDir, "Assets")
        def appConfigsDir = new File(assets, "CustomConfigs")

        def appConfigFile = createFile('buildTarget_config.asset', appConfigsDir)
        appConfigFile.text = "MonoBehaviour: {bundleId: net.wooga.test, batchModeBuildTarget: $batchModeBuildTarget}"

        and: "the app config configured"
        buildFile << """
            exportCustom.appConfigFile = file('${escapedPath(appConfigFile.path)}')
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("exportCustom")

        then:
        result.standardOutput.contains(" ${BatchModeFlags.BUILD_TARGET}") == shouldContainBuildTargetFlag
        if (shouldContainBuildTargetFlag) {
            result.standardOutput.contains(" ${BatchModeFlags.BUILD_TARGET} ${batchModeBuildTarget}")
        }

        where:
        batchModeBuildTarget | valueType             | shouldContainBuildTargetFlag
        'ios'                | 'string'              | true
        "'ios'"              | 'quoted string'       | true
        ''                   | 'empty'               | false
        "''"                 | 'quoted empty string' | false
        null                 | 'null'                | false
        message = shouldContainBuildTargetFlag ? 'use' : 'skip'
    }

    @Unroll
    def "up-to-date check returns false when input parameter: #property changes"() {
        given: "up to date task"
        runTasksSuccessfully("exportCustom")
        def result = runTasksSuccessfully("exportCustom")
        assert result.wasUpToDate('exportCustom')

        when: "update an input property"
        buildFile << """
            exportCustom.${property} = ${value}
        """.stripIndent()

        result = runTasksSuccessfully("exportCustom")

        then:
        !result.wasUpToDate('exportCustom')

        where:
        property           | value
        "exportMethodName" | "'method1'"
        "appConfigFile"    | "file('Assets/CustomConfigs/test.asset')"
        "version"          | "'1.0.1'"
        "versionCode"      | "'100100'"
    }

    def "task skips with no-source when input files are empty"() {
        given: "a task with empty input source"
        buildFile << """
            exportCustom.inputFiles.setFrom(files())
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("exportCustom")

        then:
        result.standardOutput.contains(":exportCustom NO-SOURCE")
    }

    @Shared
    def mockProjectFiles = [
            [new File("Assets/Plugins.meta"), false],
            [new File("Library/SomeCache.asset"), true],
            [new File("ProjectSettings/SomeSettings.asset"), false],
            [new File("UnityPackageManager/manifest.json"), false],
            [new File("Assets/Plugins/iOS.meta"), true],
            [new File("Assets/Plugins/iOS/somefile.m"), true],
            [new File("Assets/Plugins/iOS/somefile.m.meta"), true],
            [new File("Assets/Nested.meta"), false],
            [new File("Assets/Nested/Plugins.meta"), false],
            [new File("Assets/Nested/Plugins/iOS.meta"), true],
            [new File("Assets/Nested/Plugins/iOS/somefile.m"), true],
            [new File("Assets/Nested/Plugins/iOS/somefile.m.meta"), true],
            [new File("Assets/Plugins/WebGL.meta"), true],
            [new File("Assets/Plugins/WebGL/somefile.ts"), true],
            [new File("Assets/Plugins/WebGL/somefile.ts.meta"), true],
            [new File("Assets/Nested/Plugins/WebGL.meta"), true],
            [new File("Assets/Nested/Plugins/WebGL/somefile.ts"), true],
            [new File("Assets/Nested/Plugins/WebGL/somefile.ts.meta"), true],
            [new File("Assets/Editor.meta"), false],
            [new File("Assets/Editor/somefile.cs"), false],
            [new File("Assets/Editor/somefile.cs.meta"), false],
            [new File("Assets/Nested/Editor/somefile.cs"), false],
            [new File("Assets/Source.cs"), false],
            [new File("Assets/Source.cs.meta"), false],
            [new File("Assets/Nested/LevelEditor.meta"), false],
            [new File("Assets/Nested/LevelEditor/somefile.cs"), false],
            [new File("Assets/Nested/LevelEditor/somefile.cs.meta"), false],
            [new File("Assets/Plugins/Android.meta"), false],
            [new File("Assets/Plugins/Android/somefile.java"), false],
            [new File("Assets/Plugins/Android/somefile.java.meta"), false],
            [new File("Assets/Nested/Plugins/Android.meta"), false],
            [new File("Assets/Nested/Plugins/Android/s.java"), false],
            [new File("Assets/Nested/Plugins/Android/s.java.meta"), false],
    ]

    @Unroll
    def "task #statusMessage up-to-date when #file changed with default inputFiles"() {
        given: "a mocked unity project"
        //need to convert the relative files to absolute files
        def (_, File testFile) = prepareMockedProject(projectDir, files as Iterable<File>, file as File)

        and: "a up-to-date project state"
        def result = runTasksSuccessfully("exportCustom")
        assert !result.wasUpToDate('exportCustom')

        result = runTasksSuccessfully("exportCustom")
        assert result.wasUpToDate('exportCustom')

        when: "change content of one source file"
        testFile.text = "new content"

        result = runTasksSuccessfully("exportCustom")

        then:
        result.wasUpToDate('exportCustom') == upToDate

        where:
        files = mockProjectFiles.collect { it[0] }
        [file, upToDate] << mockProjectFiles
        statusMessage = (upToDate) ? "is" : "is not"
    }

    def "can pass provided secrets in environment"() {
        given: "a secrets file an matching key"
        Secrets secrets = new Secrets()
        SecretKeySpec key = EncryptionSpecHelper.createSecretKey("some_value")
        secrets.putSecret(secretId, new SecretText(secretValue), key)

        and: "serialized key and secrets text"
        def secretsKey = File.createTempFile("atlas-build-unity.GradleBuild", ".key")
        def secretsFile = File.createTempFile("atlas-build-unity.GradleBuild", ".secrets.yaml")

        secretsKey.bytes = key.encoded
        secretsFile.text = secrets.encode()

        and: "secrets and key configured in task"
        buildFile << """
            import javax.crypto.spec.SecretKeySpec
            exportCustom.secretsFile = project.file('${escapedPath(secretsFile.path)}')
            exportCustom.secretsKey = new SecretKeySpec(project.file('${escapedPath(secretsKey.path)}').bytes, 'AES')
        """.stripIndent()

        when:
        def result = runTasksSuccessfully('exportCustom')

        then:
        result.standardOutput.contains("${secretId.toUpperCase()}=${secretValue}")

        where:
        secretId  | secretValue
        "secret1" | "secret1Value"
    }

    @Unroll
    def "can set custom inputFiles for up-to-date check #type"() {
        given: "a mocked unity project"
        //need to convert the relative files to absolute files
        def (_, File testFile) = prepareMockedProject(projectDir, files as Iterable<File>, file as File)

        and: "a custom inputCollection"
        buildFile << """
            exportCustom.${methodName}.setFrom(${value})
        """.stripIndent()

        and: "a up-to-date project state"
        def result = runTasksSuccessfully("exportCustom")
        assert !result.wasUpToDate('exportCustom')

        result = runTasksSuccessfully("exportCustom")
        assert result.wasUpToDate('exportCustom')

        when: "change content of one source file"
        testFile.text = "new content"

        result = runTasksSuccessfully("exportCustom")

        then:
        result.wasUpToDate('exportCustom') == upToDate

        where:
        file                                          | upToDate | type             | value
        new File("Assets/Plugins/iOS/somefile.m")     | true     | 'FileTree'       | 'project.fileTree(project.projectDir){include("Assets/**"); exclude("**/Plugins/iOS/**")}'
        new File("Assets/Plugins/Android/somefile.m") | false    | 'FileTree'       | 'project.fileTree(project.projectDir){include("Assets/**"); exclude("**/Plugins/iOS/**")}'
        new File("Assets/Source.cs")                  | false    | 'FileTree'       | 'project.fileTree(project.projectDir){include("Assets/**"); exclude("**/Plugins/iOS/**")}'
        new File("Assets/Plugins/iOS/somefile.m")     | false    | 'FileTree'       | 'project.fileTree(project.projectDir){include("Assets/**"); exclude("**/Plugins/Android/**")}'
        new File("Assets/Plugins/Android/somefile.m") | true     | 'FileTree'       | 'project.fileTree(project.projectDir){include("Assets/**"); exclude("**/Plugins/Android/**")}'
        new File("Assets/Source.cs")                  | false    | 'FileTree'       | 'project.fileTree(project.projectDir){include("Assets/**"); exclude("**/Plugins/Android/**")}'
        new File("Assets/Editor/somefile.cs")         | true     | 'FileCollection' | 'project.files("Assets/Editor/anyfile.cs","Assets/Source.cs")'
        new File("Assets/Source.cs")                  | false    | 'FileCollection' | 'project.files("Assets/Editor/anyfile.cs","Assets/Source.cs")'

        files = mockProjectFiles.collect { it[0] }

        statusMessage = (upToDate) ? "is" : "is not"
        methodName = "inputFiles"
    }

    @Unroll
    def "can exclude files with custom fileCollection for up-to-date check"() {
        given: "a mocked unity project"
        //need to convert the relative files to absolute files
        def (_, File testFile) = prepareMockedProject(projectDir, files as Iterable<File>, file as File)

        and: "a custom ignore collection"
        buildFile << """
            unityBuild.${methodName}.setFrom(${value})
        """.stripIndent()

        and: "a up-to-date project state"
        def result = runTasksSuccessfully("exportCustom")
        assert !result.wasUpToDate('exportCustom')

        result = runTasksSuccessfully("exportCustom")
        assert result.wasUpToDate('exportCustom')

        when: "change content of one source file"
        testFile.text = "new content"

        result = runTasksSuccessfully("exportCustom")

        then:
        result.wasUpToDate('exportCustom') == upToDate

        where:
        file                                              | upToDate | type             | value
        new File("Assets/Source.cs.meta")                 | true     | 'FileCollection' | 'project.files("Assets/Source.cs.meta","Assets/Source.cs","ProjectSettings/SomeSettings.asset")'
        new File("Assets/Source.cs")                      | true     | 'FileCollection' | 'project.files("Assets/Source.cs.meta","Assets/Source.cs","ProjectSettings/SomeSettings.asset")'
        new File("Assets/Nested/LevelEditor/somefile.cs") | false    | 'FileCollection' | 'project.files("Assets/Source.cs.meta","Assets/Source.cs","ProjectSettings/SomeSettings.asset")'
        new File("ProjectSettings/SomeSettings.asset")    | true     | 'FileCollection' | 'project.files("Assets/Source.cs.meta","Assets/Source.cs","ProjectSettings/SomeSettings.asset")'
        new File("UnityPackageManager/manifest.json")     | true     | 'FileCollection' | 'project.files("UnityPackageManager/manifest.json","ProjectSettings/SomeSettings.asset")'
        files = mockProjectFiles.collect { it[0] }
        statusMessage = (upToDate) ? "is" : "is not"
        methodName = "ignoreFilesForExportUpToDateCheck"
    }

    Tuple prepareMockedProject(File projectDir, Iterable<File> files, File testFile) {
        files = files.collect { new File(projectDir, it.path) }
        testFile = new File(projectDir, testFile.path)

        //create directory structure
        files.each { f ->
            f.parentFile.mkdirs()
            f.text = "some content"
        }
        new Tuple(files, testFile)
    }
}
