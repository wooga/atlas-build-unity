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

package net.wooga.test.xcode;

import org.apache.commons.io.FileUtils;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class XcodeTestProject extends ExternalResource {

    @Override
    protected void before() throws Throwable {
        super.before();
        unzip();
    }

    @Override
    protected void after() {
        super.after();

        try {
            cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return super.apply(base, description);
    }

    private boolean started;

    private final List<File> fileList;
    private File projectDir;

    public File getProjectDir() {
        return projectDir;
    }

    public File getXcodeProject() {
        return new File(getProjectDir(), "xcodebuildPluginTest.xcodeproj");
    }

    public File getXcodeWorkspace() {
        return new File(getProjectDir(), "xcodebuildPluginTest.xcworkspace");
    }

    public String getSchemeName() {
        return "xcodebuildPluginTest";
    }

    public void setProjectDir(File projectDir) throws IOException {
        if (this.projectDir != null && projectDir != null && projectDir.exists() && projectDir.isDirectory()) {
            FileUtils.copyDirectory(this.projectDir, projectDir);
        }
        this.projectDir = projectDir;
    }

    public XcodeTestProject(File projectDir) {
        this.projectDir = projectDir;
        this.fileList = new ArrayList<>();
    }

    public XcodeTestProject() throws IOException {
        this.projectDir = Files.createTempDirectory("ProjectGeneratorRuleProject").toFile();
        this.fileList = new ArrayList<>();
    }

    private void unzip() throws IOException {
        URL xcodeDir = XcodeTestProject.class.getResource("/xcodeProject");
        File[] fileList = new File(xcodeDir.getPath()).listFiles();
        if (fileList != null) {
            for (File fileToCopy : fileList) {
                FileUtils.copyToDirectory(fileToCopy, projectDir);
                this.fileList.add(new File(projectDir, fileToCopy.getName()));
            }
        }
    }

    private void cleanup() throws IOException {
        for (File file : fileList) FileUtils.forceDelete(file);
        fileList.clear();
    }
}

