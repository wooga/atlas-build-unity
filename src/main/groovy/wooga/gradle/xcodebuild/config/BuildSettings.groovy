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

package wooga.gradle.xcodebuild.config

import sun.invoke.empty.Empty

class BuildSettings implements GroovyInterceptable {

    private final Map<String, List<String>> rawSettings

    BuildSettings() {
        this(new HashMap<String, List<String>>())
    }

    private BuildSettings(Map<String, List<String>> rawSettings) {
        this.rawSettings = rawSettings
    }

    static EMPTY = new BuildSettings()

    BuildSettings otherCodeSignFlags(String flag) {
        if (!rawSettings["OTHER_CODE_SIGN_FLAGS"]) {
            rawSettings.put("OTHER_CODE_SIGN_FLAGS", [])
        }
        rawSettings["OTHER_CODE_SIGN_FLAGS"] << flag
        this
    }

    BuildSettings clone() {
        new BuildSettings(rawSettings)
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

    BuildSettings put(String key, List<String> values) {
        rawSettings.put(key, values)
        this
    }

    List<String> toList() {
        rawSettings.collect { String key, List<String> values ->
            def value = values.collect({
                if (it.isEmpty()) {
                    return "''"
                }
                it
            }).join(' ')

            "${key}=${value}".toString()
        }.sort()
    }

    @Override
    String toString() {
        return toList().toString()
    }

    private static String toBOOL(Boolean value) {
        value ? "YES" : "NO"
    }

    private static String convertToSettingsKey(String name) {
        name.replaceAll(/([a-z0-9])([A-Z])/) {
            all, before, delimiter -> "${before}_${delimiter.capitalize()}"
        }.toUpperCase()
    }


    def methodMissing(String name, def args) {
        if (args.length == 1) {
            Object value = args[0]
            if (String.isInstance(value)) {
                return put(convertToSettingsKey(name), value as String)
            }

            if (Boolean.isInstance(value)) {
                return put(convertToSettingsKey(name), value as Boolean)
            }

            if (List.isInstance(value)) {
                return put(convertToSettingsKey(name), (value as List).collect { it.toString() })
            }
        }

        throw new MissingMethodException(name, BuildSettings, args)
    }

    def propertyMissing(String name, Object value) {
        if (String.isInstance(value)) {
            put(convertToSettingsKey(name), value as String)

        } else if (Boolean.isInstance(value)) {
            put(convertToSettingsKey(name), value as Boolean)
        } else if (List.isInstance(value)) {
            put(convertToSettingsKey(name), (value as List).collect { it.toString() })
        } else {
            throw new MissingPropertyException(name, BuildSettings)
        }
    }

    def propertyMissing(String name) {
        def key = convertToSettingsKey(name)
        List<String> values = rawSettings.get(key)
        if (values && !values.empty) {
            if (values.size() > 1) {
                return values
            }

            String value = values.first()
            if (value == "YES" || value == "NO") {
                return value == "YES"
            }

            return value
        }

        throw new MissingPropertyException(name, BuildSettings)
    }
}
