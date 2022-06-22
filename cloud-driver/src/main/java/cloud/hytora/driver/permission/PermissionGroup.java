package cloud.hytora.driver.permission;

import cloud.hytora.driver.CloudDriver;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public interface PermissionGroup extends PermissionEntity {

    @Nonnull
    String getName();

    @Nonnull
    String getColor();

    @Nonnull
    String getChatColor();

    @Nonnull
    String getNamePrefix();

    @Nonnull
    String getPrefix();

    @Nonnull
    String getSuffix();

    int getSortId();

    boolean isDefaultGroup();

    @Nonnull
    Collection<String> getInheritedGroups();

    void addInheritedGroup(@Nonnull String group);

    void removeInheritedGroup(@Nonnull String group);

    @Nonnull
    default Collection<PermissionGroup> findInheritedGroups() {
        return Collections.unmodifiableCollection(
                getInheritedGroups().stream()
                        .map(CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class)::getPermissionGroupByNameOrNull).collect(Collectors.toList())
        );
    }

}
