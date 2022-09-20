package net.hytora.discordbot.bot.impl;

import cloud.hytora.common.task.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.hytora.discordbot.api.wrapped.DiscordServer;
import net.hytora.discordbot.bot.HytoraDiscordBot;
import net.hytora.discordbot.bot.service.rules.RuleService;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor
public class SimpleDiscordServer implements DiscordServer {

    @Getter
    @Accessors(fluent = true)
    private final Guild wrapped;


    @Override
    public String getId() {
        return wrapped.getId();
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public Collection<Member> getMembers() {
        return wrapped.getMembers();
    }

    @Override
    public int getMaxMembers() {
        return wrapped.getMaxMembers();
    }


    @Override
    public Task<Category> createCategoryAsync(String name) {
        Task<Category> task = Task.empty();
        System.out.println("Checking " + name);
        try {
            if (wrapped.getCategories().stream().noneMatch(c -> c.getName().equalsIgnoreCase(name))) {
                wrapped.createCategory(name).queue(task::setResult, task::setFailure);
                System.out.println("CREATED : " + name);
            } else {
                System.out.println("Exists : " + name);
                task.setResult(wrapped.getCategoriesByName(name, true).get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return task;
    }

    @Override
    public Task<TextChannel> createTextChannelAsync(String name, Category category) {
        Task<TextChannel> task = Task.empty();
        TextChannel rulesChannel = null;
        try {
            RuleService ruleService = HytoraDiscordBot.getInstance().getService("rules");
            if (ruleService.getChannelId() != null) {
                rulesChannel = wrapped.getTextChannelById(ruleService.getChannelId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextChannel finalRulesChannel = rulesChannel;
        if (wrapped.getTextChannelsByName(name, true).isEmpty()) {
            wrapped.createTextChannel(name, category).queue(c -> {
                task.setResult(c);
                c.sendMessage(
                        MessageCreateData.fromEmbeds(
                                new EmbedBuilder()
                                        .setTitle("HytoraCloud | Channel-Start")
                                        .setColor(Color.CYAN)
                                        .setDescription(
                                                "This is the beginning of the conversation in this channel"
                                                        + "\n Please consider " + (finalRulesChannel == null ? "our rules." : "the rules in" + finalRulesChannel.getAsMention())
                                                        + "\n And remember to always stay friendly and let people take their time to respond :)"
                                        )
                                        .setFooter("This is an auto-generated message by the bot.")
                                        .build()
                        )
                ).queue();
            }, task::setFailure);
        } else {
            task.setResult(wrapped.getTextChannelsByName(name, true).get(0));
        }
        return task;
    }

    @Override
    public Task<NewsChannel> createNewsChannelAsync(String name, Category category) {
        Task<NewsChannel> task = Task.empty();
        if (wrapped.getNewsChannelsByName(name, true).isEmpty()) {
            wrapped.createNewsChannel(name, category).queue(task::setResult, task::setFailure);
        } else {
            task.setResult(wrapped.getNewsChannelsByName(name, true).get(0));
        }
        return task;
    }

    @Override
    public Task<ForumChannel> createForumChannelAsync(String name, Category category, ForumTagData... tags) {
        Task<ForumChannel> task = Task.empty();
        if (wrapped.getForumChannelsByName(name, true).isEmpty()) {
            wrapped.createForumChannel(name, category).setAvailableTags(Arrays.asList(tags)).queue(task::setResult, task::setFailure);
        } else {
            task.setResult(wrapped.getForumChannelsByName(name, true).get(0));
        }
        return task;
    }
}
