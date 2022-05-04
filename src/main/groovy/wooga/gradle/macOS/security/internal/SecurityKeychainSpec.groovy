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

package wooga.gradle.macOS.security.internal

import com.wooga.gradle.BaseSpec
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input

trait SecurityKeychainSpec extends BaseSpec {

    private  final Property<String> password = objects.property(String)

    @Input
    Property<String> getPassword() {
        password
    }

    void setPassword(String value) {
        password.set(value)
    }

    void setPassword(Provider<String> value) {
        password.set(value)
    }

    SecurityKeychainSpec password(String value) {
        setPassword(value)
        this
    }

    SecurityKeychainSpec password(Provider<String> value) {
        setPassword(value)
        this
    }
}
