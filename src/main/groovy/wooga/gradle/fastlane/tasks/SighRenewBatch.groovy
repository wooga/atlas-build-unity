package wooga.gradle.fastlane.tasks

import org.gradle.api.file.FileCollection
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

/**
 * Batch version of {@code SighRenew} to import multiple
 * profiles with one call.
 *
 * @see wooga.gradle.fastlane.tasks.SighRenew
 */
class SighRenewBatch extends SighRenew {

    private final MapProperty<String, String> profiles = objects.mapProperty(String, String)

    @Input
    @Optional
    MapProperty<String, String> getProfiles() {
        profiles
    }

    void setProfiles(Map<String, String> value) {
        profiles.set(value)
    }

    void setProfiles(Provider<Map<String, String>> value) {
        profiles.set(value)
    }

    void profiles(Map<String, String> value) {
        profiles.putAll(value)
    }

    void profiles(Provider<Map<String, String>> value) {
        profiles.putAll(value)
    }

    void profiles(String key, String value) {
        profiles.put(key, value)
    }

    void profiles(String key, Provider<String> value) {
        profiles.put(key, value)
    }

    @Override
    protected FileCollection getOutputFiles() {
        def files = profiles.map({ profiles -> profiles.values().collect({ profileName -> destinationDir.file("${profileName}.mobileprovision").get() }) })
        project.files(files)
    }


    SighRenewBatch() {
        fileName.set("some_value")
        setOnlyIf(new Spec<SighRenewBatch>() {
            @Override
            boolean isSatisfiedBy(SighRenewBatch task) {
                (task.teamId.present || task.teamName.present) && task.profiles.present && !profiles.get().isEmpty()
            }
        })
    }

    @TaskAction
    protected importProfiles() {
        def profiles = new HashMap<String, String>()
        profiles.putAll(this.profiles.getOrElse([:]))
        if ((appIdentifier.present && !profiles.containsKey(appIdentifier.get())) && (provisioningName.present && !profiles.containsValue(provisioningName.get()))) {
            logger.info("task appIdentifier and provisioning name not in profiles")
            logger.info("add them to the profiles map")
            profiles.put(appIdentifier.get(), provisioningName.get())
        }

        logger.info("import ${profiles.size()} profiles")
        profiles.each { appId, name ->
            appIdentifier.set(appId)
            provisioningName.set(name)
            fileName.set("${name}.mobileprovision")

            logger.info("import provisioning profile '${name}' for bundleIdentifier '${appId}' to file '${fileName.get()}'")
            exec()
        }
    }
}
