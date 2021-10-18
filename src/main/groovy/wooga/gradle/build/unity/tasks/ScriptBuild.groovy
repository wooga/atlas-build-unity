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


import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import wooga.gradle.build.unity.internal.GradleShell

abstract class ScriptBuild extends SecretsBuild {

    final RegularFileProperty script
    final ListProperty<String> arguments

    final Property<Boolean> logsShellOutput

    private final GradleShell shell

    ScriptBuild() {
        super()
        script = project.objects.fileProperty()
        arguments = project.objects.listProperty(String.class)
        logsShellOutput = project.objects.property(Boolean)
        shell = new GradleShell(project)

    }

    ExecResult execute(File executable, List<String> arguments, Map<String, ?> environment) {
        def logOutput = logsShellOutput.getOrElse(false)
        def workingDir = dir.get()
        def args = arguments.collect {it.split(" ")}.flatten()
        def result = shell.execute(logOutput) { ExecSpec spec ->
            spec.workingDir(workingDir)
            spec.args(args)
            spec.executable(executable.absolutePath)
            spec.environment(environment)
        }
        result.rethrowFailure()
        return result
    }

    @TaskAction
    protected exec() {
        environment(environmentSecrets.get())
        execute(script.get().asFile, arguments.get(), environment)
    }

    @InputFile
    RegularFileProperty getScript() {
        return script
    }

    @Optional
    @Input
    ListProperty<String> getArguments() {
        return arguments
    }

    @Optional
    @Input
    Property<Boolean> getLogsShellOutput() {
        return logsShellOutput
    }
}
