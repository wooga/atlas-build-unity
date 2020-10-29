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
 *
 */

package wooga.gradle.xcodebuild.internal

class BuildSettings {

    final Map<String, List<String>> rawSettings

    BuildSettings() {
        rawSettings = [:]
    }

    BuildSettings otherCodeSignFlags(String flag) {
        if(!rawSettings["OTHER_CODE_SIGN_FLAGS"]) {
            rawSettings.put("OTHER_CODE_SIGN_FLAGS", [])
        }
        rawSettings["OTHER_CODE_SIGN_FLAGS"] << flag
        this
    }

    BuildSettings otherCodeSignFlags(String flag, String value) {
        otherCodeSignFlags(flag)
        rawSettings["OTHER_CODE_SIGN_FLAGS"] << value
        this
    }

    BuildSettings codeSignIdentity(String identity) {
        put("CODE_SIGN_IDENTITY", identity)
    }

    BuildSettings codeSigningRequired(Boolean value) {
        put("CODE_SIGNING_REQUIRED", value)
    }

    BuildSettings codeSigningAllowed(Boolean value) {
        put("CODE_SIGNING_ALLOWED", value)
    }

    BuildSettings developmentTeam(String team) {
        put("DEVELOPMENT_TEAM", team)
    }

    BuildSettings put(String key, String value) {
        rawSettings.put(key, [value])
        this
    }

    BuildSettings put(String key, Boolean value) {
        rawSettings.put(key, [toBOOL(value)])
        this
    }

    List<String> toList() {
        rawSettings.collect {String key, List<String> values ->
            def value = values.collect({
                if(it.isEmpty()){
                    return "''"
                }
                it
            }).join(' ')

            "${key}=${value}".toString()
        }
    }

    private static String toBOOL(Boolean value) {
        value ? "YES" : "NO"
    }
}
