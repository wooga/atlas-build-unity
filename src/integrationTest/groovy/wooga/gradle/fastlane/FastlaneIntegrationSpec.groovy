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

package wooga.gradle.fastlane

import com.wooga.gradle.PlatformUtils

import java.nio.file.Paths

abstract class FastlaneIntegrationSpec extends IntegrationSpec {

    File fastlaneMock
    File fastlaneMockPath

    def setupFastlaneMock() {
        fastlaneMockPath = File.createTempDir("fastlane", "mock")

        def path = System.getenv("PATH")
        environmentVariables.clear("PATH")
        String newPath = "${fastlaneMockPath}${File.pathSeparator}${path}"
        environmentVariables.set("PATH", newPath)
        assert System.getenv("PATH") == newPath


        fastlaneMock = createFile("fastlane", fastlaneMockPath)
        fastlaneMock.executable = true
        fastlaneMock << """
            #!/usr/bin/env bash
            echo \$@
            env
        """.stripIndent()
    }

    def setup() {
        setupFastlaneMock()
        buildFile << """
              group = 'test'
              ${applyPlugin(FastlanePlugin)}
           """.stripIndent()
    }



    // TODO: Replace with newer test API
    // Should not use project dir if on not windows
    Object substitutePath(Object str, Object subStr, String typeName) {

        if (typeName != "File" && typeName != "Provider<RegularFile>") {
            return str
        }

        def path = (String) subStr
        if (path == null) {
            return str
        }

        // If it's an absolute path starting from the current volume
        if (Paths.get(path).isAbsolute()){
            return str
        }
//        if (!PlatformUtils.windows && path.startsWith("/")){
//            return str
//        }
//        else if (PlatformUtils.windows && path.startsWith("c:")) {
//            return str
//        }


        def modifiedPath = typeName == "Provider<RegularFile>"
            ? "/build${path}"
            : path

        str.replace(path, new File(projectDir, modifiedPath).path)
    }
}
