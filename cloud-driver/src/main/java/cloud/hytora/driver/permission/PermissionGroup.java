package cloud.hytora.driver.permission;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.IdentityHolder;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public interface PermissionGroup extends PermissionEntity, IdentityHolder {

    @Nonnull
    String getName();

    void setName(String name);

    @Nonnull
    String getColor();

    void setColor(String color);

    @Nonnull
    String getChatColor();

    void setChatColor(String chatColor);

    @Nonnull
    String getNamePrefix();

    void setNamePrefix(String namePrefix);

    @Nonnull
    String getPrefix();

    void setPrefix(String prefix);

    @Nonnull
    String getSuffix();


    void setSuffix(String suffix);

    int getSortId();

    void setSortId(int sortId);

    boolean isDefaultGroup();

    void setDefaultGroup(boolean defaultGroup);

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
