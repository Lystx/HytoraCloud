package net.hytora.discordbot.bot.service.welcome;

import cloud.hytora.document.Document;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.hytora.discordbot.api.internal.IBotService;
import net.hytora.discordbot.api.wrapped.DiscordListener;
import net.hytora.discordbot.api.wrapped.DiscordServer;
import net.hytora.discordbot.bot.service.welcome.listener.JoinListener;

import java.util.Collection;

@Getter
public class WelcomeService implements IBotService {

    private String welcomeCategoryId, welcomeChannelId;

    @Override
    public void addListeners(Collection<DiscordListener<?>> listeners) {
        listeners.add(new JoinListener());
    }

    @Override
    public String getIdentifier() {
        return "welcome";
    }

    @Override
    public Document handleSetup(DiscordServer server) {

        Category category = server.createCategoryAsync("\uD83C\uDF10 Community \uD83C\uDF10").syncUninterruptedly().get();
        TextChannel textChannel = server.createTextChannelAsync("entrance-hall", category).syncUninterruptedly().get();
        TextChannel generalChannel = server.createTextChannelAsync("on-topic", category).syncUninterruptedly().get();
        TextChannel offTopicChannel = server.createTextChannelAsync("off-topic", category).syncUninterruptedly().get();

        return Document.newJsonDocument(
                "welcomeCategoryId", category.getId(),
                "welcomeChannelId", textChannel.getId(),
                "generalChannelId", generalChannel.getId(),
                "offTopicChannelId", offTopicChannel.getId()
        );
    }

    @Override
    public void handleConfigLoad(Document document) {
        welcomeCategoryId = document.getString("welcomeCategoryId");
        welcomeChannelId = document.getString("welcomeChannelId");
    }
}
