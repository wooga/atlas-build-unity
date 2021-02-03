package wooga.gradle.macOS.security.tasks

import com.wooga.security.Domain
import com.wooga.security.MacOsKeychainSearchList
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

abstract class AbstractSecurityKeychainSearchListTask<T extends AbstractSecurityKeychainSearchListTask> extends DefaultTask {
    final Provider<MacOsKeychainSearchList> searchListProvider

    MacOsKeychainSearchList getSearchList() {
        searchListProvider.get()
    }

    @Optional
    @Input
    final Property<Domain> domain

    void setDomain(Domain value) {
        domain.set(value)
    }

    void setDomain(String value) {
        domain.set(Domain.valueOf(value))
    }

    void setDomain(Provider<Domain> value) {
        domain.set(value)
    }

    T domain(Domain value) {
        setDomain(value)
        this as T
    }

    T domain(String value) {
        setDomain(Domain.valueOf(value))
    }

    T domain(Provider<Domain> value) {
        setDomain(value)
        this as T
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
