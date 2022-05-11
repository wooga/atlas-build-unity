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
 */

package wooga.gradle.xcodebuild

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory

trait XcodeArchiveSpecBase extends BaseSpec {

    private final Property<String> archiveName = objects.property(String)

    @Input
    Property<String> getArchiveName(){
        archiveName
    }

    void setArchiveName(String value) {
        archiveName.set(value)
    }

    void setArchiveName(Provider<String> value) {
        archiveName.set(value)
    }

    void archiveName(String value) {
        archiveName.set(value)
    }

    void archiveName(Provider<String> value) {
        archiveName.set(value)
    }

    private final Property<String> baseName = objects.property(String)

    @Internal
    Property<String> getBaseName() {
        baseName
    }

    void setBaseName(String value) {
        baseName.set(value)
    }

    void setBaseName(Provider<String> value) {
        baseName.set(value)
    }

    private final Property<String> appendix = objects.property(String)

    @Internal
    Property<String> getAppendix() {
        appendix
    }

    void setAppendix(String value) {
        appendix.set(value)
    }

    void setAppendix(Provider<String> value) {
        appendix.set(value)
    }

    private final Property<String> version = objects.property(String)

    @Internal
    Property<String> getVersion() {
        version
    }

    void setVersion(String value) {
        version.set(value)
    }

    void setVersion(Provider<String> value) {
        version.set(value)
    }

    private final Property<String> extension = objects.property(String)

    @Internal
    Property<String> getExtension() {
        extension
    }

    void setExtension(String value) {
        extension.set(value)
    }

    void setExtension(Provider<String> value) {
        extension.set(value)
    }

    private final Property<String> classifier = objects.property(String)

    @Internal
    Property<String> getClassifier() {
        classifier
    }

    void setClassifier(String value) {
        classifier.set(value)
    }

    void setClassifier(Provider<String> value) {
        classifier.set(value)
    }

    private final DirectoryProperty destinationDir = objects.directoryProperty()

    @OutputDirectory
    DirectoryProperty getDestinationDir(){
        destinationDir
    }

    void setDestinationDir(File value) {
        destinationDir.set(value)
    }

    void setDestinationDir(Provider<Directory> value) {
        destinationDir.set(value)
    }
}
