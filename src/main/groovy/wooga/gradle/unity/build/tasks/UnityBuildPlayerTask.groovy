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

package wooga.gradle.unity.build.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import wooga.gradle.unity.tasks.internal.AbstractUnityProjectTask

class UnityBuildPlayerTask extends AbstractUnityProjectTask {

    UnityBuildPlayerTask() {
        super(UnityBuildPlayerTask.class)
    }

    @InputDirectory
    getInputDirectory() {
        getProjectPath()
    }

    @OutputDirectory
    getOutputDirectory() {
        getTemporaryDir()
    }

    private String buildPlatform
    private String buildEnvironment
    private String exportMethodName

    @Input
    String getBuildPlatform() {
        buildPlatform
    }

    void setBuildPlatform(String platform) {
        buildPlatform = platform
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

    @Override
    protected void exec() {
        args "-executeMethod", getExportMethodName()
        args "-CustomArgs:platform=${getBuildPlatform()};environment=${getBuildEnvironment()};outputPath=${getOutputDirectory().getPath()}"
        super.exec()
    }
}
