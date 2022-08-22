package net.hytora.discordbot.commands;

import cloud.hytora.common.scheduler.Scheduler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.hytora.discordbot.HytoraDiscordBot;
import net.hytora.discordbot.manager.command.CommandCategory;
import net.hytora.discordbot.manager.command.CommandHandler;
import net.hytora.discordbot.util.DiscordChat;

public class StopCommand extends CommandHandler {

    public StopCommand(String name, String description, CommandCategory category, String... aliases) {
        super(name, description, category, aliases);
    }


    @Override
    public boolean hasPermission(Member member) {
        return member.hasPermission(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(String[] args, Message raw, Member executor, TextChannel channel) {

        DiscordChat.preset(channel, "Shutdown", HytoraDiscordBot.getHytora().getDiscord().getSelfUser(), message -> {

            Scheduler.runTimeScheduler().scheduleDelayedTask(() -> {
                message.delete().queue(unused -> HytoraDiscordBot.getHytora().shutdown());
            }, 20L);

        }, "HytoraCloud DiscordBot", "will be shut down in", "about 1 second", "and delete this message!");

    }

    @Override
    public void syntax(String command, TextChannel channel, User executor) {

    }
}
