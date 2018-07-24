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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection

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

    void setTasks(List<String> tasks) {
        this.tasks.clear()
        this.tasks.addAll(tasks)
    }

    void setTasks(Iterable<String> tasks) {
        setTasks(tasks.toList())
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

    @TaskAction
    protected exec() {
        ProjectConnection connection = GradleConnector.newConnector()
                .forProjectDirectory(getDir())
                .connect()

        try {
            connection.newBuild()
                    .forTasks(*getTasks().toArray(new String[0]))
                    .withArguments(getBuildArguments())
                    .setColorOutput(false)
                    .setStandardOutput(System.out)
                    .setStandardError(System.out)
                    .run()
        } finally {
            connection.close()
        }
    }
}
