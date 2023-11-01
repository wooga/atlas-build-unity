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

package wooga.gradle.build.unity.tasks

import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.build.IntegrationSpec
import wooga.gradle.build.unity.UnityBuildPlugin

import static com.wooga.gradle.PlatformUtils.escapedPath

class GradleBuildIntegrationSpec extends IntegrationSpec {

    @Shared
    File externalDir

    @Shared
    File externalGradle

    def setup() {
        buildFile << """
            ${applyPlugin(UnityBuildPlugin)}
        """.stripIndent()

        externalDir = new File(projectDir, "extern/project")
        externalDir.mkdirs()

        externalGradle = createFile("build.gradle", externalDir)
        externalGradle << """
            plugins {
                id 'base'
            }
            
            task(foo) {
                doLast {
                    println('foo executed')
                }
            }
            
            task(bar) {
                doLast {
                    println('bar executed')
                }
            }

            task(writeOutput) {
                doLast {
                    def outputFile = new File(project.buildDir, 'output.txt')
                    outputFile.parentFile.mkdirs()
                    outputFile.text = "CustomOutput"
                }
            }

            task(printEnv) {
                doLast {
                    def outputFile = new File(project.buildDir, 'env.txt')
                    outputFile.parentFile.mkdirs()
                    outputFile.text = System.getenv().collect({key,value -> key + "=" + value}).join("\\n")
                }
            }
        """.stripIndent()
        def externalSettings = createFile("settings.gradle", externalDir)
        externalSettings << """
            rootProject.name = 'test'
        """.stripIndent()
    }

    def "can execute external gradle build"() {
        given: "build script with external execution task"
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.GradleBuild) {
                dir = file("${escapedPath(externalDir.path)}")
                tasks = ['foo']
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully('externalGradle')

        then:
        result.standardOutput.contains("foo executed")
    }

    @Unroll
    def "can execute external gradle build with custom gradle version #gradleVersion"() {
        given: "build script with external execution task"
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.GradleBuild) {
                dir = file("${escapedPath(externalDir.path)}")
                tasks = ['foo']
                gradleVersion = "${gradleVersion}"
            }
        """.stripIndent()

        when:
        def result = runTasks('externalGradle')

        then:
        result.standardOutput.contains("foo executed")
        result.standardOutput.contains(new File("/daemon/${gradleVersion}").path)
        where:
        gradleVersion <<
            [
                "4.10",
                "5.0"
            ]
    }

    @Unroll("can execute external gradle build with #message1 #message2")
    def "can execute external gradle build with custom environment"() {
        given: "build script with external execution task"
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.GradleBuild) {
                dir = file("${escapedPath(externalDir.path)}")
                tasks = ['printEnv']
            }
        """.stripIndent()

        and: "custom environment variable"

        buildFile << """
            externalGradle.${method}(${setValue})
        """.stripIndent()

        and: "future output files"
        def envOutput = new File(externalDir, "build/env.txt")

        and: "a value in the parent environment"
        environmentVariables.set("controlKey", "controlValue")

        when:
        runTasksSuccessfully('externalGradle')

        then:
        envOutput.exists()
        envOutput.text.contains("${customEnvironmentKey}=${customEnvironmentValue}")
        envOutput.text.contains("controlKey=controlValue") == containsParentEnvironment

        where:
        customEnvironmentKey | customEnvironmentValue | useMapAPI | useSetter | containsParentEnvironment
        "foo"                | "bar"                  | false     | false     | true
        "bar"                | "foo"                  | true      | false     | true
        "bar"                | "baz"                  | true      | true      | false

        setValue = useMapAPI ? "['${customEnvironmentKey}':'${customEnvironmentValue}']" : "'${customEnvironmentKey}','${customEnvironmentValue}'"
        method = useSetter ? "setEnvironment" : "environment"
        message1 = useSetter ? "custom environment variables" : "additional environment variables"
        message2 = useMapAPI ? "provided as map" : "as key value pair"
    }

    def "can execute multiple tasks in external gradle build"() {
        given: "build script with exernal execution task"
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.GradleBuild) {
                dir = file("${escapedPath(externalDir.path)}")
                tasks = ['foo', 'bar']
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully('externalGradle')

        then:
        result.standardOutput.contains("foo executed")
        result.standardOutput.contains("bar executed")
    }

    def "task fails when dir is not a valid gradle project"() {
        given: "build script with exernal execution task"
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.GradleBuild) {
                dir = file("${escapedPath(File.createTempDir().path)}")
                tasks = ['foo', 'bar']
            }
        """.stripIndent()

        when:
        def result = runTasksWithFailure('externalGradle')

        then:
        outputContains(result,
                "A Gradle build should contain a 'settings.gradle' or 'settings.gradle.kts' file in its root directory. It may also contain a 'build.gradle' or 'build.gradle.kts' file."
        )
    }

    def "task fails when task is not part of external build"() {
        given: "build script with exernal execution task"
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.GradleBuild) {
                dir = file("${escapedPath(externalDir.path)}")
                tasks = ['baz']
            }
        """.stripIndent()

        when:
        def result = runTasksWithFailure('externalGradle')

        then:
        outputContains(result, "Task 'baz' not found")
    }

    @Unroll
    def "passes current loglevel #level down to external build"() {
        given: "build script with exernal execution task"
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.GradleBuild) {
                dir = file("${escapedPath(externalDir.path)}")
                tasks = ['foo']
            }
        """.stripIndent()

        and: "print custom log messages"
        externalGradle << """
            foo {
                doLast {
                    logger.debug('foo debug message')
                    logger.error('foo error message')
                    logger.info('foo info message')
                    logger.lifecycle('foo lifecycle message')
                    logger.quiet('foo quiet message')
                    logger.warn('foo warn message')
                }
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully('externalGradle', "--$level")

        then:
        contains.every { logLevel ->
            (result.standardOutput + result.standardError).contains("foo $logLevel message")
        }
        containsNot.every { logLevel ->
            !(result.standardOutput + result.standardError).contains("foo $logLevel message")
        }

        where:
        level   | contains                                                 | containsNot
        'debug' | ['debug', 'error', 'info', 'lifecycle', 'quiet', 'warn'] | []
        'info'  | ['error', 'info', 'lifecycle', 'quiet', 'warn']          | ['debug']
        'quiet' | ['error', 'quiet']                                       | ['debug', 'info', 'lifecycle', 'warn']
    }

    @Unroll("#willPassMessage continueOnFailure flag on external build when #whenFinal")
    def "passes continueOnFailure flag down to external build"() {
        given: "build script with exernal execution task"
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.GradleBuild) {
                dir = file("${escapedPath(externalDir.path)}")
                tasks = ['failTask', 'successTask']
            }
        """.stripIndent()

        if(continueFlagInBuildArguments) {
            buildFile << """
                externalGradle.buildArguments.add('--continue')
            """.stripIndent()
        }

        if (continueOnFailureFlagSet) {
            buildFile << """
                externalGradle.setContinueOnFailure(${continueOnFailureFlagValue})
            """.stripIndent()
        }

        and: "print custom log messages"
        externalGradle << """
            task failTask {
                doLast {
                    println "Running failTask"
                    throw new TaskExecutionException(
                    this, 
                    new Exception('Fail task on purpose'))
                }
            }

            task successTask {
                doLast {
                    println "Running successTask"
                }
            }
        """.stripIndent()
        def result
        when:
        if (continueFlagInStartParameter) {
            result = runTasks('externalGradle', '--continue')
        } else {
            result = runTasks('externalGradle')
        }

        then:
        outputContains(result, "Running failTask")
        outputContains(result, "Running successTask") == passContinueOnFailureFlagToExternalGradle
        !result.success

        where:
        continueFlagInBuildArguments | continueOnFailureFlagSet | continueOnFailureFlagValue | continueFlagInStartParameter
        true                         | true                     | true                       | true
        true                         | true                     | false                      | true
        true                         | false                    | _                          | true
        true                         | false                    | _                          | false
        true                         | true                     | true                       | false
        true                         | true                     | false                      | false
        false                        | true                     | true                       | true
        false                        | true                     | false                      | true
        false                        | false                    | _                          | true
        false                        | false                    | _                          | false
        false                        | true                     | true                       | false
        false                        | true                     | false                      | false

        passContinueOnFailureFlagToExternalGradle = continueFlagInBuildArguments ? true : continueOnFailureFlagSet ? continueOnFailureFlagValue : continueFlagInStartParameter
        willPassMessage = (passContinueOnFailureFlagToExternalGradle) ? "sets" : "will not set"
        whenArguments = continueFlagInBuildArguments ? "'--continue' flag is set in build arguments" : ""
        whenFlagSet = continueOnFailureFlagSet ? "'continueOnFailure' property is set with value '${continueOnFailureFlagValue}'" : ""
        whenStartFlag = continueFlagInStartParameter ? "'--continue' flag is present in gradle start parameters" : ""
        when = [whenArguments,(!whenFlagSet.isEmpty()) ? "and " + whenFlagSet : "", (!whenStartFlag.isEmpty()) ? "and " + whenStartFlag : ""].join(" ")
        whenFinal = when.trim().isEmpty() ? "nothing is set" : when
    }

    @Unroll
    def "skips passing down loglevel when buildArguments contains --#level2"() {
        given: "build script with exernal execution task"
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.GradleBuild) {
                dir = file("${escapedPath(externalDir.path)}")
                tasks = ['foo']
                buildArguments = ['--$level2']
            }
        """.stripIndent()

        and: "print custom log messages"
        externalGradle << """
            foo {
                doLast {
                    logger.debug('foo debug message')
                    logger.error('foo error message')
                    logger.info('foo info message')
                    logger.lifecycle('foo lifecycle message')
                    logger.quiet('foo quiet message')
                    logger.warn('foo warn message')
                }
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully('externalGradle', "--$level")

        then:
        contains.every { logLevel ->
            (result.standardOutput + result.standardError).contains("foo $logLevel message")
        }
        containsNot.every { logLevel ->
            !(result.standardOutput + result.standardError).contains("foo $logLevel message")
        }


        where:
        level   | level2  | contains                                                 | containsNot
        'debug' | 'info'  | ['error', 'info', 'lifecycle', 'quiet', 'warn']          | ['debug']
        'info'  | 'quiet' | ['error', 'quiet']                                       | ['debug', 'info', 'lifecycle', 'warn']
        'quiet' | 'debug' | ['debug', 'error', 'info', 'lifecycle', 'quiet', 'warn'] | []
    }

    @Unroll
    def "can set custom buildArguments with #method(#type)"() {
        given: "build script with exernal execution task"
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.GradleBuild) {
                dir = file("${escapedPath(externalDir.path)}")
                $method = $value 
                tasks = ['foo']
            }
        """.stripIndent()

        and: "print custom log messages"
        externalGradle << """
            foo {
                doLast {
                    println("test-param-one:" + project['testParam1'])
                    println("test-param-two:" + project['testParam2'])
                }
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully('externalGradle', '--quiet')

        then:
        result.standardOutput.contains("test-param-one:foo")
        result.standardOutput.contains("test-param-two:bar")


        where:
        type               | value
        'List<String>'     | "['-PtestParam1=foo', '-PtestParam2=bar']"
        'Iterable<String>' | "new HashSet(['-PtestParam1=foo', '-PtestParam2=bar'])"

        method = "buildArguments"
    }

    @Unroll
    def "can set tasks with #method(#type)"() {
        given: "build script with exernal execution task"
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.GradleBuild) {
                dir = file("${escapedPath(externalDir.path)}")
                $method = $value
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully('externalGradle')

        then:
        result.standardOutput.contains("foo executed")
        result.standardOutput.contains("bar executed")

        where:
        type               | value
        'List<String>'     | "['foo', 'bar']"
        'List<String>'     | "['foo', 'bar']"
        'Iterable<String>' | "new HashSet(['foo', 'bar'])"
        'Iterable<String>' | "new HashSet(['foo', 'bar'])"

        method = "tasks"
    }

    def "can set custom buildDirBase for external project"() {
        given: "build script with external execution task"
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.GradleBuild) {
                dir = file("${escapedPath(externalDir.path)}")
                tasks = ['writeOutput']
            }
        """.stripIndent()

        and: "custom build base dir"
        def customBuildBase = new File(projectDir,"customBuildCache")

        and: "potential future output files"
        def outputInternal = new File(externalDir, "build/output.txt")
        def outputExternal = new File(customBuildBase, "build/output.txt")

        assert !outputInternal.exists()
        assert !outputExternal.exists()

        when: "run task without changing build base"
        runTasksSuccessfully('externalGradle')

        then:
        outputInternal.exists()
        !outputExternal.exists()

        when: "new build base set to the external task"
        buildFile << """
            externalGradle.buildDirBase = new File('${escapedPath(customBuildBase.path)}')
        """.stripIndent()

        outputExternal.delete()
        outputInternal.delete()

        runTasksSuccessfully('externalGradle')

        then:
        !outputInternal.exists()
        outputExternal.exists()
    }

    def "can set relative path to custom buildDirBase for external project"() {
        given: "build script with external execution task"
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.GradleBuild) {
                dir = file("${escapedPath(externalDir.path)}")
                tasks = ['writeOutput']
            }
        """.stripIndent()

        and: "custom build base dir"
        def customBuildBase = new File(externalDir,"../customBuildCache")

        def outputInternal = new File(externalDir, "build/output.txt")
        def outputExternal = new File(customBuildBase, "build/output.txt")

        assert !outputInternal.exists()
        assert !outputExternal.exists()

        when: "new build base set to the external task"
        buildFile << """
            externalGradle.buildDirBase = new File('../customBuildCache')
        """.stripIndent()

        outputExternal.delete()
        outputInternal.delete()

        runTasksSuccessfully('externalGradle')

        then:
        !outputInternal.exists()
        outputExternal.exists()
    }

    @Unroll
    def "can clean project buildDir for external project before build when buildDirBase #message"() {
        given: "build script with external execution task"
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.GradleBuild) {
                dir = file("${escapedPath(externalDir.path)}")
                tasks = ['writeOutput']
            }
        """.stripIndent()

        and: "custom build base dir"
        def buildBaseDir = (useCustomBuildBase) ? new File(projectDir,"customBuildCache") : externalDir

        and: "potential future output files"
        def futureOutput = new File(buildBaseDir, "build/output.txt")
        def fileAlreadyInOutput = new File(buildBaseDir, "build/some_file.txt")
        fileAlreadyInOutput.parentFile.mkdirs()
        fileAlreadyInOutput.text = "Some content"

        assert !futureOutput.exists()
        assert fileAlreadyInOutput.exists()

        and: "custom build base dir"
        if(useCustomBuildBase) {
            buildFile << """
                externalGradle.buildDirBase = new File('${escapedPath(buildBaseDir.path)}')
            """.stripIndent()
        }

        when: "run task without cleaning before build"
        runTasksSuccessfully('externalGradle')

        then:
        futureOutput.exists()
        fileAlreadyInOutput.exists()

        when: "new build base set to the external task"
        buildFile << """
            externalGradle.cleanBuildDirBeforeBuild = true
        """.stripIndent()

        runTasksSuccessfully('externalGradle')

        then:
        futureOutput.exists()
        !fileAlreadyInOutput.exists()

        where:
        useCustomBuildBase | _
        false | _
        true | _

        message = (useCustomBuildBase) ? "is set" : "is not set"
    }

    @Unroll
    def "can override init script with custom init script when buildDirBase #message and delete build before build #deleteBuildMessage"() {
        given: "build script with external execution task"
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.GradleBuild) {
                dir = file("${escapedPath(externalDir.path)}")
                tasks = ['foo']
            }
        """.stripIndent()

        and: "a custom init script"
        def initScript = createFile("customInit.gradle", projectDir)

        and: "a init script marker"

        def initMarker = "EXECUTE CUSTOM INIT"

        initScript << '''
        println "------------------------------------------------------------"
        println "                  EXECUTE CUSTOM INIT                       "
        println "------------------------------------------------------------"
        
        projectsLoaded {
            def buildDirBase = rootProject.properties.containsKey("export.buildDirBase")
            def cleanBuildDirBeforeBuild = rootProject.properties.containsKey("export.deleteBuildDirBeforeBuild")
            
            println "buildDirBase: ${buildDirBase}"
            println "cleanBuildDirBeforeBuild: ${cleanBuildDirBeforeBuild}"
        }
        
        '''.stripIndent().trim()

        def defaultMarker = "BUILD UNITY EXPORT INIT SCRIPT"

        and: "custom build base dir"
        if(useCustomBuildBase) {
            def customBuildBase = new File(projectDir,"customBuildCache")
            buildFile << """
                externalGradle.buildDirBase = new File('${escapedPath(customBuildBase.path)}')
            """.stripIndent()
        }

        if(cleanBuildDirBeforeBuild) {
            buildFile << """
                externalGradle.cleanBuildDirBeforeBuild = true
            """.stripIndent()
        }

        when:
        def result = runTasksSuccessfully('externalGradle')

        then:
        result.standardOutput.normalize().contains(defaultMarker.normalize()) == containsDefaultMarker
        !result.standardOutput.normalize().contains(initMarker.normalize())

        when: "the init script set for the task"
        buildFile << """
            externalGradle.initScript = new File('${escapedPath(initScript.path)}')
        """.stripIndent()
        result = runTasksSuccessfully('externalGradle')

        then:
        !result.standardOutput.normalize().contains(defaultMarker.normalize())
        result.standardOutput.normalize().contains(initMarker.normalize())
        result.standardOutput.contains("buildDirBase: ${useCustomBuildBase}")
        result.standardOutput.contains("cleanBuildDirBeforeBuild: ${cleanBuildDirBeforeBuild}")

        where:
        useCustomBuildBase | cleanBuildDirBeforeBuild | containsDefaultMarker
        false              | false                    | false
        true               | false                    | true
        false              | true                     | true
        true               | true                     | true

        message = (useCustomBuildBase) ? "is set" : "is not set"
        deleteBuildMessage = (cleanBuildDirBeforeBuild) ? "is set" : "is not set"
    }
}
