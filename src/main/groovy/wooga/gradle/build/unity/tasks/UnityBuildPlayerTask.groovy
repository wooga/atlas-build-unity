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
import wooga.gradle.unity.batchMode.BatchModeFlags
import wooga.gradle.unity.batchMode.BuildTarget
import wooga.gradle.unity.tasks.internal.AbstractUnityProjectTask

import java.util.concurrent.Callable

class UnityBuildPlayerTask extends AbstractUnityProjectTask {

    UnityBuildPlayerTask() {
        super(UnityBuildPlayerTask.class)
    }

    @InputFiles
    FileCollection getInputFiles() {
        def base = new File(getProjectPath(), "Assets")
        logger.info(base.path)
        def tree = project.fileTree(base)

        tree.include(new Spec<FileTreeElement>() {
            @Override
            boolean isSatisfiedBy(FileTreeElement element) {
                if(element.path.toLowerCase().contains("plugins") && element.name.toLowerCase() != "plugins") {
                    return element.path.toLowerCase().contains("plugins" + File.separator + getBuildPlatform().toLowerCase())
                }
                return true
            }
        })

        tree
    }

//    @OutputFiles
//    FileCollection getOutputFiles() {
//        project.fileTree(getOutputDirectory()) {
//            it.exclude("build", ".gradle", "**/*.meta")
//        }
//    }

    //@Internal("Base path of outputFiles")
    @OutputDirectory
    File getOutputDirectory() {
        project.file("${getOutputDirectoryBase()}/${getBuildPlatform()}/${getBuildEnvironment()}/project")
    }

    private String buildPlatform
    private String buildEnvironment
    private String exportMethodName
    private String toolsVersion
    private String outputDirectoryBase

    @Internal("Base path of outputFiles")
    File getOutputDirectoryBase() {
        outputDirectoryBase
    }

    void setOutputDirectoryBase(File outputDirectoryBase) {
        this.outputDirectoryBase = outputDirectoryBase
    }

    UnityBuildPlayerTask outputDirectoryBase(File outputDirectoryBase) {
        setOutputDirectoryBase(outputDirectoryBase)
        this
    }


    @Input
    String getBuildPlatform() {
        buildPlatform
    }

    void setBuildPlatform(String platform) {
        buildPlatform = platform
        try {
            buildTarget = platform as BuildTarget
        }
        catch(IllegalArgumentException ignored) {
            logger.warn("build target ${platform} unknown")
            buildTarget = BuildTarget.undefined
        }
    }

    void buildPlatform(String platform) {
        setBuildPlatform(platform)
    }

    @Input
    String getBuildEnvironment() {
        buildEnvironment
    }

    void setBuildEnvironment(String environment) {
        buildEnvironment = environment
    }

    void buildEnvironment(String environment) {
        setBuildEnvironment(environment)
    }

    @Input
    String getExportMethodName() {
        exportMethodName
    }

    void setExportMethodName(String method) {
        exportMethodName = method
    }
    void exportMethodName(String method) {
        setExportMethodName(method)
    }

    @Optional
    @Input
    String getToolsVersion() {
        toolsVersion
    }

    void setToolsVersion(String version) {
        toolsVersion = version
    }

    void toolsVersion(String version) {
        setToolsVersion(version)
    }

    private Object version

    @Input
    String getVersion() {
        convertToString(version)
    }

    void setVersion(String value) {
        version = value
    }

    UnityBuildPlayerTask version(String version) {
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

        if(getToolsVersion()) {
            customArgs += "toolsVersion=${getToolsVersion()}"
        }

        args "-executeMethod", getExportMethodName()
        args customArgs

        if (buildTarget == BuildTarget.undefined) {
            args BatchModeFlags.BUILD_TARGET, getBuildPlatform()
        }
        super.exec()
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
