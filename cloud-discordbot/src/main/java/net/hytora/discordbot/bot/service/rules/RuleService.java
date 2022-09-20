package net.hytora.discordbot.bot.service.rules;

import cloud.hytora.document.Document;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.hytora.discordbot.api.internal.IBotService;
import net.hytora.discordbot.api.wrapped.DiscordListener;
import net.hytora.discordbot.api.wrapped.DiscordServer;

import java.awt.*;
import java.util.Collection;

public class RuleService implements IBotService {

    @Getter
    private String categoryId, channelId;

    @Override
    public void addListeners(Collection<DiscordListener<?>> listeners) {}

    @Override
    public String getIdentifier() {
        return "rules";
    }

    @Override
    public Document handleSetup(DiscordServer server) {



        Category category = server.createCategoryAsync("❗ Important ❗").syncUninterruptedly().get();
        TextChannel textChannel = server.createTextChannelAsync("rules", category).syncUninterruptedly().get();
        server.createTextChannelAsync("announcements", category).syncUninterruptedly().get();


        textChannel.sendMessage(
                MessageCreateData.fromEmbeds(
                        new EmbedBuilder()
                                .setTitle("HytoraCloud | Rules")
                                .setDescription(
                                        "These rules can always be changed by an administrator.\n" +
                                                "\n" +
                                                "§1 Behavior\n" +
                                                "- Harassement, homophobia, racism, sexism, discrimination, hate speech, toxicity and trolling are prohibited\n" +
                                                "\n" +
                                                "§2 Voice channels\n" +
                                                "- Every Administrator has the right to join the voice channel you're in; they are also allowed to move people who are in said voice channels\n" +
                                                "- Recordings are only allowed if all of the parties in said voice channel agreed to it\n" +
                                                "\n" +
                                                "§3 Arguing\n" +
                                                "- If two or more parties are arguing, please switch to direct messages because that's not something the public has to see\n" +
                                                "\n" +
                                                "§4 Advertising\n" +
                                                "- Advertising is only allowed, when the thing you're advertising is related to HytoraCloud\n" +
                                                "\n" +
                                                "§5 Private information\n" +
                                                "- Phone numbers, addresses, passwords, etc. should not be shared on this discord server\n" +
                                                "- \"Doxing\" is prohibited and will be punished\n" +
                                                "\n" +
                                                "§6 Names and profile pictures\n" +
                                                "- Impersonation of other people is prohibited\n" +
                                                "- insulting and inappropriate Nicknames are prohibited, they should also not be against other rules\n" +
                                                "\n" +
                                                "§7 Punishments\n" +
                                                "§7.1 Warns -> Bans\n" +
                                                "- Members will be warned; after a few warnings we will temp or perma ban them"
                                )
                                .setColor(Color.RED)
                                .setFooter("This is an automatically generated message.")
                                .build()
                )
        ).queue(message -> message.addReaction(Emoji.fromUnicode("\uD83E\uDD1D")).queue());

        return Document.newJsonDocument(
                "categoryId" , category.getId(),
                "channelId", textChannel.getId()
        );
    }

    @Override
    public void handleConfigLoad(Document document) {
        categoryId = document.getString("categoryId");
        channelId = document.getString("channelId");
    }
}
