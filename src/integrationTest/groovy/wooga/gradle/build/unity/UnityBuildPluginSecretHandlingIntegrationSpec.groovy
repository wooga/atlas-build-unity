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

package wooga.gradle.build.unity

import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.build.UnityIntegrationSpec
import wooga.gradle.secrets.Secret
import wooga.gradle.secrets.SecretResolver
import wooga.gradle.secrets.internal.SecretFile
import wooga.gradle.secrets.internal.SecretText

import static com.wooga.gradle.PlatformUtils.escapedPath
import static wooga.gradle.build.unity.TestUnityAsset.unityAsset

class UnityBuildPluginSecretHandlingIntegrationSpec extends UnityIntegrationSpec {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

    @Shared
    File appConfigsDir

    @Shared
    File externalDir

    @Shared
    File externalGradle

    @Shared
    List<String> secretIds = [
            'net_wooga_secretText_test1',
            'net_wooga_secretText_test2',
            'net_wooga_secretText_test3',
            'net_wooga_secretText_test4',
            'net_wooga_secretFile_test1',
            'net_wooga_secretFile_test2',
    ]

    def setup() {
        //create the default location for app configs
        def assets = new File(projectDir, "Assets")
        appConfigsDir = new File(assets, "UnifiedBuildSystem-Assets/AppConfigs")
        appConfigsDir.mkdirs()

        ['ios_ci', 'android_ci', 'webGL_ci'].collect { createFile("${it}.asset", appConfigsDir) }.each {
            def buildTarget = it.name.split(/_/, 2).first().toLowerCase()
            unityAsset(['MonoBehaviour': ['bundleId': 'net.wooga.test', 'batchModeBuildTarget': buildTarget, secretIds: secretIds]]).write(it)
        }

        unityAsset(['MonoBehaviour': ['bundleId': 'net.wooga.test']]).write(createFile("custom.asset", appConfigsDir))

        buildFile << """
            import ${SecretResolver.name}
            import ${Secret.name}
            import ${SecretText.name}
            import ${SecretFile.name}

            class CustomResolver implements SecretResolver {
                Secret<?> resolve(String secretId) {
                    if(secretId.startsWith("net_wooga_secretFile")) {
                        return new SecretFile(secretId.toUpperCase().bytes)
                    } else {
                        return new SecretText(secretId.toUpperCase())
                    }
                }
            }

            secrets.secretResolver = new CustomResolver()
        """.stripIndent()

        //to make sure our fake unity export works we need to mock a fake exported project

        externalDir = new File(projectDir, "extern/project")
        externalDir.mkdirs()

        externalGradle = createFile("build.gradle", externalDir)
        externalGradle << """
            plugins {
                id 'base'
            }

            task(printEnv) {
                doLast {
                    def outputFile = new File(project.buildDir, 'env.txt')
                    outputFile.parentFile.mkdirs()
                    outputFile.text = System.getenv().collect({key,value -> key + "=" + value}).join("\\n")
                }
            }

            assemble.dependsOn printEnv

        """.stripIndent()
        def externalSettings = createFile("settings.gradle", externalDir)
        externalSettings << """
            rootProject.name = 'test'
        """.stripIndent()

        buildFile << """
            import wooga.gradle.build.unity.tasks.UnityBuildPlayerTask
            project.tasks.withType(UnityBuildPlayerTask) { task ->
                task.doLast {
                    project.copy {
                        from(project.file('${escapedPath(externalDir.path)}'))
                        into(task.outputDirectory)
                    }
                }
            }
        """.stripIndent()
    }

    def "task :taskToRun executes matching #expectedFetchSecretTask task"() {
        when:
        def result = runTasksSuccessfully(taskToRun)

        then:
        result.wasExecuted(expectedFetchSecretTask)

        where:
        taskToRun           | expectedFetchSecretTask
        "assembleAndroidCi" | "fetchSecretsAndroidCi"
    }

    def "task #taskToRun sends secret texts as environment variables"() {
        given: "future printEnvOutput"
        def envOutput = new File(projectDir, "build/export/android_ci/project/build/env.txt")
        assert !envOutput.exists()

        when:
        def result = runTasksSuccessfully(taskToRun)

        then:
        secretIds.every { secretId ->
            def match = result.standardOutput =~ /.*${secretId.toUpperCase()}=(.*)?.*/
            if (match) {
                String value = match.group(1)
                if (secretId.startsWith("net_wooga_secretFile")) {
                    return true
                }
                return value == secretId.toUpperCase()
            }
            false
        }

        envOutput.exists()
        def envOutputText = envOutput.text
        secretIds.every { secretId ->
            def match = envOutputText =~ /.*${secretId.toUpperCase()}=(.*)?.*/
            if (match) {
                String value = match.group(1)
                if (secretId.startsWith("net_wooga_secretFile")) {
                    return true
                }
                return value == secretId.toUpperCase()
            }
            false
        }

        where:
        taskToRun           | _
        "assembleAndroidCi" | _

    }

    @Unroll
    def "task #taskToRun deletes secret files after invocation when #message"() {
        given:
        if (!taskSucceeds) {
            buildFile << """
                unity.unityPath.set(file("${escapedPath(unityFailTestLocation.path)}"))
            """.stripIndent()
        }
        when:
        def result = runTasks(taskToRun)

        then:
        result.success == taskSucceeds

        secretIds.every { secretId ->
            def match = result.standardOutput =~ /.*${secretId.toUpperCase()}=(.*)?.*/
            if (match) {
                String value = match.group(1)
                if (secretId.startsWith("net_wooga_secretFile")) {
                    File secretFile = new File(value)
                    return !secretFile.exists()
                }
                return true
            }
            false
        }

        where:
        taskToRun           | taskSucceeds
        "assembleAndroidCi" | true
        "assembleAndroidCi" | false
        message = taskSucceeds ? "task succeeds" : "task fails"
    }
}
