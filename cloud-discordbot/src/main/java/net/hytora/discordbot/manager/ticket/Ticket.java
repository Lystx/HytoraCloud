package net.hytora.discordbot.manager.ticket;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.document.Document;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.hytora.discordbot.HytoraDiscordBot;
import net.hytora.discordbot.util.DiscordChat;
import net.hytora.discordbot.util.button.DiscordButton;
import net.hytora.discordbot.util.button.DiscordButtonAction;

import java.awt.*;
import java.util.function.Consumer;

public class Ticket {

    private final String member;
    private final int id;
    private boolean english;
    private final String claimedBy;


    public Ticket(String member, int id, boolean english, String claimedBy) {
        this.member = member;
        this.id = id;
        this.english = english;
        this.claimedBy = claimedBy;
    }


    public void selectLanguage(boolean english, Message message) {
        this.english = english;

        Document json = HytoraDiscordBot.getHytora().getConfig().getDocument("roles").getDocument("support");

        Role role = HytoraDiscordBot.getHytora().getDiscord().getRolesByName(json.getString("name"), true).get(0);

        if (role == null) {
            Logger.constantInstance().error( "§cCouldn't open ticket because support role is not set!");
            return;
        }

        EmbedBuilder embedBuilder;

        if (english) {
            embedBuilder = DiscordChat.embedBuilder(
                    Color.CYAN,
                    "Ticket | English",
                    HytoraDiscordBot.getHytora().getDiscord().getSelfUser(),
                    "You set the Ticket-language to English",
                    "So our supporters will talk English to you",
                    "And the bot instructions will be in English too!",
                    "\n",
                    "Now please wait for our " + role.getAsMention() + " to see this ticket!"
            );
        } else {
            embedBuilder = DiscordChat.embedBuilder(
                    Color.CYAN,
                    "Ticket | Deutsch",
                    HytoraDiscordBot.getHytora().getDiscord().getSelfUser(),
                    "Du hast die Ticket-Sprache zu Deutsch geändert",
                    "Also werden unsere Supporter mit dir deutsch sprechen",
                    "und die Anweisungen des Tickets werden auch deutsch sein!",
                    "\n",
                    "Nun warte bitte auf unseren " + role.getAsMention() + ", bis dieses Ticket gesehen wird!"
            );
        }
        message.editMessage(embedBuilder.build()).setActionRow(
                new DiscordButton(0x47, english ? "Close" : "Schliessen", ButtonStyle.DANGER, new Consumer<DiscordButtonAction>() {
                    @Override
                    public void accept(DiscordButtonAction a) {
                        HytoraDiscordBot.getHytora().getTicketManager().closeTicket(Ticket.this);
                    }
                }).submit(),
                new DiscordButton(0x48, english ? "Claim" : "Beanspruchen", ButtonStyle.PRIMARY, new Consumer<DiscordButtonAction>() {
                    @Override
                    public void accept(DiscordButtonAction a) {
                        HytoraDiscordBot.getHytora().getTicketManager().claimTicket(Ticket.this, a.getUser());
                    }
                }).submit()
                ).queue();
        this.getChannel().sendMessage(role.getAsMention()).queue(message1 -> message1.delete().queue());
    }


    /**
     * Gets the {@link TextChannel} for this ticket
     *
     * @return textchannel
     */
    public TextChannel getChannel() {
        return HytoraDiscordBot.getHytora().getDiscord().getTextChannelsByName("ticket-" + this.id, true).get(0);
    }

    /**
     * Gets the {@link Member} of this {@link Ticket}
     *
     * @return member
     */
    public Member getExecutor() {
        return HytoraDiscordBot.getHytora().getGuild().getMember(HytoraDiscordBot.getHytora().getDiscord().getUserById(member));
    }

    public String getMember() {
        return member;
    }

    public int getId() {
        return id;
    }

    public boolean isEnglish() {
        return english;
    }

    public String getClaimedBy() {
        return claimedBy;
    }
}
