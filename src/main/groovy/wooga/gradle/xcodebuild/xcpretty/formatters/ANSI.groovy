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

package wooga.gradle.xcodebuild.xcpretty.formatters

class ANSI {

    public Boolean colorize = false

    enum Effect {
        reset('0'),
        bold('1'),
        underline('4'),

        private final String value

        Effect(String value) {
            this.value = value
        }

        @Override
        String toString() {
            value
        }
    }

    enum Color {
        black('30'),
        red('31'),
        green('32'),
        yellow('33'),
        blue('34'),
        cyan('36'),
        white('37'),
        plain('39')

        private final String value

        Color(String value) {
            this.value = value
        }

        @Override
        String toString() {
            value
        }
    }

    String red(text) {
        ansiParse(text, Color.red)
    }

    String green(text) {
        ansiParse(text, Color.green, Effect.bold)
    }

    String cyan(text) {
        ansiParse(text, Color.cyan)
    }

    String yellow(text) {
        ansiParse(text, Color.yellow)
    }

    String white(text) {
        ansiParse(text, Color.plain, Effect.bold)
    }

    String ansiParse(String text, Color color, Effect effect = null) {
        if (!colorize) {
            return text
        }

        String colorsCode = color.toString()
        String effectCode = effect ? ';' + effect.toString() : ''

        "\\e[${colorsCode}${effectCode}m${text}\\e[${Effect.reset.toString()}m"
    }
}
