package wooga.gradle.macOS.security

import com.wooga.gradle.BaseSpec
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles

trait SecurityMultikeychainOperationSpec extends BaseSpec {
    private final ConfigurableFileCollection keychains = objects.fileCollection()

    @InputFiles
    ConfigurableFileCollection getKeychains() {
        keychains
    }

    void setKeychains(List<File> value) {
        keychains.setFrom(value)
    }

    void setKeychains(Provider<List<RegularFile>> value) {
        keychains.setFrom(value)
    }

    void keychains(Iterable<File> value) {
        keychains.from(providers.provider({ value }))
    }

    void keychains(Provider<Iterable<File>> value) {
        keychains.from(value)
    }

    void keychain(Provider<File> keychain) {
        keychains.from(keychain)
    }

    void keychain(File keychain) {
        keychains.from(keychain)
    }
}
