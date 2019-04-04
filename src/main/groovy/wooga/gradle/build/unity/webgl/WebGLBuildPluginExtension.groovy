/*
 * Copyright 2019 Wooga GmbH
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

package wooga.gradle.build.unity.webgl

import org.gradle.api.Action
import org.gradle.api.credentials.AwsCredentials

interface WebGLBuildPluginExtension {

    AwsCredentials getCdnCredentials()
    void setCdnCredentials(AwsCredentials credentials)

    WebGLBuildPluginExtension cdnCredentials(Closure configuration)
    WebGLBuildPluginExtension cdnCredentials(Action<AwsCredentials> action)
    WebGLBuildPluginExtension cdnCredentials(AwsCredentials credentials)


    Integer getWebGLMemorySize()
    void setWebGLMemorySize(Integer size)
    WebGLBuildPluginExtension webGLMemorySize(Integer size)

    Boolean getWebGLSupportsES2()
    void setWebGLSupportsES2(Boolean value)
    WebGLBuildPluginExtension webGLSupportsES2(Boolean value)

    Boolean getWebGLSupportsES3()
    void setWebGLSupportsES3(Boolean value)
    WebGLBuildPluginExtension webGLSupportsES3(Boolean value)

    String getCdnEndpoint()
    void setCdnEndpoint(String url)
    WebGLBuildPluginExtension cdnEndpoint(String url)

    String getS3Endpoint()
    void setS3Endpoint(String url)
    WebGLBuildPluginExtension s3Endpoint(String url)

    String getCdnReleaseDirectory()
    void setCdnReleaseDirectory(String dir)
    WebGLBuildPluginExtension cdnReleaseDirectory(String dir)

}
