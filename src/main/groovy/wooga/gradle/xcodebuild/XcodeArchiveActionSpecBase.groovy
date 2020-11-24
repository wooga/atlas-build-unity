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

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

interface XcodeArchiveActionSpecBase<T extends XcodeArchiveActionSpecBase> {
    Property<String> getArchiveName()

    void setArchiveName(String value)
    void setArchiveName(Provider<String> value)

    T archiveName(String value)
    T archiveName(Provider<String> value)

    Property<String> getBaseName()

    void setBaseName(String value)
    void setBaseName(Provider<String> value)

    T baseName(String value)
    T baseName(Provider<String> value)

    Property<String> getAppendix()

    void setAppendix(String value)
    void setAppendix(Provider<String> value)

    T appendix(String value)
    T appendix(Provider<String> value)

    Property<String> getVersion()

    void setVersion(String value)
    void setVersion(Provider<String> value)

    T version(String value)
    T version(Provider<String> value)

    Property<String> getExtension()

    void setExtension(String value)
    void setExtension(Provider<String> value)

    T extension(String value)
    T extension(Provider<String> value)

    Property<String> getClassifier()

    void setClassifier(String value)
    void setClassifier(Provider<String> value)

    T classifier(String value)
    T classifier(Provider<String> value)

    DirectoryProperty getDestinationDir()

    void setDestinationDir(File value)
    void setDestinationDir(Provider<Directory> value)

    T destinationDir(File value)
    T destinationDir(Provider<Directory> value)
}
