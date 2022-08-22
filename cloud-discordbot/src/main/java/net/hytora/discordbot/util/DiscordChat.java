package net.hytora.discordbot.util;

import cloud.hytora.common.misc.StringCreator;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.Button;
import net.hytora.discordbot.HytoraDiscordBot;

import java.util.function.Consumer;

@UtilityClass
public class DiscordChat {


    /**
     * Sends a default created preset message
     *
     * @param textChannel the channel to send it in
     * @param title the title
     * @param message the message
     * @param requester the requester
     */
    public void preset(TextChannel textChannel, String title, User requester, String... message) {
        preset(textChannel, title, requester, message1 -> {}, message);
    }
    /**
     * Sends a default created preset message
     *
     * @param textChannel the channel to send it in
     * @param title the title
     * @param message the message
     * @param requester the requester
     */
    public void preset(TextChannel textChannel, String title, User requester, Consumer<Message> consumer, String... message) {
        textChannel.sendMessage(embedBuilder(java.awt.Color.CYAN, title, requester, message).build()).queue(consumer);
    }

    /**
     * Sends a default created preset message
     *
     * @param textChannel the channel to send it in
     * @param title the title
     * @param message the message
     * @param requester the requester
     */
    public void preset(TextChannel textChannel, String title, User requester, Consumer<Message> consumer, Button[] actionRows, String... message) {
        textChannel.sendMessage(embedBuilder(java.awt.Color.CYAN, title, requester, message).build()).setActionRow(actionRows).queue(consumer);
    }

    /**
     * Creates an {@link EmbedBuilder} for the {@link DiscordChat#preset(TextChannel, String, User, String...)} Method
     *
     * @param title the title of the message
     * @param requester the requester
     * @param message the message
     * @return builder
     */
    public EmbedBuilder embedBuilder(java.awt.Color color, String title, User requester, String... message) {
        StringCreator stringCreator = new StringCreator();

        for (String msg : message) {
            stringCreator.append(msg);
        }

       return new EmbedBuilder()
                .setThumbnail(HytoraDiscordBot.getHytora().getGuild().getIconUrl())
                .setTitle("Hytora | " + title)
                .setColor(color)
                .setDescription(stringCreator.create())
                .setFooter("Requested by " + requester.getAsTag(), requester.getEffectiveAvatarUrl());
    }

}
