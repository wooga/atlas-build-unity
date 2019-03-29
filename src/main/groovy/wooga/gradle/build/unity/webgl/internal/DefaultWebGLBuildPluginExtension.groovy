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

package wooga.gradle.build.unity.webgl.internal

import org.gradle.api.Action
import org.gradle.api.credentials.AwsCredentials
import wooga.gradle.build.unity.webgl.WebGLBuildPluginExtension

import static org.gradle.util.ConfigureUtil.configureUsing

class DefaultWebGLBuildPluginExtension implements WebGLBuildPluginExtension {

    private final AwsCredentials cdnCredentials
    private String cdnEndpoint
    private String s3Endpoint
    private String cdnReleaseDirectory
    private Integer webGLMemorySize
    private Boolean webGLSupportsES2
    private Boolean webGLSupportsES3

    @Override
    AwsCredentials getCdnCredentials() {
        cdnCredentials
    }

    @Override
    void setCdnCredentials(AwsCredentials credentials) {
        cdnCredentials.accessKey = credentials.accessKey
        cdnCredentials.secretKey = credentials.secretKey
        cdnCredentials.sessionToken = credentials.sessionToken
    }

    @Override
    WebGLBuildPluginExtension cdnCredentials(Closure configuration) {
        cdnCredentials(configureUsing(configuration))
        this
    }

    @Override
    WebGLBuildPluginExtension cdnCredentials(Action<AwsCredentials> action) {
        action.execute(cdnCredentials)
        this
    }

    @Override
    WebGLBuildPluginExtension cdnCredentials(AwsCredentials credentials) {
        setCdnCredentials(credentials)
        this
    }

    @Override
    Integer getWebGLMemorySize() {
        this.webGLMemorySize
    }

    @Override
    void setWebGLMemorySize(Integer size) {
        this.webGLMemorySize = size
    }

    @Override
    WebGLBuildPluginExtension webGLMemorySize(Integer size) {
        this.setWebGLMemorySize(size)
        this
    }

    @Override
    Boolean getWebGLSupportsES2() {
        this.webGLSupportsES2
    }

    @Override
    void setWebGLSupportsES2(Boolean value) {
        this.webGLSupportsES2 = value
    }

    @Override
    WebGLBuildPluginExtension webGLSupportsES2(Boolean value) {
        this.setWebGLSupportsES2(value)
        this
    }

    @Override
    Boolean getWebGLSupportsES3() {
        this.webGLSupportsES3
    }

    @Override
    void setWebGLSupportsES3(Boolean value) {
        this.webGLSupportsES3 = value
    }

    @Override
    WebGLBuildPluginExtension webGLSupportsES3(Boolean value) {
        this.setWebGLSupportsES3(value)
        this
    }

    @Override
    String getCdnEndpoint() {
        this.cdnEndpoint
    }

    @Override
    void setCdnEndpoint(String url) {
        this.cdnEndpoint = url
    }

    @Override
    WebGLBuildPluginExtension cdnEndpoint(String url) {
        this.setCdnEndpoint(url)
        this
    }

    @Override
    String getS3Endpoint() {
        this.s3Endpoint
    }

    @Override
    void setS3Endpoint(String url) {
        this.s3Endpoint = url
    }

    @Override
    WebGLBuildPluginExtension s3Endpoint(String url) {
        this.setS3Endpoint(url)
        this
    }

    @Override
    String getCdnReleaseDirectory() {
        cdnReleaseDirectory
    }

    @Override
    void setCdnReleaseDirectory(String dir) {
        cdnReleaseDirectory = dir
    }

    @Override
    WebGLBuildPluginExtension cdnReleaseDirectory(String dir) {
        setCdnReleaseDirectory(dir)
        this
    }

    DefaultWebGLBuildPluginExtension() {
        cdnCredentials = new DefaultAWSCredentials()
    }

    class DefaultAWSCredentials implements AwsCredentials {

        String accessKey
        String secretKey
        String sessionToken

        DefaultAWSCredentials() {
        }

        String toString() {
            String.format("AWSCredentials [username: %s]", this.accessKey)
        }
    }
}
