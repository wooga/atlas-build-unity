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

package wooga.gradle.build

import nebula.test.functional.ExecutionResult
import org.apache.commons.text.StringEscapeUtils

class IntegrationSpec extends nebula.test.IntegrationSpec{

    def escapedPath(String path) {
        String osName = System.getProperty("os.name").toLowerCase()
        if (osName.contains("windows")) {
            return StringEscapeUtils.escapeJava(path)
        }
        path
    }

    def setup() {
        def gradleVersion = System.getenv("GRADLE_VERSION")
        if (gradleVersion) {
            this.gradleVersion = gradleVersion
            fork = true
        }
    }

    Boolean outputContains(ExecutionResult result, String message) {
        result.standardOutput.contains(message) || result.standardError.contains(message)
    }

    def wrapValueBasedOnType(Object rawValue, String type) {
        def value
        switch (type) {
            case "Closure":
                value = "{'$rawValue'}"
                break
            case "Callable":
                value = "new java.util.concurrent.Callable<String>() {@Override String call() throws Exception { '$rawValue' }}"
                break
            case "Object":
                value = "new Object() {@Override String toString() { '$rawValue' }}"
                break
            case "String":
                value = "'$rawValue'"
                break
            case "File":
                value = "new File('$rawValue')"
                break
            case "List<String>":
                value = "['$rawValue']"
                break
            default:
                value = rawValue
        }
        value
    }
}
