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

package wooga.gradle.xcodebuild.xcpretty


import wooga.gradle.xcodebuild.xcpretty.formatters.Formatter

class Printer extends Writer {

    final Formatter formatter
    final Writer writer

    Printer(Formatter formatter, Writer writer) {
        super()
        this.formatter = formatter
        this.writer = writer
    }

    @Override
    void write(char[] text, int offset, int length) throws IOException {
        new String(text, offset, length).eachLine {
            prettyPrint(it)
        }
    }

    @Override
    void flush() throws IOException {
        writer.flush()
    }

    @Override
    void close() throws IOException {
        writer.close()
    }

    def prettyPrint(String text) {
        String formattedText = formatter.prettyFormat(text)
        if(!formattedText.isEmpty()) {
            writer.print(formattedText + formatter.optionalNewline)
            writer.flush()
        }
    }
}
