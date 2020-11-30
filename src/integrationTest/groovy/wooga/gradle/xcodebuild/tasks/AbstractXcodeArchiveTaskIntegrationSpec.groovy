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

import spock.lang.Unroll

abstract class AbstractXcodeArchiveTaskIntegrationSpec extends AbstractXcodeTaskIntegrationSpec {

    @Unroll("can set property #property with #method and type #type")
    def "can set property AbstractXcodeArchiveTask"() {
        given: "a custom archive task"
        buildFile << """
            task("${testTaskName}", type: ${XcodeArchive.name})
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
        property         | method               | rawValue     | type
        "archiveName"    | "archiveName"        | "Test1"      | "String"
        "archiveName"    | "archiveName"        | "Test2"      | "Provider<String>"
        "archiveName"    | "archiveName.set"    | "Test1"      | "String"
        "archiveName"    | "archiveName.set"    | "Test2"      | "Provider<String>"
        "archiveName"    | "setArchiveName"     | "Test3"      | "String"
        "archiveName"    | "setArchiveName"     | "Test4"      | "Provider<String>"

        "baseName"       | "baseName"           | "Test1"      | "String"
        "baseName"       | "baseName"           | "Test2"      | "Provider<String>"
        "baseName"       | "baseName.set"       | "Test1"      | "String"
        "baseName"       | "baseName.set"       | "Test2"      | "Provider<String>"
        "baseName"       | "setBaseName"        | "Test3"      | "String"
        "baseName"       | "setBaseName"        | "Test4"      | "Provider<String>"

        "appendix"       | "appendix"           | "Test1"      | "String"
        "appendix"       | "appendix"           | "Test2"      | "Provider<String>"
        "appendix"       | "appendix.set"       | "Test1"      | "String"
        "appendix"       | "appendix.set"       | "Test2"      | "Provider<String>"
        "appendix"       | "setAppendix"        | "Test3"      | "String"
        "appendix"       | "setAppendix"        | "Test4"      | "Provider<String>"

        "version"        | "version"            | "Test1"      | "String"
        "version"        | "version"            | "Test2"      | "Provider<String>"
        "version"        | "version.set"        | "Test1"      | "String"
        "version"        | "version.set"        | "Test2"      | "Provider<String>"
        "version"        | "setVersion"         | "Test3"      | "String"
        "version"        | "setVersion"         | "Test4"      | "Provider<String>"

        "extension"      | "extension"          | "Test1"      | "String"
        "extension"      | "extension"          | "Test2"      | "Provider<String>"
        "extension"      | "extension.set"      | "Test1"      | "String"
        "extension"      | "extension.set"      | "Test2"      | "Provider<String>"
        "extension"      | "setExtension"       | "Test3"      | "String"
        "extension"      | "setExtension"       | "Test4"      | "Provider<String>"

        "classifier"     | "classifier"         | "Test1"      | "String"
        "classifier"     | "classifier"         | "Test2"      | "Provider<String>"
        "classifier"     | "classifier.set"     | "Test1"      | "String"
        "classifier"     | "classifier.set"     | "Test2"      | "Provider<String>"
        "classifier"     | "setClassifier"      | "Test3"      | "String"
        "classifier"     | "setClassifier"      | "Test4"      | "Provider<String>"

        "destinationDir" | "destinationDir"     | "/some/path" | "File"
        "destinationDir" | "destinationDir"     | "/some/path" | "Provider<Directory>"
        "destinationDir" | "destinationDir.set" | "/some/path" | "File"
        "destinationDir" | "destinationDir.set" | "/some/path" | "Provider<Directory>"
        "destinationDir" | "setDestinationDir"  | "/some/path" | "File"
        "destinationDir" | "setDestinationDir"  | "/some/path" | "Provider<Directory>"

        value = wrapValueBasedOnType(rawValue, type)
        expectedValue = rawValue
    }


    @Unroll("constructs archive name #expectedValue from baseName: #baseName, appendix: #appendix version: #version classifier: #classifier extension: extension archiveName: #archiveName")
    def "set archive name"() {
        given: "a custom archive task"
        buildFile << """
            task("${testTaskName}", type: ${XcodeArchive.name})
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
                    println("archiveName: '" + ${testTaskName}.archiveName.get() + "'")
                }
            }
        """.stripIndent()

        and: "a set propertis"
        archiveNameParts.each {
            if (it.value != _) {
                buildFile << """
                ${testTaskName}.${it.key}(${wrapValueBasedOnType(it.value, "String")})
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
