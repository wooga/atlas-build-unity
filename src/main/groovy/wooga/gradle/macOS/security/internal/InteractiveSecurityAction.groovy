package wooga.gradle.macOS.security.internal

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.gradle.process.internal.ExecException

import java.nio.charset.StandardCharsets
import java.util.logging.Logger

class InteractiveSecurityAction {
    Logger logger = Logger.getLogger(InteractiveSecurityAction.name)

    final Project project
    final List<String> commands
    final File tempLockFile

    InteractiveSecurityAction(Project project, List<String> commands, File tempLockFile) {
        this.project = project
        this.commands = commands
        this.tempLockFile = tempLockFile
    }

    ExecResult exec() {
        logger.info("Run security tasks:")
        logger.info(commands.join("\n").replaceAll("-([p|P]) '.*'") { _, flag ->
            "-${flag} ****"
        })

        def stdout = new ByteArrayOutputStream()
        def stderr = new ByteArrayOutputStream()

        def execResult = project.exec(new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec execSpec) {
                execSpec.executable "security"
                execSpec.args "-i"
                execSpec.standardInput = new ByteArrayInputStream(commands.join("\n").getBytes(StandardCharsets.UTF_8))
                execSpec.ignoreExitValue = true
                execSpec.standardOutput = stdout
                execSpec.errorOutput = stderr
            }
        })

        logger.info(stdout.toString())
        if (execResult.exitValue != 0) {
            logger.error(stderr.toString())
            throw new ExecException(stderr.toString())
        }

        if (tempLockFile.exists()) {
            tempLockFile.deleteOnExit()
            tempLockFile.delete()
        }
        execResult
    }
}
