package net.hytora.discordbot.bot.service.welcome.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.hytora.discordbot.api.wrapped.DiscordListener;
import net.hytora.discordbot.api.wrapped.DiscordServer;
import net.hytora.discordbot.bot.HytoraDiscordBot;
import net.hytora.discordbot.bot.service.rules.RuleService;
import net.hytora.discordbot.bot.service.welcome.WelcomeService;

import java.awt.*;

public class JoinListener implements DiscordListener<GuildMemberJoinEvent> {

    @Override
    public void handleEvent(GuildMemberJoinEvent event) {
        Member member = event.getMember();

        WelcomeService ser = HytoraDiscordBot.getInstance().getService("welcome");
        String welcomeChannelId = ser.getWelcomeChannelId();

        DiscordServer server = discord().getRunningServer(event.getGuild().getId());

        if (server == null) {
            return;
        }

        TextChannel textChannelById = server.wrapped().getTextChannelById(welcomeChannelId);

        if (textChannelById == null) {
            return;
        }

        RuleService ruleService = HytoraDiscordBot.getInstance().getService("rules");
        TextChannel rulesChannel = event.getGuild().getTextChannelById(ruleService.getChannelId());

        textChannelById.sendMessage(
                MessageCreateData.fromEmbeds(
                        new EmbedBuilder()
                                .setTitle("HytoraCloud | Welcome")
                                .setColor(Color.CYAN)
                                .setDescription(member.getUser().getAsMention() + " has joined the Discord!" +
                                        "\nPlease read our rules in " + rulesChannel.getAsMention() + "!")
                                .setFooter("Requested by " + member.getUser().getAsTag(), member.getUser().getEffectiveAvatarUrl())
                                .setImage("https://i.imgur.com/Mde2rcA.png")
                                .build()
                )
        ).queue(message -> {
            message.addReaction(Emoji.fromFormatted("⬆️")).queue();
            message.addReaction(Emoji.fromFormatted("⬇️")).queue();
        });
    }
}
