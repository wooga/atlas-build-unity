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

package com.wooga.security.command

import sun.util.logging.PlatformLogger

import java.util.logging.Level
import java.util.logging.Logger

abstract class SecurityCommand<T> {

    Logger logger = Logger.getLogger(this.class.name)

    final T execute() {
        def result = callSecurity()
        convertResult(result)
    }

    List<String> getSensitiveFlags() {
        []
    }

    abstract String getCommand()

    abstract protected List<String> getArguments()

    abstract protected T convertResult(String output)

    private String callSecurity() {
        def processBuilder = new ProcessBuilder()
        def command = ["security", getCommand()]
        command.addAll(getArguments())
        processBuilder.command(command)

        if (logger.isLoggable(Level.INFO)) {
            logger.info("Run security command:")
            if (getSensitiveFlags().isEmpty()) {
                logger.info(command.join(" "))
            } else {
                def printOutput = []
                def hideNextArgument = false
                command.each {
                    if (hideNextArgument && !it.startsWith("-")) {
                        printOutput << "****"
                        hideNextArgument = false
                    } else {
                        if (sensitiveFlags.contains(it)) {
                            hideNextArgument = true
                        }
                        printOutput << it
                    }
                }
                logger.info(printOutput.join(" "))
            }
        }

        def process = processBuilder.start()
        def out = new StringBuffer()
        def err = new StringBuffer()
        process.consumeProcessOutput(out, err)

        def result = process.waitFor()
        if (result != 0) {
            def message = ""
            if (err.size() > 0) {
                message = err.toString()
            }
            throw new IOException("Security command ${this.command} failed: ${message}")
        }

        err.toString() + out.toString()
    }

    static void validateStringProperty(String value, String propertyName) {
        if (value == null) {
            throw new NullPointerException("provided ${propertyName} is null")
        }

        if (value.isEmpty()) {
            throw new IllegalArgumentException("provided ${propertyName} is empty")
        }
    }

    static void validateFileProperty(File file, String propertyName) {
        if (!file) {
            throw new NullPointerException("provided ${propertyName} is null")
        }

        if (!file.exists()) {
            throw new IllegalArgumentException("provided ${propertyName} '${file.path}' does not exist")
        }

        if (!file.isFile()) {
            throw new IllegalArgumentException("provided ${propertyName} '${file.path}' is not a file")
        }
    }

    static void validateKeychainProperty(File keychain) {
        validateFileProperty(keychain, "keychain")
    }

    static void validateKeychainsProperty(List<File> keychains) {
        keychains.each {
            validateKeychainProperty(it)
        }
    }
}
