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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.gradle.util.CollectionUtils

class GradleBuild extends DefaultTask {

    @Internal
    final DirectoryProperty dir = project.layout.directoryProperty()

    @Input
    final ListProperty<String> tasks = project.objects.listProperty(String.class)

    @Input
    final ListProperty<String> buildArguments = project.objects.listProperty(String.class)

    @Input
    final Property<String> gradleVersion = project.objects.property(String.class)

    @TaskAction
    protected exec() {
        def args = []
        args.addAll(buildArguments.get())

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
                .forProjectDirectory(dir.get().asFile)
                .useGradleVersion(gradleVersion.get())
                .connect()

        try {
            connection.newBuild()
                    .forTasks(*tasks.get().toArray(new String[0]))
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
