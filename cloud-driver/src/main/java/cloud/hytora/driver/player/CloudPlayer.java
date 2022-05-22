package cloud.hytora.driver.player;

import cloud.hytora.document.Document;
import cloud.hytora.driver.common.SelfCloneable;
import cloud.hytora.driver.networking.packets.player.CloudPlayerKickPacket;
import cloud.hytora.driver.networking.packets.player.CloudPlayerSendServicePacket;
import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.player.connection.PlayerConnection;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.services.CloudServer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public interface CloudPlayer extends CloudOfflinePlayer, SelfCloneable<CloudPlayer> {

    /**
     * The current Proxy-Server this player is currently on
     * Might never be null because a player is always on a proxy
     * but not always on a server (e.g. when switching servers)
     *
     * @return server instance
     */
    @Nonnull
    CloudServer getProxyServer();

    /**
     * The current Sub-Server this player is currently on
     * Might be null because when switching servers or because
     * of other reasons or complications
     *
     * @return server instance or null
     */
    @Nullable
    CloudServer getServer();

    /**
     * Public method to override the proxy server of this player
     *
     * @param service the service to set
     */
    void setProxyServer(@NotNull CloudServer service);

    /**
     * Public method to override the sub server of this player
     *
     * @param service the service to set
     */
    void setServer(@NotNull CloudServer service);

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
     * The temporary properties as {@link Document} a player gets
     * when joining
     * These properties will always be deleted when leaving / re-joining
     * the network (don't save anything important in these properties;
     * only temporary stuff)
     */
    @Nonnull
    Document getTemporaryProperties();
}
