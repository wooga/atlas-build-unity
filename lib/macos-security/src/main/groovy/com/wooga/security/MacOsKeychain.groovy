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

import com.wooga.security.command.AddGenericPassword
import com.wooga.security.command.AddInternetPassword
import com.wooga.security.command.CreateKeychain
import com.wooga.security.command.DeleteKeychain
import com.wooga.security.command.FindCertificate
import com.wooga.security.command.FindGenericPassword
import com.wooga.security.command.FindIdentity
import com.wooga.security.command.FindInternetPassword
import com.wooga.security.command.FindKey
import com.wooga.security.command.Import
import com.wooga.security.command.LockKeychain
import com.wooga.security.command.SetKeychainSettings
import com.wooga.security.command.ShowKeychainInfo
import com.wooga.security.command.UnlockKeychain
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString

class MacOsKeychain {

    final File location
    final String password

    MacOsKeychain(File location, String password) {
        this.location = location
        this.password = password
    }

    Boolean unlock() {
        try {
            return new UnlockKeychain().withKeychain(this).withPassword(this.password).execute()
        } catch (IOException ignored) {
            return false
        }
    }

    Boolean lock() {
        try {
            return new LockKeychain().withKeychain(this).execute()
        } catch (IOException ignored) {
            return false
        }
    }

    Boolean getLockWhenSystemSleeps() {
        getSettings().lockWhenSystemSleeps
    }

    Boolean setLockWhenSystemSleeps(Boolean value) {
        withSettings {
            it.lockWhenSystemSleeps = value
        }
    }

    Integer getTimeout() {
        getSettings().timeout
    }

    Boolean setTimeout(Integer timeout) {
        withSettings {
            it.timeout = timeout
        }
    }

    Boolean getLockAfterTimeout() {
        getSettings().lockAfterTimeout
    }

    Boolean withSettings(@ClosureParams(value = FromString.class, options = ["com.wooga.security.MacOsKeychainSettings"]) Closure action) {
        def settings = getSettings()

        action.call(settings)
        setSettings(settings)
    }

    MacOsKeychainSettings getSettings() {
        new ShowKeychainInfo(this).execute()
    }

    Boolean setSettings(MacOsKeychainSettings settings) {
        try {
            new SetKeychainSettings().withKeychain(this).withSettings(settings).execute()
        } catch (IOException ignored) {
            return false
        }
        true
    }

    Boolean addGenericPassword(String account, String service, String password, Map config = [:]) {
        new AddGenericPassword(account, service, password, config)
                .withKeychain(this)
                .execute()
        true
    }

    Boolean addInternetPassword(String account, String server, String password, Map config = [:]) {
        new AddInternetPassword(account, server, password, config)
                .withKeychain(this)
                .execute()
        true
    }

    Boolean importFile(File importFile, Map config = [:]) {
        new Import(importFile, this.location, config).execute()
        true
    }

    String findCertificate(Map query = [:]) {
        new FindCertificate(query).withKeychain(this).execute()
    }

    String findKey(Map query = [:]) {
        new FindKey(query).withKeychain(this).execute()
    }

    String findIdentity(Map query = [:]) {
        new FindIdentity(query).withKeychain(this).execute()
    }

    String findGenericPassword(String account, String service, Map query = [:]) {
        new FindGenericPassword(account, service, query).withKeychain(this).printPasswordOnly().execute()
    }

    String findInternetPassword(String account, String server, Map query = [:]) {
        new FindInternetPassword(account, server, query).withKeychain(this).printPasswordOnly().execute()
    }

    Boolean delete() {
        new DeleteKeychain().withKeychain(this).execute()
        this.location.delete()
    }

    Boolean exists() {
        this.location.exists()
    }

    static MacOsKeychain create(File location, String password) {
        new CreateKeychain(password, location).execute()
    }

    @Override
    String toString() {
        return "MacOsKeychain{" +
                "location=" + location +
                '}';
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof MacOsKeychain)) return false

        MacOsKeychain that = (MacOsKeychain) o

        if (location != that.location) return false
        if (password != that.password) return false

        return true
    }

    int hashCode() {
        int result
        result = (location != null ? location.hashCode() : 0)
        result = 31 * result + (password != null ? password.hashCode() : 0)
        return result
    }

}
