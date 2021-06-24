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
 */

package wooga.gradle.fastlane

import wooga.gradle.unity.utils.PropertyLookup


class FastlanePluginConventions {

    static final PropertyLookup USERNAME_LOOKUP = new PropertyLookup("FASTLANE_USERNAME", "fastlane.username", null)
    static final PropertyLookup PASSWORD_LOOKUP = new PropertyLookup("FASTLANE_PASSWORD", "fastlane.password", null)
    static final PropertyLookup API_KEY_PATH_LOOKUP = new PropertyLookup("FASTLANE_API_KEY_PATH", "fastlane.apiKeyPath", null)
}
