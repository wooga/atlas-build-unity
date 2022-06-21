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

package wooga.gradle.xcodebuild

import wooga.gradle.xcodebuild.config.BuildSettings

abstract class XcodeBuildIntegrationSpec extends IntegrationSpec {

    def setup() {
        buildFile << """
          group = 'test'
          ${applyPlugin(XcodeBuildPlugin)}
       """.stripIndent()
    }

    static wrapValueFallback = { Object rawValue, String type, Closure<String> fallback ->
        switch (type) {
            case ConsoleSettings.class.simpleName:
                return "${ConsoleSettings.class.name}.fromGradleOutput(org.gradle.api.logging.configuration.ConsoleOutput.${rawValue.toString().capitalize()})"
            case ConsoleSettings.ColorOption.simpleName:
                return ConsoleSettings.ColorOption.name + ".${rawValue.toString()}"
            case BuildSettings.class.simpleName:
                return "new ${BuildSettings.class.name}()" + rawValue.replaceAll(/(\[|\])/, '').split(',').collect({
                    List<String> parts = it.split("=")
                    ".put('${parts[0].trim()}', '${parts[1].trim()}')"
                }).join("")
            default:
                return rawValue.toString()
        }
    }


}
