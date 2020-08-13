package wooga.gradle.build.unity.ios.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*

class PodInstallTask extends DefaultTask {
    private Object projectPath

    @Internal
    File getProjectPath() {
        project.files(projectPath).getSingleFile()
    }

    void setProjectPath(Object path) {
        projectPath = path
    }

    XCodeArchiveTask projectPath(Object path) {
        setProjectPath(path)
    }

    @InputFiles
    @SkipWhenEmpty
    protected FileCollection getInputFiles() {
        def podFile = project.file("Podfile")
        def inputFiles = [podFile]

        if(podFile.exists()) {
            inputFiles << project.file( "Podfile.lock")
        }

        project.files(inputFiles.findAll { it.exists() }.toArray())
    }

    @OutputDirectory
    protected getPodsDir() {
        project.file("Pods")
    }

    @OutputDirectory
    File getWorkspace() {
        project.file(getProjectPath().path.replaceAll('xcodeproj', 'xcworkspace'))
    }

    @TaskAction
    protected void install() {
        project.exec {
            executable 'pod'
            args 'install'
        }
    }
}
