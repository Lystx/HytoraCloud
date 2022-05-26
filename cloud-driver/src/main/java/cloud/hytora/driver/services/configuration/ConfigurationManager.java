package cloud.hytora.driver.services.configuration;

import cloud.hytora.driver.services.configuration.bundle.ConfigurationParent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface ConfigurationManager {

    Collection<ConfigurationParent> getAllParentConfigurations();

    void setAllParentConfigurations(Collection<ConfigurationParent> parents);

    void addParentConfiguration(@NotNull ConfigurationParent serviceGroup);

    void removeParentConfiguration(@NotNull ConfigurationParent serviceGroup);

    default @NotNull Optional<ConfigurationParent> getParentByName(@NotNull String name) {
        return this.getAllParentConfigurations().stream().filter(it -> it.getName().equalsIgnoreCase(name)).findAny();
    }

    default @Nullable ConfigurationParent getParentByNameOrNull(@NotNull String name) {
        return this.getParentByName(name).orElse(null);
    }

    /**
     * gets all cached service groups
     * @return the cached service groups
     */
    @NotNull Collection<ServerConfiguration> getAllCachedConfigurations();

    void setAllCachedConfigurations(Collection<ServerConfiguration> groups);

    /**
     * adds a service group
     * @param serviceGroup the service group to add
     */
    void addConfiguration(@NotNull ServerConfiguration serviceGroup);

    /**
     * removes a service group
     * @param serviceGroup the service group to remove
     */
    void removeConfiguration(@NotNull ServerConfiguration serviceGroup);

    /**
     * gets a service group
     * @param name the name of the service group
     * @return the service group in an optional
     */
    default @NotNull Optional<ServerConfiguration> getConfigurationByName(@NotNull String name) {
        return this.getAllCachedConfigurations().stream().filter(it -> it.getName().equalsIgnoreCase(name)).findAny();
    }

    /**
     * gets a service group
     * @param name the name of the service group
     * @return the service group or null when it not exists
     */
    default @Nullable ServerConfiguration getConfigurationByNameOrNull(@NotNull String name) {
        return this.getConfigurationByName(name).orElse(null);
    }

    /**
     * gets all service groups by node
     * @param node the node
     * @return all services of the node
     */
    default @NotNull List<ServerConfiguration> getConfigurations(@NotNull String node) {
        return this.getAllCachedConfigurations().stream()
            .filter(it -> it.getNode().equalsIgnoreCase(node))
            .collect(Collectors.toList());
    }

    /**
     * update a group
     * @param serviceGroup the group to update
     */
    void update(@NotNull ServerConfiguration serviceGroup);

}
