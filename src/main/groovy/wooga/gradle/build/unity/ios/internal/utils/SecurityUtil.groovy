/*
 * Copyright 2018 Wooga GmbH
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

package wooga.gradle.build.unity.ios.internal.utils

import org.apache.commons.lang3.StringUtils

class SecurityUtil {
    enum Domain {
        user, system, common, dynamic, all
    }

    static File getLoginKeyChain() {
        def command = ["security", "login-keychain"]

        def commandOutput = executeCommand(command)
        commandOutput.toString().trim().readLines().collect {
            new File(StringUtils.substringBetween(it.trim(), '"'))
        }.first()
    }

    static File getDefaultKeyChain(Domain domain = Domain.user) {
        def command = ["security", "default-keychain"]
        if (domain != Domain.all) {
            command << "-d" << domain.toString()
        }

        def commandOutput = executeCommand(command)
        commandOutput.toString().trim().readLines().collect {
            new File(StringUtils.substringBetween(it.trim(), '"'))
        }.first()
    }

    private static String executeCommand(command) {
        def stdout = new StringBuilder()
        def stderr = new StringBuilder()

        def p = new ProcessBuilder(command).start()
        p.consumeProcessOutput(stdout,stderr)

        if (p.waitFor() != 0) {
            throw new Error("Security error\n" + stderr)
        }

        stdout
    }

    private static String listOrSetKeychains(Iterable<File> keychains = null, Domain domain = Domain.user) {
        def command = ["security"]
        command << "list-keychains"
        if (domain != Domain.all) {
            command << "-d" << domain.toString()
        }

        if (keychains != null) {
            def k = keychains.collect { expandPath(it.canonicalPath) }.unique()
            command << "-s"
            for (String keychainPath : k) {
                command << keychainPath
            }
        }

        executeCommand(command)
    }

    static List<File> listKeychains(Domain domain = Domain.user) {
        def commandOutput = listOrSetKeychains(null, domain)
        commandOutput.toString().trim().readLines().collect {
            new File(StringUtils.substringBetween(it.trim(), '"'))
        }
    }

    static void setKeychains(Iterable<File> keychains, Domain domain = Domain.user) {
        listOrSetKeychains(keychains, domain)
    }

    static boolean keychainIsAdded(File keychain, Domain domain = Domain.user) {
        listKeychains(domain).contains(canonical(keychain))
    }

    static void resetKeychains(Domain domain = Domain.user) {
        def rawValue = System.getenv().get("ATLAS_BUILD_UNITY_IOS_DEFAULT_KEYCHAINS")
        def defaultKeyChains
        if(rawValue) {
            defaultKeyChains = rawValue.split(File.pathSeparator).collect { canonical(new File(it)) }
        } else {
            defaultKeyChains = [getLoginKeyChain(), getDefaultKeyChain()].unique()
        }
        setKeychains(defaultKeyChains, domain)
    }

    static String expandPath(String path) {
        if (path.startsWith("~" + File.separator)) {
            path = System.getProperty("user.home") + path.substring(1)
        }
        path
    }

    static File expandPath(File path) {
        new File(expandPath(path.path))
    }

    static File canonical(File keychain) {
        expandPath(keychain).canonicalFile
    }
}
