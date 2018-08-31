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

import org.apache.commons.io.FilenameUtils
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import wooga.gradle.build.unity.ios.internal.utils.PropertyUtils
import wooga.gradle.unity.batchMode.BatchModeFlags
import wooga.gradle.unity.batchMode.BuildTarget
import wooga.gradle.unity.tasks.internal.AbstractUnityProjectTask
import wooga.gradle.unity.utils.GenericUnityAsset

class UnityBuildPlayerTask extends AbstractUnityProjectTask {

    static String BUILD_TARGET_KEY = "buildTarget"

    UnityBuildPlayerTask() {
        super(UnityBuildPlayerTask.class)
        super.setBuildTarget(BuildTarget.undefined)
    }

    private GenericUnityAsset appConfig
    private FileCollection inputFiles
    private Object appConfigFile
    private Object exportMethodName
    private Object version
    private Object toolsVersion
    private Object outputDirectoryBase

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

    @InputFile
    File getAppConfigFile() {
        if(!appConfigFile) {
            return null
        }

        project.file(appConfigFile)
    }

    String getAppConfigName() {
        if(getAppConfigFile()) {
            return FilenameUtils.removeExtension(getAppConfigFile().name)
        }
        null
    }

    void setAppConfigFile(Object appConfigFile) {
        this.appConfigFile = appConfigFile
        this.appConfig = null
    }

    void appConfigFile(Object appConfigFile) {
        this.setAppConfigFile(appConfigFile)
    }

    @OutputDirectory
    File getOutputDirectory() {
        if(getOutputDirectoryBase() && getAppConfigName()) {
            return project.file("${getOutputDirectoryBase()}/${getAppConfigName()}/project")
        }

        null
    }

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

    @Internal("loaded app config asset")
    protected GenericUnityAsset getAppConfig() {
        if(!appConfig) {
            appConfig = new GenericUnityAsset(getAppConfigFile())
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

    @Input
    String getExportMethodName() {
        PropertyUtils.convertToString(exportMethodName)
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
        PropertyUtils.convertToString(toolsVersion)
    }

    void setToolsVersion(Object version) {
        toolsVersion = version
    }

    void toolsVersion(Object version) {
        setToolsVersion(version)
    }

    @Input
    String getVersion() {
        PropertyUtils.convertToString(version)
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
        String customArgs = "-CustomArgs:appConfig=${getAppConfigFile()};"
        customArgs += "outputPath=${out.getPath()};"
        customArgs += "version=${getVersion()};"

        if (getBuildPlatform()) {
            args BatchModeFlags.BUILD_TARGET, getBuildPlatform()
        }

        if (getToolsVersion()) {
            customArgs += "toolsVersion=${getToolsVersion()}"
        }

        args "-executeMethod", getExportMethodName()
        args customArgs

        super.exec()
    }

}
