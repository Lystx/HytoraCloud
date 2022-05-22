package cloud.hytora.driver.player;

import cloud.hytora.document.Document;
import cloud.hytora.driver.common.IdentityHolder;
import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.player.connection.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public interface CloudOfflinePlayer extends Bufferable, IdentityHolder {

    /**
     * The cached name of this offline player entry
     * (Note that names might have changed between log-ins)
     */
    @NotNull
    String getName();

    /**
     * Overrides the name of this cached player entry
     * because the name might have changed over time
     * to keep correct offline player entries
     *
     * @param name the name of this player entry
     */
    void setName(@Nonnull String name);

    /**
     * The cached uuid of this offline player entry
     */
    @NotNull
    UUID getUniqueId();

    /**
     * The permanent properties of this player
     * That are stored as {@link Document}
     *
     * @see Document
     */
    @NotNull
    Document getProperties();

    /**
     * Overrides the properties to keep track of correct data
     *
     * @param properties the properties to set
     */
    void setProperties(@Nonnull Document properties);

    /**
     * The last {@link PlayerConnection} that was registered under this player
     * when he left the network for the last time
     *
     */
    @NotNull
    PlayerConnection getLastConnection();

    /**
     * Overrides the last registered connection to keep
     * track of correct data
     *
     * @param connection the connection to set
     */
    void setLastConnection(@Nonnull PlayerConnection connection);

    /**
     * The time as long (date in millis) when this player has
     * logged in for the first time on this network
     */
    long getFirstLogin();

    /**
     * Sets the first login of this player when joining
     * (Only modify if you know what you're doing)
     *
     * @param time the time to set
     */
    void setFirstLogin(long time);

    /**
     * The time as long (date in millis) when the player
     * lastly joined the network
     */
    long getLastLogin();

    /**
     * Sets the last login of this player when joining
     * (Only modify if you know what you're doing)
     *
     * @param time the time to set
     */
    void setLastLogin(long time);

}
