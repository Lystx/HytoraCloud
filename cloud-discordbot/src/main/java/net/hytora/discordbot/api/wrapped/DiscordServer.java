package net.hytora.discordbot.api.wrapped;

import cloud.hytora.common.task.Task;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagData;

import java.util.Collection;

/**
 * A wrapper for the discord jda "Guild" that sounds more logical and is more
 * easy to use than the official api
 *
 * @see Discord
 * @author Lystx
 * @since SNAPSHOT-1.0
 */
public interface DiscordServer {

    Guild wrapped();

    /**
     * The developer id of the server
     */
    String getId();

    /**
     * The name of the guild
     */
    String getName();

    /**
     * The users of this guild
     */
    Collection<Member> getMembers();

    /**
     * The maximum amount of members allowed on this guild
     */
    int getMaxMembers();

    Task<Category> createCategoryAsync(String name);

    Task<TextChannel> createTextChannelAsync(String name, Category category);

    Task<ForumChannel> createForumChannelAsync(String name, Category category, ForumTagData... tags);

    Task<NewsChannel> createNewsChannelAsync(String name, Category category);
}
