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

package wooga.gradle.fastlane.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import wooga.gradle.fastlane.FastlaneActionSpec
import wooga.gradle.fastlane.internal.FastlaneAction

abstract class AbstractFastlaneTask extends DefaultTask implements FastlaneActionSpec {

    @Internal
    final RegularFileProperty logFile

    @Override
    void setLogFile(File value) {
        logFile.set(value)
    }

    @Override
    void setLogFile(Provider<RegularFile> value) {
        logFile.set(value)
    }

    @Override
    AbstractFastlaneTask logFile(File value) {
        setLogFile(value)
        this
    }

    @Override
    AbstractFastlaneTask logFile(Provider<RegularFile> value) {
        setLogFile(value)
        this
    }

    final ListProperty<String> additionalArguments

    @Override
    void setAdditionalArguments(Iterable<String> value) {
        additionalArguments.set(value)
    }

    @Override
    void setAdditionalArguments(Provider<? extends Iterable<String>> value) {
        additionalArguments.set(value)
    }

    @Override
    AbstractFastlaneTask argument(String argument) {
        additionalArguments.add(argument)
        return this
    }

    @Override
    AbstractFastlaneTask arguments(String[] arguments) {
        additionalArguments.addAll(project.provider({ arguments.toList() }))
        return this
    }

    @Override
    AbstractFastlaneTask arguments(Iterable arguments) {
        additionalArguments.addAll(project.provider({ arguments }))
        return this
    }

    AbstractFastlaneTask() {
        additionalArguments = project.objects.listProperty(String)
        logFile = project.layout.fileProperty()
    }

    @TaskAction
    protected void exec() {
        def action = new FastlaneAction(project, arguments.get(), environment.get(), logFile.getAsFile().getOrNull())
        action.exec()
    }
}