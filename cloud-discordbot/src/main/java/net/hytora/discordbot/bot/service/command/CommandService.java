package net.hytora.discordbot.bot.service.command;

import cloud.hytora.document.Document;
import net.hytora.discordbot.api.internal.IBotService;
import net.hytora.discordbot.api.wrapped.DiscordListener;
import net.hytora.discordbot.api.wrapped.DiscordServer;
import net.hytora.discordbot.bot.HytoraDiscordBot;
import net.hytora.discordbot.bot.service.command.impl.MemeCommand;
import net.hytora.discordbot.bot.service.command.listener.CommandListener;

import java.util.Collection;

public class CommandService implements IBotService {

    public CommandService() {
        HytoraDiscordBot bot = HytoraDiscordBot.getInstance();

        //registering commands
        bot.getCommandManager().registerCommands(new MemeCommand());
    }

    @Override
    public void addListeners(Collection<DiscordListener<?>> listeners) {
        listeners.add(new CommandListener());
    }

    @Override
    public String getIdentifier() {
        return "commands";
    }

    @Override
    public Document handleSetup(DiscordServer server) {
        return null;
    }

    @Override
    public void handleConfigLoad(Document document) {
    }
}
