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

package wooga.gradle.xcodebuild

import com.wooga.gradle.PlatformUtils
import com.wooga.gradle.PropertyUtils
import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.queries.TestValue
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetInvocation
import com.wooga.gradle.test.writers.PropertySetterWriter
import net.wooga.test.xcode.XcodeTestProject
import org.junit.ClassRule
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.xcodebuild.tasks.ArchiveDebugSymbols
import wooga.gradle.xcodebuild.tasks.ExportArchive
import wooga.gradle.xcodebuild.tasks.XcodeArchive

import static com.wooga.gradle.PlatformUtils.escapedPath

class XcodeBuildPluginIntegrationSpec extends XcodeBuildIntegrationSpec {

    @Shared
    @ClassRule
    XcodeTestProject xcodeProject = new XcodeTestProject()

    @Unroll()
    def "extension property #property of type #type sets #rawValue when #location"() {
        expect:
        runPropertyQuery(getter, setter).matches(rawValue)

        where:
        property          | method                            | rawValue                                                                                                | type                        | location
        "logsDir"         | _                                 | TestValue.set("custom/logs").expectProjectFile("build/custom/logs")                                     | _                           | PropertyLocation.environment
        "logsDir"         | _                                 | TestValue.set("custom/logs").expectProjectFile("build/custom/logs")                                     | _                           | PropertyLocation.property
        "logsDir"         | PropertySetInvocation.assignment  | TestValue.projectFile("build/custom/logs")                                                              | "File"                      | PropertyLocation.script
        "logsDir"         | PropertySetInvocation.assignment  | TestValue.set("custom/logs").expectProjectFile("build/custom/logs")                                     | "Provider<Directory>"       | PropertyLocation.script
        "logsDir"         | PropertySetInvocation.providerSet | TestValue.projectFile("build/custom/logs")                                                              | "File"                      | PropertyLocation.script
        "logsDir"         | PropertySetInvocation.providerSet | TestValue.set("custom/logs").expectProjectFile("build/custom/logs")                                     | "Provider<Directory>"       | PropertyLocation.script
        "logsDir"         | PropertySetInvocation.setter      | TestValue.projectFile("build/custom/logs")                                                              | "File"                      | PropertyLocation.script
        "logsDir"         | PropertySetInvocation.setter      | TestValue.set("custom/logs").expectProjectFile("build/custom/logs")                                     | "Provider<Directory>"       | PropertyLocation.script
        "logsDir"         | _                                 | TestValue.set(null).expectProjectFile("build/logs")                                                     | _                           | PropertyLocation.none

        "logsDir"         | _                                 | TestValue.set("custom/logs").expectProjectFile("build/custom/logs")                                     | _                           | PropertyLocation.environment
        "logsDir"         | _                                 | TestValue.set("custom/logs").expectProjectFile("build/custom/logs")                                     | _                           | PropertyLocation.property
        "logsDir"         | PropertySetInvocation.assignment  | osPath("/custom/logs")                                                                                  | "File"                      | PropertyLocation.script
        "logsDir"         | PropertySetInvocation.assignment  | osPath("/custom/logs")                                                                                  | "Provider<Directory>"       | PropertyLocation.script
        "logsDir"         | PropertySetInvocation.providerSet | osPath("/custom/logs")                                                                                  | "File"                      | PropertyLocation.script
        "logsDir"         | PropertySetInvocation.providerSet | osPath("/custom/logs")                                                                                  | "Provider<Directory>"       | PropertyLocation.script
        "logsDir"         | PropertySetInvocation.setter      | osPath("/custom/logs")                                                                                  | "File"                      | PropertyLocation.script
        "logsDir"         | PropertySetInvocation.setter      | osPath("/custom/logs")                                                                                  | "Provider<Directory>"       | PropertyLocation.script

        "derivedDataPath" | _                                 | TestValue.set("custom/derivedData").expectProjectFile("build/custom/derivedData")                       | _                           | PropertyLocation.environment
        "derivedDataPath" | _                                 | TestValue.set("custom/derivedData").expectProjectFile("build/custom/derivedData")                       | _                           | PropertyLocation.property
        "derivedDataPath" | PropertySetInvocation.assignment  | TestValue.projectFile("build/custom/derivedData")                                                       | "File"                      | PropertyLocation.script
        "derivedDataPath" | PropertySetInvocation.assignment  | TestValue.set("custom/derivedData").expectProjectFile("build/custom/derivedData")                       | "Provider<Directory>"       | PropertyLocation.script
        "derivedDataPath" | PropertySetInvocation.providerSet | TestValue.projectFile("build/custom/derivedData")                                                       | "File"                      | PropertyLocation.script
        "derivedDataPath" | PropertySetInvocation.providerSet | TestValue.set("custom/derivedData").expectProjectFile("build/custom/derivedData")                       | "Provider<Directory>"       | PropertyLocation.script
        "derivedDataPath" | PropertySetInvocation.setter      | TestValue.projectFile("build/custom/derivedData")                                                       | "File"                      | PropertyLocation.script
        "derivedDataPath" | PropertySetInvocation.setter      | TestValue.set("custom/derivedData").expectProjectFile("build/custom/derivedData")                       | "Provider<Directory>"       | PropertyLocation.script
        "derivedDataPath" | _                                 | TestValue.set(null).expectProjectFile("build/derivedData")                                              | _                           | PropertyLocation.none

        "derivedDataPath" | _                                 | TestValue.set("custom/derivedData").expectProjectFile("build/custom/derivedData")                       | _                           | PropertyLocation.environment
        "derivedDataPath" | _                                 | TestValue.set("custom/derivedData").expectProjectFile("build/custom/derivedData")                       | _                           | PropertyLocation.property
        "derivedDataPath" | PropertySetInvocation.assignment  | osPath("/custom/derivedData")                                                                           | "File"                      | PropertyLocation.script
        "derivedDataPath" | PropertySetInvocation.assignment  | osPath("/custom/derivedData")                                                                           | "Provider<Directory>"       | PropertyLocation.script
        "derivedDataPath" | PropertySetInvocation.providerSet | osPath("/custom/derivedData")                                                                           | "File"                      | PropertyLocation.script
        "derivedDataPath" | PropertySetInvocation.providerSet | osPath("/custom/derivedData")                                                                           | "Provider<Directory>"       | PropertyLocation.script
        "derivedDataPath" | PropertySetInvocation.setter      | osPath("/custom/derivedData")                                                                           | "File"                      | PropertyLocation.script
        "derivedDataPath" | PropertySetInvocation.setter      | osPath("/custom/derivedData")                                                                           | "Provider<Directory>"       | PropertyLocation.script

        "xarchivesDir"    | _                                 | TestValue.set("custom/archives").expectProjectFile("build/custom/archives")                             | _                           | PropertyLocation.environment
        "xarchivesDir"    | _                                 | TestValue.set("custom/archives").expectProjectFile("build/custom/archives")                             | _                           | PropertyLocation.property
        "xarchivesDir"    | PropertySetInvocation.assignment  | TestValue.projectFile("build/custom/archives")                                                          | "File"                      | PropertyLocation.script
        "xarchivesDir"    | PropertySetInvocation.assignment  | TestValue.set("custom/archives").expectProjectFile("build/custom/archives")                             | "Provider<Directory>"       | PropertyLocation.script
        "xarchivesDir"    | PropertySetInvocation.providerSet | TestValue.projectFile("build/custom/archives")                                                          | "File"                      | PropertyLocation.script
        "xarchivesDir"    | PropertySetInvocation.providerSet | TestValue.set("custom/archives").expectProjectFile("build/custom/archives")                             | "Provider<Directory>"       | PropertyLocation.script
        "xarchivesDir"    | PropertySetInvocation.setter      | TestValue.projectFile("build/custom/archives")                                                          | "File"                      | PropertyLocation.script
        "xarchivesDir"    | PropertySetInvocation.setter      | TestValue.set("custom/archives").expectProjectFile("build/custom/archives")                             | "Provider<Directory>"       | PropertyLocation.script
        "xarchivesDir"    | _                                 | TestValue.set(null).expectProjectFile("build/archives")                                                 | _                           | PropertyLocation.none

        "xarchivesDir"    | _                                 | osPath("/custom/archives")                                                                              | _                           | PropertyLocation.environment
        "xarchivesDir"    | _                                 | TestValue.set("custom/archives").expectProjectFile("build/custom/archives")                             | _                           | PropertyLocation.property
        "xarchivesDir"    | PropertySetInvocation.assignment  | osPath("/custom/archives")                                                                              | "File"                      | PropertyLocation.script
        "xarchivesDir"    | PropertySetInvocation.assignment  | osPath("/custom/archives")                                                                              | "Provider<Directory>"       | PropertyLocation.script
        "xarchivesDir"    | PropertySetInvocation.providerSet | osPath("/custom/archives")                                                                              | "File"                      | PropertyLocation.script
        "xarchivesDir"    | PropertySetInvocation.providerSet | osPath("/custom/archives")                                                                              | "Provider<Directory>"       | PropertyLocation.script
        "xarchivesDir"    | PropertySetInvocation.setter      | osPath("/custom/archives")                                                                              | "File"                      | PropertyLocation.script
        "xarchivesDir"    | PropertySetInvocation.setter      | osPath("/custom/archives")                                                                              | "Provider<Directory>"       | PropertyLocation.script

        "debugSymbolsDir" | _                                 | TestValue.set("custom/symbols").expectProjectFile("build/custom/symbols")                               | _                           | PropertyLocation.environment
        "debugSymbolsDir" | _                                 | TestValue.set("custom/symbols").expectProjectFile("build/custom/symbols")                               | _                           | PropertyLocation.property
        "debugSymbolsDir" | PropertySetInvocation.assignment  | TestValue.projectFile("build/custom/symbols")                                                           | "File"                      | PropertyLocation.script
        "debugSymbolsDir" | PropertySetInvocation.assignment  | TestValue.set("custom/symbols").expectProjectFile("build/custom/symbols")                               | "Provider<Directory>"       | PropertyLocation.script
        "debugSymbolsDir" | PropertySetInvocation.providerSet | TestValue.projectFile("build/custom/symbols")                                                           | "File"                      | PropertyLocation.script
        "debugSymbolsDir" | PropertySetInvocation.providerSet | TestValue.set("custom/symbols").expectProjectFile("build/custom/symbols")                               | "Provider<Directory>"       | PropertyLocation.script
        "debugSymbolsDir" | PropertySetInvocation.setter      | TestValue.projectFile("build/custom/symbols")                                                           | "File"                      | PropertyLocation.script
        "debugSymbolsDir" | PropertySetInvocation.setter      | TestValue.set("custom/symbols").expectProjectFile("build/custom/symbols")                               | "Provider<Directory>"       | PropertyLocation.script
        "debugSymbolsDir" | _                                 | TestValue.none().expectProjectFile("build/symbols")                                                     | _                           | PropertyLocation.none

        "debugSymbolsDir" | _                                 | osPath("/custom/symbols")                                                                               | _                           | PropertyLocation.environment
        "debugSymbolsDir" | _                                 | TestValue.set(escapedPath(osPath("/custom/symbols"))).expect(osPath("/custom/symbols"))                 | _                           | PropertyLocation.property
        "debugSymbolsDir" | PropertySetInvocation.assignment  | osPath("/custom/symbols")                                                                               | "File"                      | PropertyLocation.script
        "debugSymbolsDir" | PropertySetInvocation.assignment  | osPath("/custom/symbols")                                                                               | "Provider<Directory>"       | PropertyLocation.script
        "debugSymbolsDir" | PropertySetInvocation.providerSet | osPath("/custom/symbols")                                                                               | "File"                      | PropertyLocation.script
        "debugSymbolsDir" | PropertySetInvocation.providerSet | osPath("/custom/symbols")                                                                               | "Provider<Directory>"       | PropertyLocation.script
        "debugSymbolsDir" | PropertySetInvocation.setter      | osPath("/custom/symbols")                                                                               | "File"                      | PropertyLocation.script
        "debugSymbolsDir" | PropertySetInvocation.setter      | osPath("/custom/symbols")                                                                               | "Provider<Directory>"       | PropertyLocation.script

        "consoleSettings" | PropertySetInvocation.providerSet | TestValue.set("plain").expect("ConsoleSettings{prettyPrint=true, useUnicode=false, colorize=never}")    | "ConsoleSettings"           | PropertyLocation.script
        "consoleSettings" | PropertySetInvocation.providerSet | TestValue.set("rich").expect("ConsoleSettings{prettyPrint=true, useUnicode=true, colorize=always}")     | "Provider<ConsoleSettings>" | PropertyLocation.script
        "consoleSettings" | PropertySetInvocation.setter      | TestValue.set("verbose").expect("ConsoleSettings{prettyPrint=false, useUnicode=false, colorize=never}") | "ConsoleSettings"           | PropertyLocation.script
        "consoleSettings" | PropertySetInvocation.setter      | TestValue.set("auto").expect("ConsoleSettings{prettyPrint=true, useUnicode=true, colorize=auto}")       | "Provider<ConsoleSettings>" | PropertyLocation.script

        extensionName = "xcodebuild"
        setter = new PropertySetterWriter(extensionName, property)
            .serialize(wrapValueFallback)
            .set(rawValue, type)
            .to(location)
            .use(method)

        getter = new PropertyGetterTaskWriter(setter)
    }

    @Unroll("can configure console settings property #property with #method, value #rawValue and type #type")
    def "can configure console settings"() {
        expect:
        runPropertyQuery(getter, setter).matches(rawValue)

        where:
        property      | method                           | rawValue                           | type          | useConfigureBlock
        "prettyPrint" | PropertySetInvocation.setter     | true                               | "Boolean"     | false
        "prettyPrint" | PropertySetInvocation.setter     | false                              | "Boolean"     | false
        "useUnicode"  | PropertySetInvocation.setter     | true                               | "Boolean"     | false
        "useUnicode"  | PropertySetInvocation.setter     | false                              | "Boolean"     | false
        "colorize"    | PropertySetInvocation.setter     | ConsoleSettings.ColorOption.always | "ColorOption" | false
        "colorize"    | PropertySetInvocation.setter     | ConsoleSettings.ColorOption.never  | "ColorOption" | false
        "colorize"    | PropertySetInvocation.setter     | ConsoleSettings.ColorOption.auto   | "ColorOption" | false
        "colorize"    | PropertySetInvocation.assignment | "always"                           | "String"      | false
        "colorize"    | PropertySetInvocation.assignment | "never"                            | "String"      | false
        "colorize"    | PropertySetInvocation.assignment | "auto"                             | "String"      | false

        "prettyPrint" | PropertySetInvocation.setter     | true                               | "Boolean"     | true
        "prettyPrint" | PropertySetInvocation.setter     | false                              | "Boolean"     | true
        "useUnicode"  | PropertySetInvocation.setter     | true                               | "Boolean"     | true
        "useUnicode"  | PropertySetInvocation.setter     | false                              | "Boolean"     | true
        "colorize"    | PropertySetInvocation.setter     | ConsoleSettings.ColorOption.always | "ColorOption" | true
        "colorize"    | PropertySetInvocation.setter     | ConsoleSettings.ColorOption.never  | "ColorOption" | true
        "colorize"    | PropertySetInvocation.setter     | ConsoleSettings.ColorOption.auto   | "ColorOption" | true
        "colorize"    | PropertySetInvocation.assignment | "always"                           | "String"      | true
        "colorize"    | PropertySetInvocation.assignment | "never"                            | "String"      | true
        "colorize"    | PropertySetInvocation.assignment | "auto"                             | "String"      | true

        extensionName = "xcodebuild"
        setter = new PropertySetterWriter("${extensionName}.consoleSettings.get()", property)
            .serialize(wrapValueFallback)
            .set(rawValue, type)
            .use(method)

        getter = new PropertyGetterTaskWriter(setter, "")
    }

    @Unroll("property #property of type #tasktype.simpleName is bound to property #extensionProperty of extension #extensionName")
    def "task property is connected with extension"() {
        expect:
        addTask(taskName, tasktype.name, true)
        runPropertyQuery(getter, setter).matches(value)

        where:
        property          | extensionProperty | taskName         | tasktype            | value                                                                                               | type   | useProviderApi
        "logFile"         | "logsDir"         | "xcodeBuildTask" | XcodeArchive        | TestValue.set("build/custom/logs").expectProjectFile("build/custom/logs/" + taskName + ".log")      | "File" | true
        "logFile"         | "logsDir"         | "xcodeBuildTask" | ExportArchive       | TestValue.set("build/custom/logs").expectProjectFile("build/custom/logs/" + taskName + ".log")      | "File" | true
        "derivedDataPath" | "derivedDataPath" | "xcodeBuildTask" | XcodeArchive        | TestValue.set("build/custom/derivedData").expectProjectFile("build/custom/derivedData/" + taskName) | "File" | true
        "destinationDir"  | "xarchivesDir"    | "xcodeBuildTask" | XcodeArchive        | TestValue.set("build/custom/archives").expectProjectFile("build/custom/archives")                   | "File" | true
        "destinationDir"  | "xarchivesDir"    | "xcodeBuildTask" | ExportArchive       | TestValue.set("build/custom/archives").expectProjectFile("build/custom/archives")                   | "File" | true
        "destinationDir"  | "debugSymbolsDir" | "xcodeBuildTask" | ArchiveDebugSymbols | TestValue.set("build/custom/symbols").expectProjectFile("build/custom/symbols")                     | "File" | false

        extensionName = "xcodebuild"
        setter = new PropertySetterWriter(extensionName, extensionProperty)
            .set(value, type)
        getter = new PropertyGetterTaskWriter("${taskName}.${property}", useProviderApi ? ".getOrNull()" : "")
    }

    @Unroll("gradle console #console sets default value for consoleSettings.#consoleSettingProperty: #expectedValue")
    def "consoleSettings fallback to gradle console settings"() {
        expect:
        runPropertyQuery(getter, setter).matches(expectedValue)

        where:
        console   | consoleSettingProperty | expectedValue
        "rich"    | "prettyPrint"          | true
        "rich"    | "useUnicode"           | true
        "rich"    | "colorize"             | ConsoleSettings.ColorOption.always

        "plain"   | "prettyPrint"          | true
        "plain"   | "useUnicode"           | false
        "plain"   | "colorize"             | ConsoleSettings.ColorOption.never

        "verbose" | "prettyPrint"          | false
        "verbose" | "useUnicode"           | false
        "verbose" | "colorize"             | ConsoleSettings.ColorOption.never

        "auto"    | "prettyPrint"          | true
        "auto"    | "useUnicode"           | true
        "auto"    | "colorize"             | ConsoleSettings.ColorOption.auto

        extensionName = "xcodebuild"
        setter = new PropertySetterWriter("org.gradle", "console")
            .set(console, String)
            .to(PropertyLocation.property)
        getter = new PropertyGetterTaskWriter("${extensionName}.consoleSettings.get().${consoleSettingProperty}", "")


    }
}
