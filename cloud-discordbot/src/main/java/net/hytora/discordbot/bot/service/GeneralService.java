package net.hytora.discordbot.bot.service;

import cloud.hytora.document.Document;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.hytora.discordbot.api.internal.IBotService;
import net.hytora.discordbot.api.wrapped.DiscordListener;
import net.hytora.discordbot.api.wrapped.DiscordServer;

import java.util.Collection;

public class GeneralService implements IBotService {


    private String managementCategoryId;
    private String adminChannelId;

    @Override
    public void addListeners(Collection<DiscordListener<?>> listeners) {
    }

    @Override
    public String getIdentifier() {
        return "general";
    }

    @Override
    public Document handleSetup(DiscordServer server) {


        Category category = server.createCategoryAsync("\uD83D\uDCBB Staff \uD83D\uDCBB").syncUninterruptedly().get();
        TextChannel channel = server.createTextChannelAsync("bot-managing", category).syncUninterruptedly().get();


        return Document.newJsonDocument(
                "managementCategoryId", category.getId(),
                "adminChannelId", channel.getId()
        );
    }

    @Override
    public void handleConfigLoad(Document document) {
        this.managementCategoryId = document.getString("managementCategoryId");
        this.adminChannelId = document.getString("adminChannelId");
    }
}
