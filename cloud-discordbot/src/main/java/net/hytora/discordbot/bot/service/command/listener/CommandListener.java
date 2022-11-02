package net.hytora.discordbot.bot.service.command.listener;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hytora.discordbot.api.wrapped.DiscordListener;
import net.hytora.discordbot.bot.HytoraDiscordBot;
import net.hytora.discordbot.bot.service.command.impl.DiscordCommandContext;
import net.hytora.discordbot.bot.service.command.impl.DiscordCommandSender;

public class CommandListener implements DiscordListener<MessageReceivedEvent> {


    @Override
    public void handleEvent(MessageReceivedEvent event) {
        Member member = event.getMember();
        TextChannel textChannel = event.getChannel().asTextChannel();
        String message = event.getMessage().getContentRaw();

        System.out.println(event.getMessage().is());
        System.out.println(event.getMessage().getContentDisplay());
        System.out.println(event.getMessage().getContentStripped());
        System.out.println(event.getMessage().getType());
        System.out.println("===");
        if (!message.startsWith("!")) {
            return;
        }
        message = message.replaceFirst("!", "");

        DiscordCommandSender sender = new DiscordCommandSender(member, textChannel);

        HytoraDiscordBot.getInstance().getCommandManager().executeCommand(message.split(" "), new DiscordCommandContext(sender));
    }
}
