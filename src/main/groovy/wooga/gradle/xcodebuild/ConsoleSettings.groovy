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

package wooga.gradle.xcodebuild

import org.gradle.api.logging.configuration.ConsoleOutput

class ConsoleSettings {
    enum ColorOption {
        never,
        always,
        auto
    }

    Boolean prettyPrint
    Boolean useUnicode
    ColorOption colorize = ColorOption.auto

    Boolean hasColors() {
        switch (colorize) {
            case ColorOption.never:
                return false
            case ColorOption.always:
                return true
            case ColorOption.auto:
                // normally one would do a check if stdout is a tty.
                // that is not possible here because of issues with java (solvable)
                // and gradle. If one needs to pipe the output of gradle to a file
                // --console plain will do the trick
                return (System.getenv("CLICOLOR") != "0" ||
                        System.getenv("CLICOLOR_FORCE") == "1")
        }
    }

    ConsoleSettings() {
        this(true, true, ColorOption.auto)
    }

    static ConsoleSettings fromGradleOutput(ConsoleOutput output) {
        switch (output) {
            case ConsoleOutput.Rich:
                return new ConsoleSettings(true, true, ColorOption.always)
                break
            case ConsoleOutput.Verbose:
                return new ConsoleSettings(false, false, ColorOption.never)
                break

            case ConsoleOutput.Plain:
                return new ConsoleSettings(true, false, ColorOption.never)
                break
            case ConsoleOutput.Auto:
                return new ConsoleSettings()
                break
        }
    }

    ConsoleSettings(Boolean prettyPrint, Boolean useUnicode, ColorOption colorize) {
        this.prettyPrint = prettyPrint
        this.useUnicode = useUnicode
        this.colorize = colorize
    }

    @Override
    String toString() {
        return "ConsoleSettings{" +
                "prettyPrint=" + prettyPrint +
                ", useUnicode=" + useUnicode +
                ", colorize=" + colorize +
                '}';
    }
}
