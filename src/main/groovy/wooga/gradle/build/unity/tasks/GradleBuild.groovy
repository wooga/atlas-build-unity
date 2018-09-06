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

import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.gradle.util.CollectionUtils

class GradleBuild extends DefaultTask {

    private Object dir
    private final List<String> tasks = new ArrayList<String>()
    private final List<String> buildArguments = new ArrayList<String>()

    @Internal
    File getDir() {
        project.file(dir)
    }

    void setDir(Object dir) {
        this.dir = dir
    }

    GradleBuild dir(Object dir) {
        setDir(dir)
        this
    }

    @Input
    List<String> getTasks() {
        this.tasks
    }

    void setTasks(Iterable<String> tasks) {
        this.tasks.clear()
        this.tasks.addAll(tasks.toList())
    }

    GradleBuild tasks(Iterable<String> tasks) {
        setTasks(tasks)
        this
    }

    GradleBuild tasks(String... tasks) {
        List<String> taskList = CollectionUtils.flattenCollections(tasks) as List<String>
        setTasks(taskList)
        this
    }

    @Input
    List<String> getBuildArguments() {
        this.buildArguments
    }

    void setBuildArguments(List<String> arguments) {
        this.buildArguments.clear()
        this.buildArguments.addAll(arguments)
    }

    void setBuildArguments(Iterable<String> arguments) {
        setBuildArguments(arguments.toList())
    }

    GradleBuild buildArguments(List<String> arguments) {
        setBuildArguments(arguments)
        this
    }

    GradleBuild buildArguments(Iterable<String> arguments) {
        setBuildArguments(arguments)
        this
    }

    GradleBuild buildArguments(String... args) {
        List<String> arguments = CollectionUtils.flattenCollections(args)
        buildArguments(arguments)
    }

    @TaskAction
    protected exec() {
        def args = []
        args.addAll(getBuildArguments())

        if (!['--debug', '--info', '--quiet'].any { args.contains(it) }) {
            def startParameter = this.project.gradle.startParameter
            switch (startParameter.logLevel) {
                case LogLevel.DEBUG:
                    args << '--debug'
                    break
                case LogLevel.INFO:
                    args << '--info'
                    break
                case LogLevel.QUIET:
                    args << '--quiet'
                    break
            }
        }

        ProjectConnection connection = GradleConnector.newConnector()
                .forProjectDirectory(getDir())
                .connect()

        try {
            connection.newBuild()
                    .forTasks(*getTasks().toArray(new String[0]))
                    .withArguments(args)
                    .setColorOutput(false)
                    .setStandardOutput(System.out)
                    .setStandardError(System.err)
                    .run()
        } finally {
            connection.close()
        }
    }
}
