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

import org.gradle.api.internal.provider.AbstractProvider
import org.gradle.api.provider.Provider

class MemoisationProvider<T> extends AbstractProvider<T> implements Provider<T> {

    private T inferredValue
    private final Provider<T> inner

    MemoisationProvider(Provider<T> provider) {
        this.inner = provider
    }

    @Override
    Class<T> getType() {
        null
    }

    @Override
    T getOrNull() {
        if(!inferredValue) {
            inferredValue = inner.getOrNull()
        }
        inferredValue
    }
}
