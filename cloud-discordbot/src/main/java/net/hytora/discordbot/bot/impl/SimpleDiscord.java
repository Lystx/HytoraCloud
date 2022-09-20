package net.hytora.discordbot.bot.impl;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.hytora.discordbot.api.wrapped.Discord;
import net.hytora.discordbot.api.wrapped.DiscordServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor
public class SimpleDiscord implements Discord {

    private final JDA wrapped;

    @Override
    public void login() throws Exception {

    }

    @Override
    public void shutdown() {
        wrapped.shutdownNow();
    }

    @Override
    public @NotNull String getProvidedToken() {
        return wrapped.getToken();
    }

    @Override
    public @NotNull Collection<DiscordServer> getRunningServers() {
        return wrapped.getGuilds().stream().map(SimpleDiscordServer::new).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public DiscordServer getRunningServer(String id) {
        return new SimpleDiscordServer(wrapped.getGuildById(id));
    }

    @Override
    public DiscordServer getFirstServer() {
        return getRunningServers().stream().findFirst().orElse(null);
    }

    @Override
    public @NotNull User getBotUser() {
        return wrapped.getSelfUser();
    }
}
