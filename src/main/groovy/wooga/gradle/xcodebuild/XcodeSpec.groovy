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

import com.wooga.gradle.BaseSpec
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Console

import static org.gradle.util.ConfigureUtil.configureUsing

trait XcodeSpec extends BaseSpec {

    final Property<ConsoleSettings> consoleSettings = objects.property(ConsoleSettings)

    @Console
    Property<ConsoleSettings> getConsoleSettings() {
        consoleSettings
    }

    void setConsoleSettings(ConsoleSettings value) {
        consoleSettings.set(value)
    }

    void setConsoleSettings(Provider<ConsoleSettings> value) {
        consoleSettings.set(value)
    }

    void consoleSettings(ConsoleSettings value) {
        consoleSettings.set(value)
    }

    void consoleSettings(Provider<ConsoleSettings> value) {
        consoleSettings.set(value)
    }

    void consoleSettings(Closure configuration) {
        consoleSettings(configureUsing(configuration))
    }

    void consoleSettings(Action<ConsoleSettings> action) {
        def settings = consoleSettings.getOrElse(new ConsoleSettings())
        action.execute(settings)
        consoleSettings.set(settings)
    }
}
