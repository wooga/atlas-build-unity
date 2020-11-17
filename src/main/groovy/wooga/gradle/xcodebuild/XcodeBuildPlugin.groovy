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
 *
 *
 *
 */

package wooga.gradle.xcodebuild

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.configuration.ConsoleOutput
import org.gradle.api.provider.Provider
import wooga.gradle.xcodebuild.internal.DefaultXcodeBuildPluginExtension
import wooga.gradle.xcodebuild.internal.PropertyLookup
import wooga.gradle.xcodebuild.tasks.AbstractXcodeTask
import wooga.gradle.xcodebuild.tasks.XcodeArchive

import static wooga.gradle.xcodebuild.XcodeBuildPluginConsts.*

class XcodeBuildPlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = "xcodebuild"
    private Project project

    @Override
    void apply(Project project) {
        this.project = project
        def extension = project.extensions.create(XcodeBuildPluginExtension, EXTENSION_NAME, DefaultXcodeBuildPluginExtension, project)

        extension.logsDir.set(project.layout.buildDirectory.dir(lookupValueInEnvAndPropertiesProvider(LOGS_DIR_LOOKUP)))
        extension.derivedDataPath.set(project.layout.buildDirectory.dir(lookupValueInEnvAndPropertiesProvider(DERIVED_DATA_PATH_LOOKUP)))
        extension.xarchivesDir.set(project.layout.buildDirectory.dir(lookupValueInEnvAndPropertiesProvider(XARCHIVES_DIR_LOOKUP)))
        extension.consoleSettings.set(ConsoleSettings.fromGradleOutput(project.gradle.startParameter.consoleOutput))

        project.tasks.withType(XcodeArchive.class, new Action<XcodeArchive>() {
            @Override
            void execute(XcodeArchive task) {
                task.version.set(project.provider({ project.version.toString() }))
                task.baseName.set(project.name)
                task.extension.set("xcarchive")
                task.destinationDir.set(extension.xarchivesDir)
                task.derivedDataPath.set(extension.derivedDataPath.dir(task.name))
            }
        })

        project.tasks.withType(AbstractXcodeTask.class, new Action<AbstractXcodeTask>() {
            @Override
            void execute(AbstractXcodeTask task) {
                task.logFile.set(extension.logsDir.file("${task.name}.log"))
                task.consoleSettings.set(extension.consoleSettings)
            }
        })
    }

    private Provider<String> lookupValueInEnvAndPropertiesProvider(PropertyLookup lookup) {
        lookupValueInEnvAndPropertiesProvider(lookup.env, lookup.property, lookup.defaultValue)
    }

    private Provider<String> lookupValueInEnvAndPropertiesProvider(String env, String property, String defaultValue = null) {
        project.provider({
            lookupValueInEnvAndProperties(env, property, defaultValue)
        })
    }

    protected String lookupValueInEnvAndProperties(String env, String property, String defaultValue = null) {
        System.getenv().get(env) ?:
                project.properties.getOrDefault(property, defaultValue)
    }
}
