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
 *
 *
 */

package wooga.gradle.build.unity.internal

import nebula.test.ProjectSpec
import spock.lang.Unroll

class MemoisationProviderSpec extends ProjectSpec {
    def "provider caches value"() {
        given: "a random number generator"
        def random = new Random()
        and: "a normal provider"
        def baseProvider = project.provider({ random.nextInt() })

        and: "a memoisation provider"
        def subject = new MemoisationProvider(baseProvider)

        expect:
        subject.get() == subject.get()
        subject.get() == subject.get()
        subject.get() == subject.get()
        baseProvider.get() != baseProvider.get()
        baseProvider.get() != baseProvider.get()
        baseProvider.get() != baseProvider.get()
    }

    @Unroll
    def "getOrNull returns value or null"() {
        given: "a memoisation provider"
        def baseProvider = project.provider({ innerValue })
        def subject = new MemoisationProvider(baseProvider)

        when:
        def value = subject.getOrNull()

        then:
        value == innerValue
        noExceptionThrown()

        where:
        innerValue << [1, null]
    }

    @Unroll
    def "get throws error when value is null"() {
        given: "a memoisation provider"
        def baseProvider = project.provider({ null })
        def subject = new MemoisationProvider(baseProvider)

        when:
        subject.get()

        then:
        thrown(IllegalStateException)
    }

    @Unroll
    def "present returns #expectedValue when value is #message"() {
        given: "a memoisation provider"
        def baseProvider = project.provider({ innerValue })
        def subject = new MemoisationProvider(baseProvider)

        when:
        def value = subject.isPresent()

        then:
        value == expectedValue

        where:
        innerValue | expectedValue
        1          | true
        null       | false
        message = innerValue == null ? "not set" : "set"
    }
}
