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

import spock.lang.Shared
import spock.lang.Unroll
import wooga.gradle.build.IntegrationSpec
import wooga.gradle.build.unity.UnityBuildPlugin

import java.util.regex.Pattern

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
        """.stripIndent()
        def externalSettings = createFile("settings.gradle", externalDir)
        externalSettings << """
            rootProject.name = 'test'
        """.stripIndent()
    }

    def "can execute external gradle build"() {
        given: "build script with exernal execution task"
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
        result.standardOutput.contains(new File(".gradle/daemon/${gradleVersion}").path)
        where:
        gradleVersion << ["4.0", "4.10", "5.0"]
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
        outputContains(result, "Task 'foo' not found")
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
}
