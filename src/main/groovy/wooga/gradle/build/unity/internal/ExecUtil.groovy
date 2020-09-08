package wooga.gradle.build.unity.internal

class ExecUtil {
    /**
     * Finds path to executable in PATH.
     *
     * This function is aimed to make the whole task testable.
     * The tests can override the PATH environment variable and
     * point to a mock executable.
     *
     * @param executableName the name of the executable to find in PATH
     * @return path to executable or executableName
     */
    static String getExecutable(String executableName) {
        def path = System.getenv("PATH").split(File.pathSeparator)
                .collect {path -> new File(path, executableName)}
                .find {path -> path.exists() && path.isFile() && path.canExecute()}
        path? path.path : executableName
    }
}
