package net.hytora.discordbot.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.hytora.discordbot.HytoraDiscordBot;
import net.hytora.discordbot.util.DiscordChat;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        System.out.println("YES");
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        System.out.println(0);
        if (!event.getChannel().getType().equals(ChannelType.TEXT)) {
            System.out.println(1);
            return;
        }

        Message message = event.getMessage();
        User user = event.getAuthor();
        TextChannel channel = event.getChannel();

        System.out.println(event.getMessage().getContentRaw());
        if (event.getMessage().getContentRaw().startsWith(HytoraDiscordBot.getHytora().getCommandManager().getPrefix())) {
            if (!HytoraDiscordBot.getHytora().getCommandManager().execute(true, message.getContentRaw(), channel, user, message)) {

                EmbedBuilder embedBuilder = DiscordChat.embedBuilder(Color.RED, "Managing", user, "This command", "does not exist!");
                channel.sendMessage(embedBuilder.build()).queue(message1 -> message1.delete().queueAfter(2, TimeUnit.SECONDS));
            }
        }

    }
}
