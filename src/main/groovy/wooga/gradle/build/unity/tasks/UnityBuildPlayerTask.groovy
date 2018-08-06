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

import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTreeElement
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import wooga.gradle.unity.batchMode.BatchModeFlags
import wooga.gradle.unity.batchMode.BatchModeSpec
import wooga.gradle.unity.batchMode.BuildTarget
import wooga.gradle.unity.tasks.internal.AbstractUnityProjectTask

import java.util.concurrent.Callable

class UnityBuildPlayerTask extends AbstractUnityProjectTask {

    UnityBuildPlayerTask() {
        super(UnityBuildPlayerTask.class)
        super.setBuildTarget(BuildTarget.undefined)
    }

    private FileCollection inputFiles

    @SkipWhenEmpty
    @InputFiles
    FileCollection getInputFiles() {
        inputFiles
    }

    void setInputFiles(FileCollection files) {
        this.inputFiles = files
    }

    void inputFiles(FileCollection files) {
        setInputFiles(files)
    }

    @OutputDirectory
    File getOutputDirectory() {
        project.file("${getOutputDirectoryBase()}/${getBuildPlatform()}/${getBuildEnvironment()}/project")
    }

    private Object buildPlatform
    private Object buildEnvironment
    private Object exportMethodName
    private Object version
    private Object toolsVersion
    private Object outputDirectoryBase

    @Internal("Base path of outputFiles")
    File getOutputDirectoryBase() {
        if(outputDirectoryBase)
        {
            return project.file(outputDirectoryBase)
        }
        null
    }

    void setOutputDirectoryBase(Object outputDirectoryBase) {
        this.outputDirectoryBase = outputDirectoryBase
    }

    UnityBuildPlayerTask outputDirectoryBase(Object outputDirectoryBase) {
        setOutputDirectoryBase(outputDirectoryBase)
        this
    }

    @Input
    String getBuildPlatform() {
        def platform = convertToString(buildPlatform)
        platform
    }

    void setBuildPlatform(Object platform) {
        buildPlatform = platform
    }

    void buildPlatform(Object platform) {
        setBuildPlatform(platform)
    }

    @Input
    String getBuildEnvironment() {
        convertToString(buildEnvironment)
    }

    void setBuildEnvironment(Object environment) {
        buildEnvironment = environment
    }

    void buildEnvironment(Object environment) {
        setBuildEnvironment(environment)
    }

    @Input
    String getExportMethodName() {
        convertToString(exportMethodName)
    }

    void setExportMethodName(Object method) {
        exportMethodName = method
    }

    void exportMethodName(Object method) {
        setExportMethodName(method)
    }

    @Optional
    @Input
    String getToolsVersion() {
        convertToString(toolsVersion)
    }

    void setToolsVersion(Object version) {
        toolsVersion = version
    }

    void toolsVersion(Object version) {
        setToolsVersion(version)
    }

    @Input
    String getVersion() {
        convertToString(version)
    }

    void setVersion(Object value) {
        version = value
    }

    UnityBuildPlayerTask version(Object version) {
        setVersion(version)
        this
    }

    @Override
    protected void exec() {
        File out = getOutputDirectory()
        out.mkdirs()
        String customArgs = "-CustomArgs:platform=${getBuildPlatform()};"
        customArgs += "environment=${getBuildEnvironment()};"
        customArgs += "outputPath=${out.getPath()};"
        customArgs += "version=${getVersion()};"

        if (getToolsVersion()) {
            customArgs += "toolsVersion=${getToolsVersion()}"
        }

        args "-executeMethod", getExportMethodName()
        args customArgs

        if (getBuildTarget() == BuildTarget.undefined) {
            args BatchModeFlags.BUILD_TARGET, convertBuildPlatformToBuildTarget(getBuildPlatform())
        }
        super.exec()
    }

    BuildTarget convertBuildPlatformToBuildTarget(Object platform) {
        BuildTarget buildTarget
        try {
            buildTarget = convertToString(platform).toLowerCase() as BuildTarget
        }
        catch (IllegalArgumentException ignored) {
            logger.warn("build target ${platform} unknown")
            buildTarget = BuildTarget.undefined
        }

        buildTarget
    }

    //TODO: move duplicate code
    private static String convertToString(Object value) {
        if (!value) {
            return null
        }

        if (value instanceof Callable) {
            value = ((Callable) value).call()
        }

        value.toString()
    }
}
