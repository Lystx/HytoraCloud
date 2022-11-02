package net.hytora.discordbot.bot.service.command.impl;

import cloud.hytora.common.misc.StringCreator;
import cloud.hytora.document.Document;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import jdk.nashorn.api.scripting.URLReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;
import java.io.BufferedReader;
import java.net.URL;

public class MemeCommand {

    public static Document CURRENT_SUB_REDDIT = Document.newJsonDocument();

    @Command(
            label = "meme",
            aliases = {"memes"},
            desc = "Shows you a random meme!"
    )
    public void execute(DiscordCommandContext ctx, CommandArguments args) {

        TextChannel channel = ctx.getTextChannel();
        Member executor = ctx.getCommandSender().getExecutor();


        String subReddit = args.getString(0, "");

        try {

            StringCreator stringCreator = new StringCreator();
            BufferedReader bufferedReader = new BufferedReader(new URLReader(new URL("http://meme-api.herokuapp.com/gimme/" + subReddit)));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringCreator.append(line);
            }

            Document jsonObject = Document.newJsonDocument(stringCreator.toString());
            CURRENT_SUB_REDDIT = jsonObject;
            if (jsonObject.getBoolean("nsfw") && !channel.isNSFW()) {
                channel.sendMessage(
                        MessageCreateData.fromEmbeds(
                                new EmbedBuilder()
                                        .setTitle("Memes | Failed")
                                        .setDescription("This channel is not a NSFW-Channel\nSuch pictures can't be shown here\nToggle NSFW-Option for this channel!")
                                        .setImage("https://support.discord.com/hc/article_attachments/360007795191/2_.jpg")
                                        .setFooter("Executor | " + executor.getUser().getAsTag(), executor.getUser().getEffectiveAvatarUrl())
                                        .build()
                        )
                ).queue();
                return;
            }
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setColor(Color.ORANGE)
                    .setImage(jsonObject.getString("url"))
                    .setTitle(jsonObject.getString("title"))
                    .setFooter("Subreddit | " + jsonObject.getString("subreddit"), executor.getUser().getEffectiveAvatarUrl());

            channel.sendMessage(MessageCreateData.fromEmbeds(embedBuilder.build())).queue(message -> {
                message.addReaction(Emoji.fromFormatted("⬆️")).queue();
                message.addReaction(Emoji.fromFormatted("⬇️")).queue();
                message.addReaction(Emoji.fromUnicode("\uD83D\uDD01")).queue();
            });
        } catch (Exception e) {
            channel.sendMessage(
                    MessageCreateData.fromEmbeds(

                            new EmbedBuilder()
                                    .setTitle("Memes | Error")
                                    .setColor(Color.RED)
                                    .setThumbnail("http://www.stochasticgeometry.ie/wp-content/uploads/2010/03/RedditProhibited.png")
                                    .setDescription("Couldn't get a meme from Reddit!")
                                    .setFooter("Executor | " + executor.getUser().getAsTag(), executor.getUser().getEffectiveAvatarUrl())
                                    .build()
                    )
            ).queue();
        }

    }

}
