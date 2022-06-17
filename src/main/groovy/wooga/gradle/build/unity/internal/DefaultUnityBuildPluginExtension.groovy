/*
 * Copyright 2018-2020 Wooga GmbH
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

package wooga.gradle.build.unity.internal

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import wooga.gradle.build.unity.UBSVersion
import wooga.gradle.build.unity.UnityBuildPluginConventions
import wooga.gradle.build.unity.UnityBuildPluginExtension

class DefaultUnityBuildPluginExtension implements UnityBuildPluginExtension {

    protected final Project project

    @Override
    DefaultUnityBuildPluginExtension appConfigSecretsKey(String key) {
        setAppConfigSecretsKey(key)
        return this
    }

    DefaultUnityBuildPluginExtension(final Project project) {
        this.project = project
    }

    @Override
    FileCollection getAppConfigs() {
        project.fileTree(getAppConfigsDirectory()) {
            it.include UnityBuildPluginConventions.DEFAULT_APP_CONFIGS_INCLUDE_PATTERN
            it.exclude UnityBuildPluginConventions.DEFAULT_APP_CONFIGS_EXCLUDE_PATTERN
        }
    }
}
