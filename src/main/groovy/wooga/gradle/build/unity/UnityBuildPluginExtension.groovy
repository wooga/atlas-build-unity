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

package wooga.gradle.build.unity

import org.gradle.api.file.FileCollection

interface UnityBuildPluginExtension<T extends UnityBuildPluginExtension> {

    File getAppConfigsDirectory()
    void setAppConfigsDirectory(Object appConfigsDirectory)
    T appConfigsDirectory(Object appConfigsDirectory)

    FileCollection getAppConfigs()

    String getExportMethodName()
    void setExportMethodName(String method)
    T exportMethodName(String method)

    String getDefaultAppConfigName()
    void setDefaultAppConfigName(String name)
    T defaultAppConfigName(String name)

    String getToolsVersion()
    void setToolsVersion(String version)
    T toolsVersion(String version)

    File getOutputDirectoryBase()
    void setOutputDirectoryBase(Object outputDirectoryBase)
    T outputDirectoryBase(Object outputDirectoryBase)
}