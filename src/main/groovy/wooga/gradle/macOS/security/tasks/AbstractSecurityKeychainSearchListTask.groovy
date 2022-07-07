package wooga.gradle.macOS.security.tasks

import com.wooga.security.Domain
import com.wooga.security.MacOsKeychainSearchList
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

abstract class AbstractSecurityKeychainSearchListTask<T extends AbstractSecurityKeychainSearchListTask> extends DefaultTask {

    private final Provider<MacOsKeychainSearchList> searchListProvider

    @Internal
    MacOsKeychainSearchList getSearchList() {
        searchListProvider.get()
    }

    private final Property<Domain> domain

    @Optional
    @Input
    Property<Domain> getDomain() {
        domain
    }

    void setDomain(Domain value) {
        domain.set(value)
    }

    void setDomain(String value) {
        domain.set(Domain.valueOf(value))
    }

    void setDomain(Provider<Domain> value) {
        domain.set(value)
    }

    AbstractSecurityKeychainSearchListTask() {
        this.domain = project.objects.property(Domain)
        this.searchListProvider = project.provider({
            if (domain.isPresent()) {
                return new MacOsKeychainSearchList(domain.get())
            }
            new MacOsKeychainSearchList()
        })
    }
}
