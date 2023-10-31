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
 *
 */

package wooga.gradle.build

import org.apache.commons.lang3.StringUtils
import org.yaml.snakeyaml.Yaml
import wooga.gradle.build.unity.UBSVersion
import wooga.gradle.build.unity.UnityBuildPlugin

import static com.wooga.gradle.PlatformUtils.escapedPath

abstract class UnityIntegrationSpec extends IntegrationSpec {

    File unityTestLocation
    File unityFailTestLocation
    File unityMainDirectory

    File createFakeUnity(File unityTestLocation, exitCode = 0) {
        String osName = System.getProperty("os.name").toLowerCase()
        unityTestLocation.createNewFile()
        unityTestLocation.executable = true
        if (osName.contains("windows")) {
            unityTestLocation << """
                @echo off
                echo arguments
                echo %*
                echo environment
                set
                exit ${exitCode}
            """.stripIndent()
        }
        else
        {
            unityTestLocation << """
                #!/usr/bin/env bash
                echo arguments
                echo \$@
                echo environment
                env
                exit ${exitCode}
            """.stripIndent()
        }

        unityTestLocation
    }

    static wrapValueFallback = { Object rawValue, String type, Closure<String> fallback ->
        switch (type) {
            case UBSVersion.getSimpleName():
                return "${UBSVersion.canonicalName}.${rawValue.toString()}".toString()
            default:
                return rawValue.toString()
        }
    }

    File createConfig(String path) {
        def configsDir = new File(projectDir, path)
        configsDir.mkdirs()

        def config = ['MonoBehaviour': ['bundleId': 'net.wooga.test', 'batchModeBuildTarget': 'android']]
        ['custom', 'test'].collect { createFile("${it}.asset", configsDir) }.each {
            it << UNITY_ASSET_HEADER
            it << "\n"
            Yaml yaml = new Yaml()
            it << yaml.dump(config)
        }
        return new File(configsDir, "custom.asset")
    }

    String[] unityArgs(String base) {
        def tailString = substringAt(base, "arguments").replace("arguments", "")
        def endIndex = tailString.indexOf("environment")
        def argsString = tailString.substring(0, endIndex)
        def parts = argsString.split(" ").
                findAll {!StringUtils.isEmpty(it) }.collect{ it.trim() }
        return parts
    }

    String substringAt(String base, String expression) {
        def customArgsIndex = base.indexOf(expression)
        return base.substring(customArgsIndex)
    }

    boolean hasKeyValue(String key, String value, String[] customArgParts) {
        return customArgParts.any {
            def keyIndex = customArgParts.findIndexOf {
                it == key
            }
            return value == customArgParts[keyIndex+1]
        }
    }


    public static final String UNITY_ASSET_HEADER = """
            %YAML 1.1
            %TAG !u! tag:unity3d.com,2011:
            --- !u!114 &11400000
            """.stripIndent().trim()

    def setup() {
        String osName = System.getProperty("os.name").toLowerCase()
        unityMainDirectory = projectDir
        if (!osName.contains("windows")) {
            unityMainDirectory = new File(projectDir, "Unity/SomeLevel/SecondLevel")
            unityMainDirectory.mkdirs()
        }
        unityTestLocation = createFakeUnity(new File(unityMainDirectory,"fakeUnity.bat"))
        unityFailTestLocation = createFakeUnity(new File(unityMainDirectory,"fakeUnityFailing.bat"), 1)

        buildFile << """
            group = 'test'
            ${applyPlugin(UnityBuildPlugin)}
         
            unity.unityPath.set(file("${escapedPath(unityTestLocation.path)}"))
        """.stripIndent()

        //create Assets dir with some files
        def assets = new File(projectDir, "Assets")
        assets.mkdirs()
        createFile("Test.cs", assets)
    }

    Throwable rootCause(Throwable e) {
        if(e.cause == null) {
            return e
        }
        return rootCause(e.cause)
    }
}
