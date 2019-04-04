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

import groovy.json.JsonOutput
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.Copy
import org.meteogroup.jbrotli.Brotli
import org.meteogroup.jbrotli.BrotliCompressor
import org.meteogroup.jbrotli.libloader.BrotliLibraryLoader
import wooga.gradle.build.unity.webgl.internal.DefaultWebGLBuildPluginExtension

class WebGLBuildPlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = "webglBuild"

    private Project project
    private DefaultWebGLBuildPluginExtension extension

    @Override
    void apply(Project project) {
        this.project = project
        project.pluginManager.apply(BasePlugin.class)
        project.pluginManager.apply(PublishingPlugin.class)

        this.extension = project.getExtensions().create(DefaultWebGLBuildPluginExtension, EXTENSION_NAME, DefaultWebGLBuildPluginExtension.class)

        def webglBuildDir = "${project.projectDir}/UnityOutput"
        def outputDir = "${project.buildDir}/outputs"
        def intermediatesDir = "${project.buildDir}/intermediates"
        def gzipDir = "${intermediatesDir}/gzip"
        def brotliDir = "${intermediatesDir}/brotli"

        def tasks = project.tasks

        def assembleGzipArtifacts = tasks.create("assembleGzipArtifacts") {
            description "assembles gzip compressed assets"
            group BasePlugin.BUILD_GROUP
            doLast {
                project.file(gzipDir).mkdirs()

                project.fileTree(webglBuildDir) {
                    include '*.unityweb'
                }.each { File sourceFile ->
                    println(sourceFile)
                    def fileName = sourceFile.getName()
                    def destFile = "${gzipDir}/${fileName}.gz"

                    project.ant.gzip(src: sourceFile.getAbsolutePath(), destfile: destFile)
                }
            }
        }

        def assembleBrotliArtifacts = tasks.create("assembleBrotliArtifacts") {
            description "assembles brotli compressed assets"
            group BasePlugin.BUILD_GROUP
            doLast {
                BrotliLibraryLoader.loadBrotli()
                project.file(brotliDir).mkdirs()

                def params = new Brotli.Parameter(Brotli.DEFAULT_MODE, 8, Brotli.DEFAULT_LGWIN, 17)

                project.fileTree(webglBuildDir) {
                    include '*.unityweb'
                }.each { File sourceFile ->
                    def fileName = sourceFile.getName()
                    def destFile = new File(brotliDir, "${fileName}.br")

                    params.mode = (fileName.contains(".asm.code") || fileName.contains("asm.framework")) ? Brotli.Mode.TEXT : Brotli.Mode.GENERIC

                    byte[] inBuf = sourceFile.bytes
                    byte[] compressedBuf = new byte[inBuf.size()]
                    BrotliCompressor compressor = new BrotliCompressor()

                    int compressedBytesSize = compressor.compress(params, inBuf, compressedBuf)

                    destFile.withDataOutputStream {
                        it.write(compressedBuf, 0, compressedBytesSize)
                    }
                }
            }
        }

        def assemble = tasks.getByName(BasePlugin.ASSEMBLE_TASK_NAME)
        assemble.dependsOn assembleGzipArtifacts, assembleBrotliArtifacts

        def hashGzipArtifacts = tasks.create("hashGzipArtifacts", Copy, new Action<Copy>() {

            @Override
            void execute(Copy task) {
                task.from gzipDir
                task.into outputDir

                task.rename { String fileName ->
                    def baseName = fileName.substring(0, fileName.indexOf('.'))
                    def extension = fileName.substring(fileName.indexOf('.'), fileName.length())
                    def sha1 = DigestUtils.sha1Hex(new FileInputStream("${gzipDir}/${fileName}"))

                    "$baseName-$sha1$extension"
                }
            }
        })

        hashGzipArtifacts.dependsOn assembleGzipArtifacts

        def hashBrotliArtifacts = tasks.create("hashBrotliArtifacts", Copy, new Action<Copy>() {
            @Override
            void execute(Copy task) {
                task.from brotliDir
                task.into outputDir

                task.rename { String fileName ->
                    def baseName = fileName.substring(0, fileName.indexOf('.'));
                    def extension = fileName.substring(fileName.indexOf('.'), fileName.length());
                    def sha1 = DigestUtils.sha1Hex(new FileInputStream("${brotliDir}/${fileName}"));

                    "$baseName-$sha1$extension"
                }
            }
        })

        hashBrotliArtifacts.dependsOn assembleBrotliArtifacts

        def createGzipManifest = tasks.create("createGzipManifest") {
            dependsOn hashGzipArtifacts
            description "creates the gzip manifest"
            group 'webGL'

            doLast {
                createManifest(outputDir, '.gz', "${outputDir}/manifest.json")
            }
        }

        def createBrotliManifest = tasks.create("createBrotliManifest") {
            dependsOn hashBrotliArtifacts
            description "creates the brotli manifest"
            group 'webGL'

            doLast {
                createManifest(outputDir, '.br', "${outputDir}/manifest-brotli.json")
            }
        }

        def createManifests = tasks.create("createManifests") {
            dependsOn createGzipManifest, createBrotliManifest
            description "creates the manifest"
            group 'webGL'
        }

        def publishWebGL = tasks.create("publishWebGL") {
            dependsOn createManifests

            description "publish webGL artifacts to S3"
            group PublishingPlugin.PUBLISH_TASK_GROUP


            doLast {
                publishS3(outputDir, '*.unityweb.gz', 'public,max-age=2592000,immutable --expires 2034-01-01T00:00:00Z', 'application/octet-stream', 'gzip')
                publishS3(outputDir, '*.unityweb.br', 'public,max-age=2592000,immutable --expires 2034-01-01T00:00:00Z', 'application/octet-stream', 'br')
                publishS3(outputDir, 'manifest.json', 'public,max-age=60', 'application/json')
                publishS3(outputDir, 'manifest-brotli.json', 'public,max-age=60', 'application/json')
            }
        }

        def publish = tasks.getByName(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME)
        publish.dependsOn publishWebGL
    }

    def publishS3(outputDir, includeFilter, cacheControl, contentType, contentEncoding = null) {
        project.exec {
            workingDir outputDir
            environment "AWS_CONFIG_FILE", "${project.projectDir}/aws.config"
            executable "aws"
            args "s3", "cp", ".", "${extension.s3Endpoint.path}/${extension.cdnReleaseDirectory}"
            args "--recursive"
            args "--exclude", "*"
            args "--include", "$includeFilter"
            args "--cache-control", "$cacheControl"
            args "--content-type", "$contentType"

            if (contentEncoding != null) {
                args "--content-encoding", "$contentEncoding"
            }
        }
    }

    def createManifest(outputDir, fileEnding, outputFilePath) {
        def assets = project.fileTree(outputDir).filter { f -> f.name.endsWith(fileEnding) }.files.collect { f -> f.toString() }

        project.file(outputFilePath).parentFile.mkdirs()

        def supportedGraphicsAPIs = []
        if (extension.webGLSupportsES2) {
            supportedGraphicsAPIs.add('WebGL 1.0')
        }

        if (extension.webGLSupportsES2) {
            supportedGraphicsAPIs.add('WebGL 2.0')
        }

        def json = JsonOutput.toJson([
                TOTAL_MEMORY          : extension.webGLMemorySize * 1024 * 1024,
                graphicsAPI           : supportedGraphicsAPIs,
                webglContextAttributes: [preserveDrawingBuffer: false],
                splashScreenStyle     : 'Dark',
                backgroundColor       : '#231F20',
                dataUrl               : getAssetUrl(assets, ".data.unityweb${fileEnding}"),
                dataSize              : getAssetSize(assets, ".data.unityweb${fileEnding}"),
                asmCodeUrl            : getAssetUrl(assets, ".asm.code.unityweb${fileEnding}"),
                asmCodeSize           : getAssetSize(assets, ".asm.code.unityweb${fileEnding}"),
                asmMemoryUrl          : getAssetUrl(assets, ".asm.memory.unityweb${fileEnding}"),
                asmMemorySize         : getAssetSize(assets, ".asm.memory.unityweb${fileEnding}"),
                asmFrameworkUrl       : getAssetUrl(assets, ".asm.framework.unityweb${fileEnding}"),
                asmFrameworkSize      : getAssetSize(assets, ".asm.framework.unityweb${fileEnding}"),
                asmSymbolsUrl         : getAssetUrl(assets, ".asm.symbols.unityweb${fileEnding}"),
                asmSymbolsSize        : getAssetSize(assets, ".asm.symbols.unityweb${fileEnding}"),
                wasmCodeUrl           : getAssetUrl(assets, ".wasm.code.unityweb${fileEnding}"),
                wasmCodeSize          : getAssetSize(assets, ".wasm.code.unityweb${fileEnding}"),
                wasmFrameworkUrl      : getAssetUrl(assets, ".wasm.framework.unityweb${fileEnding}"),
                wasmFrameworkSize     : getAssetSize(assets, ".wasm.framework.unityweb${fileEnding}"),
                wasmSymbolsUrl        : getAssetUrl(assets, ".wasm.symbols.unityweb${fileEnding}"),
                wasmSymbolsSize       : getAssetSize(assets, ".wasm.symbols.unityweb${fileEnding}")
        ])

        new File(outputFilePath).text = JsonOutput.prettyPrint(json)
    }

    def getAssetUrl(assets, fileExtension) {
        def filePath = assets.find { f -> f.endsWith(fileExtension) }
        def baseUrl = "${extension.cdnEndpoint.path}/${extension.cdnReleaseDirectory}"

        return filePath == null
                ? ''
                : "${baseUrl}/${new File(filePath).getName()}"
    }

    def getAssetSize(assets, fileExtension) {
        def filePath = assets.find { f -> f.endsWith(fileExtension) };

        return filePath == null
                ? 0
                : new File(filePath).size()
    }
}
