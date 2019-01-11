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

import org.apache.commons.io.FilenameUtils
import org.gradle.api.Transformer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import wooga.gradle.build.unity.ios.internal.utils.PropertyUtils
import wooga.gradle.unity.batchMode.BatchModeFlags
import wooga.gradle.unity.batchMode.BuildTarget
import wooga.gradle.unity.tasks.internal.AbstractUnityProjectTask
import wooga.gradle.unity.utils.GenericUnityAsset

import java.util.concurrent.Callable

class UnityBuildPlayerTask extends AbstractUnityProjectTask {

    static String BUILD_TARGET_KEY = "batchModeBuildTarget"
    private GenericUnityAsset appConfig

    @SkipWhenEmpty
    @InputFiles
    final ConfigurableFileCollection inputFiles

    @InputFile
    final RegularFileProperty appConfigFile

    @Internal("Base path of outputFiles")
    final DirectoryProperty outputDirectoryBase

    @OutputDirectory
    final Provider<Directory> outputDirectory

    @Internal
    final Provider<String> appConfigName

    @Input
    final Property<String> exportMethodName

    @Optional
    @Input
    final Property<String> toolsVersion

    @Input
    final Property<String> version

    @Internal("loaded app config asset")
    protected GenericUnityAsset getAppConfig() {
        if(!appConfig) {
            appConfig = new GenericUnityAsset(appConfigFile.get().asFile)
            if(!appConfig.isValid()) {
                throw new StopExecutionException('provided appConfig is invalid')
            }
        }

        appConfig
    }

    @Internal("read from appConfig file")
    String getBuildPlatform() {
        getAppConfig()[BUILD_TARGET_KEY]
    }

    UnityBuildPlayerTask() {
        super(UnityBuildPlayerTask.class)
        super.setBuildTarget(BuildTarget.undefined)

        inputFiles = project.layout.configurableFiles()
        appConfigFile = project.layout.fileProperty()
        appConfigName = appConfigFile.map { FilenameUtils.removeExtension(it.asFile.name) }

        outputDirectoryBase = project.layout.directoryProperty()

        def outputPath = appConfigName.map(new Transformer<String, String>() {
            @Override
            String transform(String configName) {
                configName + "/project"
            }
        })

        outputDirectory = project.layout.directoryProperty(outputDirectoryBase.dir(outputPath))

        exportMethodName = project.objects.property(String.class)
        toolsVersion = project.objects.property(String.class)
        version = project.objects.property(String.class)
    }

    @Override
    protected void exec() {
        File out = outputDirectory.get().asFile
        out.mkdirs()
        String customArgs = "-CustomArgs:appConfig=${appConfigFile.get().asFile.path};"
        customArgs += "outputPath=${out.path};"
        customArgs += "version=${version.get()};"

        if (getBuildPlatform()) {
            setBuildTarget(getBuildPlatform().toLowerCase() as BuildTarget)
        }

        if (toolsVersion.present) {
            customArgs += "toolsVersion=${toolsVersion.get()}"
        }

        args "-executeMethod", exportMethodName.get()
        args customArgs

        super.exec()
    }
}
