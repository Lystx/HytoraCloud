package net.hytora.discordbot.api.wrapped;

import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * A wrapper for the discord jda to access the features more easily
 *
 * @author Lystx
 * @since SNAPSHOT-1.0
 */
public interface Discord {

    /**
     * Logs in the current driver
     */
    void login() throws Exception;

    /**
     * Shuts down the discord api
     */
    void shutdown();

    /**
     * The token that was provided for this api instance
     */
    @NotNull
    String getProvidedToken();

    /**
     * Retrieves a {@link Collection} of all {@link DiscordServer}s
     * that run this bot
     *
     * @see DiscordServer
     */
    @NotNull
    Collection<DiscordServer> getRunningServers();

    /**
     * Returns a {@link DiscordServer} with the provided id
     *
     * @param id the id to check
     * @return instance or null
     */
    @Nullable
    DiscordServer getRunningServer(String id);

    /**
     * Returns the first {@link DiscordServer} found
     */
    DiscordServer getFirstServer();

    /**
     * The user instance of the bot instance
     */
    @NotNull
    User getBotUser();
}
