package wooga.gradle.macOS.security

import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

interface InteractiveSecurityActionSpec<T extends InteractiveSecurityActionSpec> {
    Provider<List<String>> getSecurityCommands()
    Provider<RegularFile> getTempLockFile()
}
