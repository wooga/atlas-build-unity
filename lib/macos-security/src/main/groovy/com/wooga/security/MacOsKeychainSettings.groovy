/*
 * Copyright 2021 Wooga GmbH
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

package com.wooga.security

class MacOsKeychainSettings {
    Boolean lockWhenSystemSleeps
    Integer timeout

    Boolean getLockAfterTimeout() {
        timeout >= 0
    }

    MacOsKeychainSettings(Boolean lockWhenSystemSleeps, Integer timeout) {
        this.lockWhenSystemSleeps = lockWhenSystemSleeps
        this.timeout = timeout
    }

    static fromOutput(String output) {
        Boolean lockWhenSystemSleep = output.contains("lock-on-sleep")
        Integer timeout = -1
        def m = (output.trim() =~ /.*timeout=(\d+)s.*/)
        if (m.matches()) {
            timeout = Integer.parseInt(m.group(1))
        }

        new MacOsKeychainSettings(lockWhenSystemSleep, timeout)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof MacOsKeychainSettings)) return false

        MacOsKeychainSettings settings = (MacOsKeychainSettings) o

        if (timeout != settings.timeout) return false
        if (lockWhenSystemSleeps != settings.lockWhenSystemSleeps) return false

        return true
    }

    int hashCode() {
        int result
        result = lockWhenSystemSleeps.hashCode()
        result = 31 * result + timeout.hashCode()
        return result
    }
}
