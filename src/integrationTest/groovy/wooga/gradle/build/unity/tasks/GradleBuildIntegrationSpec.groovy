/*
 * Copyright 2017 the original author or authors.
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
    }

    def "can execute external gradle build"() {
        given: "build script with exernal execution task"
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.GradleBuild) {
                dir "${escapedPath(externalDir.path)}"
                tasks = ['foo']
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully('externalGradle')

        then:
        result.standardOutput.contains("foo executed")
    }

    def "can execute multiple tasks in external gradle build"() {
        given: "build script with exernal execution task"
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.GradleBuild) {
                dir "${escapedPath(externalDir.path)}"
                tasks = ['foo', 'bar']
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully('externalGradle')

        then:
        result.standardOutput.contains("foo executed")
        result.standardOutput.contains("bar executed")
    }

    @Unroll
    def "passes current loglevel #level down to external build"() {
        given: "build script with exernal execution task"
        buildFile << """
            task("externalGradle", type:wooga.gradle.build.unity.tasks.GradleBuild) {
                dir "${escapedPath(externalDir.path)}"
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
        contains.every { logLevel -> result.standardOutput.contains("foo $logLevel message") }
        containsNot.every { logLevel -> !result.standardOutput.contains("foo $logLevel message") }

        where:
        level   | contains                                                 | containsNot
        'debug' | ['debug', 'error', 'info', 'lifecycle', 'quiet', 'warn'] | []
        'info'  | ['error', 'info', 'lifecycle', 'quiet', 'warn']          | ['debug']
        'quiet' | ['error', 'quiet']                                       | ['debug', 'info', 'lifecycle', 'warn']
    }
}
