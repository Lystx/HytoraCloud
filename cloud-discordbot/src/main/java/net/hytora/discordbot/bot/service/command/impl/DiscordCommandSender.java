package net.hytora.discordbot.bot.service.command.impl;

import cloud.hytora.driver.commands.sender.CommandSender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
public class DiscordCommandSender implements CommandSender {

    private final Member executor;
    private final TextChannel executionChannel;

    @Override
    public void sendMessage(String message) {
        // TODO: 26.09.2022
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return executor.hasPermission(Permission.valueOf(permission));
    }

    @NotNull
    @Override
    public String getName() {
        return executor.getEffectiveName();
    }
}
