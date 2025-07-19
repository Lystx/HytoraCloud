package cloud.hytora.driver.entity.player;

import cloud.hytora.common.location.impl.CloudLocation;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.command.sender.PlayerCommandSender;
import cloud.hytora.driver.common.exception.IncompatibleDriverEnvironmentException;
import cloud.hytora.driver.common.objects.Cloneable;
import cloud.hytora.driver.common.exception.ModuleNeededException;
import cloud.hytora.driver.entity.player.connection.PlayerConnection;
import cloud.hytora.driver.entity.player.extension.CloudBukkitPlayer;
import cloud.hytora.driver.entity.player.extension.CloudProxyPlayer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.module.permission.PermissionGroup;
import cloud.hytora.driver.module.permission.PermissionManager;
import cloud.hytora.driver.module.permission.PermissionPlayer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * CloudPlayer objects are used to manage online players on the whole network.
 * The CloudPlayer does not differentiate between proxy or spigot. You can access the players' data on the
 * whole cloudNetwork and even perform the same actions across the whole network
 *
 * Players of this class are always online players and can perform various actions that are only
 * provided for online players.
 * All other offline actions can be found in the {@link CloudOfflinePlayer}
 * CloudPlayers can be updated using {@link CloudPlayer#update(PublishingType...)}
 *
 * @author Lystx
 * @since DEV-1.0
 * @see CloudOfflinePlayer
 */
public interface CloudPlayer extends CloudOfflinePlayer, PlayerCommandSender, Cloneable<CloudPlayer> {

    /**
     * The current Proxy-Server this player is currently on
     * Might never be null because a player is always on a proxy
     * but not always on a server (e.g. when switching servers)
     *
     * @return server instance
     */
    @NotNull
    CloudService getProxyServer();

    /**
     * Checks if this player is fully connected
     * That means {@link CloudPlayer#getProxyServer()}
     * and {@link CloudPlayer#getServer()} must be set
     *
     * @return if online on network
     */
    boolean isConnected();

    /**
     * The current Sub-Server this player is currently on
     * Might be null because when switching servers or because
     * of other reasons or complications
     *
     * @return server instance or null
     */
    CloudService getServer();

    /**
     * Sends a message to this player without provided cloud prefix
     *
     * @param message the message to send
     */
    void sendPlainMessage(String message);

    /**
     * Public method to override the proxy server of this player
     *
     * @param service the service to set
     */
    void setProxyServer(@NotNull CloudService service);

    /**
     * Public method to override the sub server of this player
     *
     * @param service the service to set
     */
    void setServer(CloudService service);

    /**
     * The current {@link PlayerConnection} of this player
     * to get information about the connection of the player
     */
    @Nonnull
    PlayerConnection getConnection();

    /**
     * Sets the connection of this player
     *
     * @param connection the connection to set
     */
    void setConnection(@NotNull PlayerConnection connection);

    /**
     * Updates the player and all its data all over the network
     * inside the whole cluster system (services & nodes)
     */
    void update(PublishingType... type);


    /**
     * Retrieves the highest {@link PermissionGroup} that this player has
     * (Note: this method requires the house-own permissionModule)
     *
     * @throws ModuleNeededException if the perms-module is not loaded
     * @return group instance or default group if none set
     */
    PermissionGroup getHighestPermissionGroup();

    /**
     * To keep the code of the {@link CloudPlayer} clean we transferred all the
     * proxy-related stuff into the {@link CloudProxyPlayer}.
     *
     *
     * Proxy-Stuff can be executed from every Cloud-Instance (also from bukkit-side)
     *
     * @see PlayerExtension
     * @return proxyPlayer instance
     * @throws IncompatibleDriverEnvironmentException if executed on wrong environment
     */
    CloudProxyPlayer asProxyPlayer() throws IncompatibleDriverEnvironmentException;

    /**
     * To keep the code of the {@link CloudPlayer} clean we transferred all the
     * bukkit-related stuff into the {@link CloudBukkitPlayer}.
     *
     *
     * Bukkit-Stuff might not be able to be executed from every Cloud-Instance
     * So be careful !
     *
     * @see PlayerExtension
     * @return bukkitPlayer instance
     * @throws IncompatibleDriverEnvironmentException if executed on wrong environment
     */
    CloudBukkitPlayer asBukkitPlayer() throws IncompatibleDriverEnvironmentException;

}
