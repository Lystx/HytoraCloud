package cloud.hytora.driver.player;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.commands.sender.PlayerCommandSender;
import cloud.hytora.driver.common.ICopyableObject;
import cloud.hytora.driver.exception.ModuleNeededException;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.driver.player.connection.PlayerConnection;
import cloud.hytora.driver.services.ICloudServer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ICloudPlayer extends CloudOfflinePlayer, PlayerCommandSender, ICopyableObject<ICloudPlayer> {

    /**
     * The current Proxy-Server this player is currently on
     * Might never be null because a player is always on a proxy
     * but not always on a server (e.g. when switching servers)
     *
     * @return server instance
     */
    @Nonnull
    ICloudServer getProxyServer();

    Task<ICloudServer> getProxyServerAsync();

    /**
     * The current Sub-Server this player is currently on
     * Might be null because when switching servers or because
     * of other reasons or complications
     *
     * @return server instance or null
     */
    @Nullable
    ICloudServer getServer();

    Task<ICloudServer> getServerAsync();


    /**
     * Public method to override the proxy server of this player
     *
     * @param service the service to set
     */
    void setProxyServer(@NotNull ICloudServer service);

    /**
     * Public method to override the sub server of this player
     *
     * @param service the service to set
     */
    void setServer(@NotNull ICloudServer service);

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
    void update();

    /**
     * Tries to get the {@link PermissionPlayer} of this player
     *
     * @throws ModuleNeededException if the perms-module is not loaded
     */
    @Nonnull
    PermissionPlayer asPermissionPlayer() throws ModuleNeededException;

}
