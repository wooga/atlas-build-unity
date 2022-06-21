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

package wooga.gradle.xcodebuild.tasks


import org.gradle.util.GUtil
import wooga.gradle.xcodebuild.XcodeArchiveSpecBase

abstract class AbstractXcodeArchiveTask extends AbstractXcodeTask implements XcodeArchiveSpecBase {

    AbstractXcodeArchiveTask() {
        // Sourced from the gradle abstract archive task:
        // https://github.com/gradle/gradle/blob/master/subprojects/core/src/main/java/org/gradle/api/tasks/bundling/AbstractArchiveTask.java
        archiveName.convention(project.provider({
            // [baseName]-[appendix]-[version]-[classifier].[extension]
            String name = GUtil.elvis(baseName.getOrNull(), "");
            name += maybe(name, appendix.getOrNull());
            name += maybe(name, version.getOrNull());
            name += maybe(name, classifier.getOrNull());

            String extension = extension.getOrNull();
            name += GUtil.isTrue(extension) ? "." + extension : "";
            return name;
        }))
    }

    private static String maybe(String prefix, String value) {
        if (GUtil.isTrue(value)) {
            if (GUtil.isTrue(prefix)) {
                return "-".concat(value);
            } else {
                return value;
            }
        }
        return "";
    }
}
