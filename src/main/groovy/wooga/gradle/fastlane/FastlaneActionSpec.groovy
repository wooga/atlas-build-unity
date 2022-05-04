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

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

trait FastlaneActionSpec extends BaseSpec {

    private final RegularFileProperty apiKeyPath = objects.fileProperty()

    @Optional
    @InputFile
    RegularFileProperty getApiKeyPath() {
        apiKeyPath
    }

    void setApiKeyPath(File value) {
        apiKeyPath.set(value)
    }

    void setApiKeyPath(Provider<RegularFile> value) {
        apiKeyPath.set(value)
    }
}
