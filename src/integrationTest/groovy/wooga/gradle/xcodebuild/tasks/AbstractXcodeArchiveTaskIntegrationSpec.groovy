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

import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetInvocation
import com.wooga.gradle.test.writers.PropertySetterWriter
import spock.lang.Unroll

abstract class AbstractXcodeArchiveTaskIntegrationSpec<T extends AbstractXcodeArchiveTask> extends AbstractXcodeTaskIntegrationSpec<T> {

    @Unroll("can set property #property with #method and type #type")
    def "can set property AbstractXcodeArchiveTask"() {
        expect:
        addMockTask(true)
        def query = runPropertyQuery(getter, setter)
        query.matches(rawValue)

        where:
        property         | method                            | rawValue             | type
        "archiveName"    | PropertySetInvocation.method      | "Test1"              | "String"
        "archiveName"    | PropertySetInvocation.method      | "Test2"              | "Provider<String>"
        "archiveName"    | PropertySetInvocation.providerSet | "Test1"              | "String"
        "archiveName"    | PropertySetInvocation.providerSet | "Test2"              | "Provider<String>"
        "archiveName"    | PropertySetInvocation.setter      | "Test3"              | "String"
        "archiveName"    | PropertySetInvocation.setter      | "Test4"              | "Provider<String>"

        "baseName"       | PropertySetInvocation.method      | "Test1"              | "String"
        "baseName"       | PropertySetInvocation.method      | "Test2"              | "Provider<String>"
        "baseName"       | PropertySetInvocation.providerSet | "Test1"              | "String"
        "baseName"       | PropertySetInvocation.providerSet | "Test2"              | "Provider<String>"
        "baseName"       | PropertySetInvocation.setter      | "Test3"              | "String"
        "baseName"       | PropertySetInvocation.setter      | "Test4"              | "Provider<String>"

        "appendix"       | PropertySetInvocation.method      | "Test1"              | "String"
        "appendix"       | PropertySetInvocation.method      | "Test2"              | "Provider<String>"
        "appendix"       | PropertySetInvocation.providerSet | "Test1"              | "String"
        "appendix"       | PropertySetInvocation.providerSet | "Test2"              | "Provider<String>"
        "appendix"       | PropertySetInvocation.setter      | "Test3"              | "String"
        "appendix"       | PropertySetInvocation.setter      | "Test4"              | "Provider<String>"

        "version"        | PropertySetInvocation.method      | "Test1"              | "String"
        "version"        | PropertySetInvocation.method      | "Test2"              | "Provider<String>"
        "version"        | PropertySetInvocation.providerSet | "Test1"              | "String"
        "version"        | PropertySetInvocation.providerSet | "Test2"              | "Provider<String>"
        "version"        | PropertySetInvocation.setter      | "Test3"              | "String"
        "version"        | PropertySetInvocation.setter      | "Test4"              | "Provider<String>"

        "extension"      | PropertySetInvocation.method      | "Test1"              | "String"
        "extension"      | PropertySetInvocation.method      | "Test2"              | "Provider<String>"
        "extension"      | PropertySetInvocation.providerSet | "Test1"              | "String"
        "extension"      | PropertySetInvocation.providerSet | "Test2"              | "Provider<String>"
        "extension"      | PropertySetInvocation.setter      | "Test3"              | "String"
        "extension"      | PropertySetInvocation.setter      | "Test4"              | "Provider<String>"

        "classifier"     | PropertySetInvocation.method      | "Test1"              | "String"
        "classifier"     | PropertySetInvocation.method      | "Test2"              | "Provider<String>"
        "classifier"     | PropertySetInvocation.providerSet | "Test1"              | "String"
        "classifier"     | PropertySetInvocation.providerSet | "Test2"              | "Provider<String>"
        "classifier"     | PropertySetInvocation.setter      | "Test3"              | "String"
        "classifier"     | PropertySetInvocation.setter      | "Test4"              | "Provider<String>"

        "destinationDir" | PropertySetInvocation.method      | osPath("/some/path") | "File"
        "destinationDir" | PropertySetInvocation.method      | osPath("/some/path") | "Provider<Directory>"
        "destinationDir" | PropertySetInvocation.providerSet | osPath("/some/path") | "File"
        "destinationDir" | PropertySetInvocation.providerSet | osPath("/some/path") | "Provider<Directory>"
        "destinationDir" | PropertySetInvocation.setter      | osPath("/some/path") | "File"
        "destinationDir" | PropertySetInvocation.setter      | osPath("/some/path") | "Provider<Directory>"

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(rawValue, type)
            .use(method)
            .serialize(wrapValueFallback)
        getter = new PropertyGetterTaskWriter(setter)
    }



}
