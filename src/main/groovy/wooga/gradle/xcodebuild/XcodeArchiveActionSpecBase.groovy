package wooga.gradle.xcodebuild

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

interface XcodeArchiveActionSpecBase<T extends XcodeArchiveActionSpecBase> {
    Property<String> getArchiveName()

    void setArchiveName(String value)
    void setArchiveName(Provider<String> value)

    T archiveName(String value)
    T archiveName(Provider<String> value)

    Property<String> getBaseName()

    void setBaseName(String value)
    void setBaseName(Provider<String> value)

    T baseName(String value)
    T baseName(Provider<String> value)

    Property<String> getAppendix()

    void setAppendix(String value)
    void setAppendix(Provider<String> value)

    T appendix(String value)
    T appendix(Provider<String> value)

    Property<String> getVersion()

    void setVersion(String value)
    void setVersion(Provider<String> value)

    T version(String value)
    T version(Provider<String> value)

    Property<String> getExtension()

    void setExtension(String value)
    void setExtension(Provider<String> value)

    T extension(String value)
    T extension(Provider<String> value)

    Property<String> getClassifier()

    void setClassifier(String value)
    void setClassifier(Provider<String> value)

    T classifier(String value)
    T classifier(Provider<String> value)

    DirectoryProperty getDestinationDir()

    void setDestinationDir(File value)
    void setDestinationDir(Provider<Directory> value)

    T destinationDir(File value)
    T destinationDir(Provider<Directory> value)
}
