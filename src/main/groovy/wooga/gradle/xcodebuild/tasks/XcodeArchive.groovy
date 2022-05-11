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

package wooga.gradle.xcodebuild.tasks

import org.gradle.api.Transformer
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import wooga.gradle.xcodebuild.XcodeArchiveActionSpec
import wooga.gradle.xcodebuild.XcodeBuildAction
import wooga.gradle.xcodebuild.XcodeBuildPluginConventions
import wooga.gradle.xcodebuild.config.BuildSettings

class XcodeArchive extends AbstractXcodeArchiveTask implements XcodeArchiveActionSpec {

    @InputFiles
    protected getInputFiles() {
        project.files(projectPath)
    }

    @OutputDirectory
    final Provider<Directory> xcArchivePath


    XcodeArchive() {
        super()

        xcArchivePath = destinationDir.map(new Transformer<Directory, Directory>() {
            @Override
            Directory transform(Directory directory) {
                directory.dir(archiveName.get())
            }
        })

        setInternalArguments(project.provider({
            List<String> arguments = new ArrayList<String>()
            BuildSettings settings = buildSettings.getOrElse(BuildSettings.EMPTY).clone()
            arguments << "xcodebuild"

            if (clean.present && clean.get()) {
                arguments << XcodeBuildAction.clean.toString()
            }

            arguments << XcodeBuildAction.archive.toString()

            def projectOrWorkspace = projectPath.get().asFile

            if (projectOrWorkspace.name.endsWith(".xcworkspace")) {
                arguments << "-workspace" << projectOrWorkspace.getPath()
            } else if (projectOrWorkspace.name.endsWith(".xcodeproj")) {
                arguments << "-project" << projectOrWorkspace.getPath()
            } else {
                throw new ScriptException(XcodeBuildPluginConventions.INVALID_XCODE_PROJECT_ERROR_MESSAGE)
            }

            arguments << "-scheme" << scheme.get()

            if (configuration.present) {
                arguments << "-configuration" << configuration.get()
            }

            if (buildKeychain.present) {
                settings.otherCodeSignFlags("--keychain", buildKeychain.get().asFile.path)
            }

            if (teamId.present) {
                settings.developmentTeam(teamId.get())
            }

            arguments << "-archivePath" << xcArchivePath.get().asFile.path

            if (derivedDataPath.present) {
                arguments << "-derivedDataPath" << derivedDataPath.get().asFile.path
            }

            if (additionalBuildArguments.present) {
                additionalBuildArguments.get().each {
                    arguments << it
                }
            }

            arguments.addAll(settings.toList())
            arguments
        }))
    }
}
