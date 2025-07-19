package cloud.hytora.driver.module.permission;

import cloud.hytora.common.identification.ModifiableNameHolder;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.objects.Identifiable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;


/**
 * A {@link PermissionGroup} is a {@link PermissionEntity} that acts as a parent for {@link PermissionPlayer}s
 * Players can have multiple {@link PermissionGroup}s set to their profile.
 * Groups can hold permissions like players but also declare different values like prefix/suffix/chatcolor for
 * specific {@link PermissionGroup} to differentiate between other groups ingame (different color in chat for example)
 *
 * @author Lystx
 * @since SNAPSHOT-1.5
 * @version STABLE-1.3
 *
 * @see PermissionEntity
 */
public interface PermissionGroup extends PermissionEntity, Identifiable, ModifiableNameHolder {

    /**
     * The plain chat color of this group
     * (e.g. §6)
     */
    @Nonnull
    String getChatColor();

    /**
     * Sets the plain chat color of this group
     *
     * @param chatColor the color to set
     * @see #getChatColor()
     */
    void setChatColor(String chatColor);

    /**
     * The prefix that can be put infront of the name
     * of a player. In chat or in tab
     */
    @Nonnull
    String getPrefix();

    /**
     * Sets the prefix of this group
     *
     * @param prefix the prefix to set
     * @see #getPrefix()
     */
    void setPrefix(String prefix);

    /**
     * The suffix that can be put behind the name
     * of a player. In chat or in tab
     */
    @Nonnull
    String getSuffix();

    /**
     * Sets the suffix of this group
     *
     * @param suffix the suffix to set
     * @see #getSuffix()
     */
    void setSuffix(String suffix);

    /**
     * The sort id of this group
     * The lower the Id, the higher the rating
     */
    int getSortId();

    /**
     * Sets the sort id of this group
     *
     * @param sortId the id to set
     * @see #getSortId()
     */
    void setSortId(int sortId);

    /**
     * If this group is a default group and will be added
     * to new players that join the network
     */
    boolean isDefaultGroup();

    /**
     * Sets if this group is a default group
     *
     * @param defaultGroup the value
     * @see #isDefaultGroup()
     */
    void setDefaultGroup(boolean defaultGroup);

    /**
     * Returns the inherited group names that this
     * group has (also inheriting permissions etc)
     */
    @Nonnull
    Collection<String> getInheritedGroups();

    /**
     * Adds a group-name that this group inherits its data from
     *
     * @param group the group to add
     * @see #getInheritedGroups()
     */
    void addInheritedGroup(@Nonnull String group);

    /**
     * Removes a group-name that this group inherits its data from
     *
     * @param group the group to remove
     * @see #getInheritedGroups()
     */
    void removeInheritedGroup(@Nonnull String group);

    /**
     * Finds all inherited {@link PermissionGroup} instances
     * from the provided group-names that inherit
     *
     * @return collection with found instances
     */
    @Nonnull
    Collection<PermissionGroup> findInheritedGroups();

}
