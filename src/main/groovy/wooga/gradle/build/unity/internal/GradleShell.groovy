package wooga.gradle.build.unity.internal

import org.gradle.api.Project
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec

class GradleShell {

    private final Project project

    GradleShell(Project project) {
        this.project = project
    }

    public ExecResult execute(boolean logging=true, Closure execSpecClosure) {
        def stdOut = new ByteArrayOutputStream()
        def stdErr = new ByteArrayOutputStream()
        try {
            def execResult = project.exec { ExecSpec execSpec ->
                execSpec.standardOutput = stdOut
                execSpec.errorOutput = stdErr
                execSpec.ignoreExitValue = true
                execSpecClosure(execSpec)
            }
            stdOut.flush()
            stdErr.flush()
            if(logging) {
                project.logger.info(stdOut.toString())
                project.logger.error(stdErr.toString())
            }
            return execResult
        } finally {
            stdOut.close()
            stdErr.close()
        }
    }


}
