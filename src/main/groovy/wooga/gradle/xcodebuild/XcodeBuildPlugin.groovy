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
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import wooga.gradle.xcodebuild.tasks.XcodeArchive

class XcodeBuildPlugin implements Plugin<Project> {
    private static final Logger LOG = Logging.getLogger(XcodeBuildPlugin.class)

    @Override
    void apply(Project project) {
        //check if system is running mac os
        String osName = System.getProperty("os.name").toLowerCase()
        if (!osName.contains('mac os')) {
            LOG.warn("This plugin is only supported on Mac OS systems.")
            return
        }

        project.tasks.withType(XcodeArchive.class, new Action<XcodeArchive>() {
            @Override
            void execute(XcodeArchive task) {
                task.version.set(project.provider({ project.version.toString() }))
                task.baseName.set(project.name)
                task.extension.set("xcarchive")
                task.destinationDir.set(project.layout.buildDirectory.dir("archives"))

//                def conventionMapping = task.getConventionMapping()
//                conventionMapping.map("version", { PropertyUtils.convertToString(project.version) })
//                //conventionMapping.map("clean", { false })
//                conventionMapping.map("destinationDir", {
//                    project.file("${project.buildDir}/archives")
//                })
//                conventionMapping.map("baseName", { project.name })
//                conventionMapping.map("extension", { "xcarchive" })
//                //conventionMapping.map("scheme", { extension.getScheme() })
//                //conventionMapping.map("configuration", { extension.getConfiguration() })
//                //conventionMapping.map("teamId", { extension.getTeamId() })
            }
        })
    }
}
