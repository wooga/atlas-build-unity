package wooga.gradle.xcodebuild.tasks

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.util.GUtil
import wooga.gradle.xcodebuild.XcodeArchiveActionSpecBase

abstract class AbstractXcodeArchiveTask extends AbstractXcodeTask implements XcodeArchiveActionSpecBase {
    @Input
    final Property<String> archiveName

    @Override
    void setArchiveName(String value) {
        archiveName.set(value)
    }

    @Override
    void setArchiveName(Provider<String> value) {
        archiveName.set(value)
    }

    @Override
    XcodeArchive archiveName(String value) {
        setArchiveName(value)
        this
    }

    @Override
    XcodeArchive archiveName(Provider<String> value) {
        setArchiveName(value)
        this
    }

    final Property<String> baseName

    @Override
    void setBaseName(String value) {
        baseName.set(value)
    }

    @Override
    void setBaseName(Provider<String> value) {
        baseName.set(value)
    }

    @Override
    XcodeArchive baseName(String value) {
        setBaseName(value)
        this
    }

    @Override
    XcodeArchive baseName(Provider<String> value) {
        setBaseName(value)
        this
    }

    final Property<String> appendix

    @Override
    void setAppendix(String value) {
        appendix.set(value)
    }

    @Override
    void setAppendix(Provider<String> value) {
        appendix.set(value)
    }

    @Override
    XcodeArchive appendix(String value) {
        setAppendix(value)
        this
    }

    @Override
    XcodeArchive appendix(Provider<String> value) {
        setAppendix(value)
        this
    }

    final Property<String> version

    @Override
    void setVersion(String value) {
        version.set(value)
    }

    @Override
    void setVersion(Provider<String> value) {
        version.set(value)
    }

    @Override
    XcodeArchive version(String value) {
        setVersion(value)
        this
    }

    @Override
    XcodeArchive version(Provider<String> value) {
        setVersion(value)
        this
    }


    final Property<String> extension

    @Override
    void setExtension(String value) {
        extension.set(value)
    }

    @Override
    void setExtension(Provider<String> value) {
        extension.set(value)
    }

    @Override
    XcodeArchive extension(String value) {
        setExtension(value)
        this
    }

    @Override
    XcodeArchive extension(Provider<String> value) {
        setExtension(value)
        this
    }

    final Property<String> classifier

    @Override
    void setClassifier(String value) {
        classifier.set(value)
    }

    @Override
    void setClassifier(Provider<String> value) {
        classifier.set(value)
    }

    @Override
    XcodeArchive classifier(String value) {
        setClassifier(value)
        this
    }

    @Override
    XcodeArchive classifier(Provider<String> value) {
        setClassifier(value)
        this
    }

    final DirectoryProperty destinationDir

    @Override
    void setDestinationDir(File value) {
        destinationDir.set(value)
    }

    @Override
    void setDestinationDir(Provider<Directory> value) {
        destinationDir.set(value)
    }

    @Override
    XcodeArchive destinationDir(File value) {
        setDestinationDir(value)
        this
    }

    @Override
    XcodeArchive destinationDir(Provider<Directory> value) {
        setDestinationDir(value)
        this
    }

    AbstractXcodeArchiveTask() {
        baseName = project.objects.property(String)
        appendix = project.objects.property(String)
        version = project.objects.property(String)
        extension = project.objects.property(String)
        classifier = project.objects.property(String)

        archiveName = project.objects.property(String)
        archiveName.set(project.provider({
            String name = baseName.getOrElse("") + maybe(baseName.getOrElse(""), appendix)
            name += maybe(name, version)
            name += maybe(name, classifier)
            name += extension.isPresent() && extension.get() != "" ? "." + extension.get() : ""
            name
        }))

        destinationDir = project.layout.directoryProperty()
    }

    protected static String maybe(String prefix, Provider<String> value) {
        if (value.isPresent() && value.get().size() > 0) {
            if (GUtil.isTrue(prefix)) {
                return "-".concat(value.get())
            } else {
                return value.get()
            }
        }
        return ""
    }
}
