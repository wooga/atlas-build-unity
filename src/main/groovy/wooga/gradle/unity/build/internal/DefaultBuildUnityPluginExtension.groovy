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

package wooga.gradle.unity.build.internal

import org.gradle.util.GUtil
import wooga.gradle.unity.build.BuildUnityPluginExtension

class DefaultBuildUnityPluginExtension implements BuildUnityPluginExtension {

    static List<String> DEFAULT_ENVIRONMENTS = ["ci", "staging", "production"]
    static List<String> DEFAULT_PLATFORMS = ["android", "iOS", "webGL"]

    private final Set<String> environments = new ArrayList<String>()
    private final Set<String> platforms = new ArrayList<String>()

    DefaultBuildUnityPluginExtension() {
        environments(DEFAULT_ENVIRONMENTS)
        platforms(DEFAULT_PLATFORMS)
    }

    @Override
    Set<String> getPlatforms() {
        return this.platforms
    }

    @Override
    void setPlatforms(Iterable platforms) {
        this.platforms.clear()
        this.platforms.addAll(platforms)
    }

    @Override
    BuildUnityPluginExtension platforms(Iterable platforms) {
        GUtil.addToCollection(this.platforms, platforms)
        return this
    }

    @Override
    BuildUnityPluginExtension platforms(String[] platforms) {
        if (environments == null) {
            throw new IllegalArgumentException("platforms == null!")
        }
        this.platforms.addAll(Arrays.asList(platforms))
        return this
    }

    @Override
    BuildUnityPluginExtension platform(String platform) {
        this.platforms.add(platform)
        return this
    }

    @Override
    Set<String> getEnvironments() {
        return environments
    }

    @Override
    void setEnvironments(Iterable environments) {
        this.environments.clear()
        this.environments.addAll(environments)
    }

    @Override
    BuildUnityPluginExtension environments(Iterable environments) {
        GUtil.addToCollection(this.environments, environments)
        return this
    }

    @Override
    BuildUnityPluginExtension environments(String[] environments) {
        if (environments == null) {
            throw new IllegalArgumentException("environments == null!")
        }
        this.environments.addAll(Arrays.asList(environments))
        return this
    }

    @Override
    BuildUnityPluginExtension environment(String environment) {
        this.environments.add(environment)
        return this
    }
}
