/*
 * Copyright 2020 Wooga GmbH
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
 */

package wooga.gradle.xcodebuild


import nebula.test.ProjectSpec
import wooga.gradle.build.unity.UnityBuildPlugin
import wooga.gradle.xcodebuild.internal.DefaultXcodeBuildPluginExtension

class XcodeBuildPluginSpec extends ProjectSpec {
    public static final String PLUGIN_NAME = 'net.wooga.xcodebuild'

    def 'Creates the [xcodebuild] extension'() {
        given:
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        assert !project.extensions.findByName(XcodeBuildPlugin.EXTENSION_NAME)

        when:
        project.plugins.apply(PLUGIN_NAME)

        then:
        def extension = project.extensions.findByName(XcodeBuildPlugin.EXTENSION_NAME)
        extension instanceof DefaultXcodeBuildPluginExtension
    }
}
