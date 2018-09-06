/*
 * Copyright 2018 Wooga GmbH
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
 *
 */

package wooga.gradle.build.unity.tasks

import org.apache.commons.io.FilenameUtils
import org.yaml.snakeyaml.Yaml
import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.build.UnityIntegrationSpec
import wooga.gradle.unity.batchMode.BatchModeFlags

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
                appConfigFile file('Assets/CustomConfigs/custom.asset')
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

    @Unroll
    def "can override #methodIsOptional property #property with #methodName(#type)"() {
        given: "a export task with custom configuration"
        buildFile << """
            exportCustom.${methodName}(${value})
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("exportCustom")

        then:

        if (expectedToolsVersion) {
            result.standardOutput.contains("toolsVersion=${expectedToolsVersion}")
        }

        result.standardOutput.contains("-executeMethod ${expectedExportMethod}")
        result.standardOutput.contains("version=${expectedVersion}")
        result.standardOutput.contains("outputPath=${new File(projectDir, expectedOutputPath).path}")

        where:
        property              | rawValue                          | type       | useSetter
        "exportMethodName"    | "method1"                         | 'String'   | true
        "exportMethodName"    | "method2"                         | 'String'   | false
        "exportMethodName"    | "method3"                         | 'Callable' | true
        "exportMethodName"    | "method4"                         | 'Callable' | false
        "exportMethodName"    | "method5"                         | 'Closure'  | true
        "exportMethodName"    | "method6"                         | 'Closure'  | false
        "exportMethodName"    | "method7"                         | 'Object'   | true
        "exportMethodName"    | "method8"                         | 'Object'   | false
        "version"             | "1.0.0"                           | 'String'   | true
        "version"             | "1.0.1"                           | 'String'   | false
        "version"             | "2.0.0"                           | 'Callable' | true
        "version"             | "2.0.1"                           | 'Callable' | false
        "version"             | "3.0.0"                           | 'Closure'  | true
        "version"             | "3.0.1"                           | 'Closure'  | false
        "version"             | "4.0.0"                           | 'Object'   | true
        "version"             | "4.0.1"                           | 'Object'   | false
        "toolsVersion"        | "1.0.0"                           | 'String'   | true
        "toolsVersion"        | "1.0.1"                           | 'String'   | false
        "toolsVersion"        | "2.0.0"                           | 'Callable' | true
        "toolsVersion"        | "2.0.1"                           | 'Callable' | false
        "toolsVersion"        | "3.0.0"                           | 'Closure'  | true
        "toolsVersion"        | "3.0.1"                           | 'Closure'  | false
        "toolsVersion"        | "4.0.0"                           | 'Object'   | true
        "toolsVersion"        | "4.0.1"                           | 'Object'   | false
        "outputDirectoryBase" | "build/customExport"              | 'String'   | true
        "outputDirectoryBase" | "build/customExport2"             | 'String'   | false
        "outputDirectoryBase" | "build/customExport3"             | 'File'     | true
        "outputDirectoryBase" | "build/customExport4"             | 'File'     | false
        "outputDirectoryBase" | "build/customExport5"             | 'Closure'  | true
        "outputDirectoryBase" | "build/customExport6"             | 'Closure'  | false
        "outputDirectoryBase" | "build/customExport5"             | 'Callable' | true
        "outputDirectoryBase" | "build/customExport6"             | 'Callable' | false
        "appConfigFile"       | "Assets/CustomConfigs/test.asset" | 'String'   | true
        "appConfigFile"       | "Assets/CustomConfigs/test.asset" | 'String'   | false
        "appConfigFile"       | "Assets/CustomConfigs/test.asset" | 'File'     | true
        "appConfigFile"       | "Assets/CustomConfigs/test.asset" | 'File'     | false
        "appConfigFile"       | "Assets/CustomConfigs/test.asset" | 'Closure'  | true
        "appConfigFile"       | "Assets/CustomConfigs/test.asset" | 'Closure'  | false
        "appConfigFile"       | "Assets/CustomConfigs/test.asset" | 'Callable' | true
        "appConfigFile"       | "Assets/CustomConfigs/test.asset" | 'Callable' | false

        expectedExportMethod = (property == "exportMethodName") ? rawValue : 'Wooga.UnifiedBuildSystem.Build.Export'

        expectedVersion = (property == "version") ? rawValue : 'unspecified'
        expectedToolsVersion = (property == "toolsVersion") ? rawValue : null

        expectedOutputDirectoryBase = (property == 'outputDirectoryBase') ? rawValue : "/build/export"
        expectedAppConfigFile = (property == 'appConfigFile') ? new File(rawValue) : new File("Assets/CustomConfigs/custom.asset")

        expectedOutputPath = "$expectedOutputDirectoryBase/${FilenameUtils.removeExtension(expectedAppConfigFile.name)}/project"

        methodIsOptional = (property == "toolsVersion") ? 'optional' : ''
        methodName = (useSetter) ? "set${property.capitalize()}" : property
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
        'value'              | 'string'              | true
        "'value'"            | 'quoted string'       | true
        ''                   | 'empty'               | false
        "''"                 | 'quoted empty string' | false
        null                 | 'null'                | false
        message = shouldContainBuildTargetFlag ? 'use' : 'skip'
    }

    @Unroll
    def "up-to-date check returns false when input paramter: #property changes"() {
        given: "up to date task"
        runTasksSuccessfully("exportCustom")
        def result = runTasksSuccessfully("exportCustom")
        assert result.wasUpToDate('exportCustom')

        when: "update an input property"
        buildFile << """
            exportCustom.${methodName}('${value}')
        """.stripIndent()

        result = runTasksSuccessfully("exportCustom")

        then:
        !result.wasUpToDate('exportCustom')

        where:
        property           | value                             | useSetter
        "exportMethodName" | "method1"                         | true
        "exportMethodName" | "method2"                         | false
        "appConfigFile"    | "Assets/CustomConfigs/test.asset" | true
        "appConfigFile"    | "Assets/CustomConfigs/test.asset" | false
        "version"          | "1.0.1"                           | true
        "version"          | "1.1.0"                           | false
        methodName = (useSetter) ? "set${property.capitalize()}" : property
    }

    def "task skips with no-source when input files are empty"() {
        given: "a task with empty input source"
        buildFile << """
            exportCustom.inputFiles = files()
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
    def "task #statusMessage up-to-date when file change at location #file with default inputFiles"() {
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

    @Unroll
    def "can set custom inputFiles for up-to-date check with #methodName(#type)"() {
        given: "a mocked unity project"
        //need to convert the relative files to absolute files
        def (_, File testFile) = prepareMockedProject(projectDir, files as Iterable<File>, file as File)

        and: "a custom inputCollection"
        buildFile << """
            exportCustom.${methodName}(${value})
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
        file                                          | upToDate | type             | value                                                                                          | useGetter
        new File("Assets/Plugins/iOS/somefile.m")     | true     | 'FileTree'       | 'project.fileTree(project.projectDir){include("Assets/**"); exclude("**/Plugins/iOS/**")}'     | false
        new File("Assets/Plugins/Android/somefile.m") | false    | 'FileTree'       | 'project.fileTree(project.projectDir){include("Assets/**"); exclude("**/Plugins/iOS/**")}'     | false
        new File("Assets/Source.cs")                  | false    | 'FileTree'       | 'project.fileTree(project.projectDir){include("Assets/**"); exclude("**/Plugins/iOS/**")}'     | false
        new File("Assets/Plugins/iOS/somefile.m")     | false    | 'FileTree'       | 'project.fileTree(project.projectDir){include("Assets/**"); exclude("**/Plugins/Android/**")}' | true
        new File("Assets/Plugins/Android/somefile.m") | true     | 'FileTree'       | 'project.fileTree(project.projectDir){include("Assets/**"); exclude("**/Plugins/Android/**")}' | true
        new File("Assets/Source.cs")                  | false    | 'FileTree'       | 'project.fileTree(project.projectDir){include("Assets/**"); exclude("**/Plugins/Android/**")}' | true
        new File("Assets/Editor/somefile.cs")         | true     | 'FileCollection' | 'project.files("Assets/Editor/anyfile.cs","Assets/Source.cs")'                                 | false
        new File("Assets/Editor/somefile.cs")         | true     | 'FileCollection' | 'project.files("Assets/Editor/anyfile.cs","Assets/Source.cs")'                                 | true
        new File("Assets/Source.cs")                  | false    | 'FileCollection' | 'project.files("Assets/Editor/anyfile.cs","Assets/Source.cs")'                                 | false
        new File("Assets/Source.cs")                  | false    | 'FileCollection' | 'project.files("Assets/Editor/anyfile.cs","Assets/Source.cs")'                                 | true

        files = mockProjectFiles.collect { it[0] }

        statusMessage = (upToDate) ? "is" : "is not"
        methodName = (useGetter) ? "setInputFiles" : "inputFiles"
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
