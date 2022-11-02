package net.hytora.discordbot.bot.service.command.impl;

import cloud.hytora.common.util.DisplayFormat;
import cloud.hytora.driver.commands.context.CommandContext;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.UUID;

public class DiscordCommandContext extends CommandContext<DiscordCommandSender> {

    private final UUID senderUniqueId;
    @Getter
    private final TextChannel textChannel;

    public DiscordCommandContext(DiscordCommandSender commandSender) {
        super(commandSender);

        this.senderUniqueId = UUID.randomUUID();
        this.textChannel = commandSender.getExecutionChannel();

        //setting discord properties
        this.setProperty("executor", commandSender.getExecutor());
        this.setProperty("channel", commandSender.getExecutionChannel());
    }

    @Override
    public UUID getSendersUniqueId() {
        return senderUniqueId;
    }

    @Override
    protected void message(String msg, DiscordCommandSender target) {
        target.sendMessage(msg);
    }

    @Override
    public void sendDisplayFormat(DisplayFormat format, DiscordCommandSender... receivers) {

    }
}
