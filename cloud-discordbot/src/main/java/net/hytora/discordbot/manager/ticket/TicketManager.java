package net.hytora.discordbot.manager.ticket;

import cloud.hytora.common.logging.Logger;
import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.hytora.discordbot.HytoraDiscordBot;
import net.hytora.discordbot.util.DiscordChat;
import net.hytora.discordbot.util.button.DiscordButton;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Getter
public class TicketManager extends ListenerAdapter {

    /**
     * All current tickets
     */
   private final List<Ticket> tickets;

    /**
     * The ticket channel
     */
    private final TextChannel channel;

    /**
     * The ticket category
     */
    private final Category category;

    public TicketManager(String channelId) {
        this.tickets = new ArrayList<>();

        this.channel = HytoraDiscordBot.getHytora().getGuild().getTextChannelById(channelId);
        this.category = channel.getParent();

        this.checkCreator();
        Logger.constantInstance().info("§7Loaded §b" + this.tickets.size() + " §7Opened §3Tickets§8!");
    }

    /**
     * Checks if the open ticket message exists
     */
    private void checkCreator() {

        for (Message message : this.channel.getIterableHistory()) {
            message.delete().queue();
        }

        DiscordChat.preset(
                channel,
                "TicketSystem",
                HytoraDiscordBot.getHytora().getDiscord().getSelfUser(),
                message -> {},
                new Button[]{
                        new DiscordButton(0x099, "Open Ticket", ButtonStyle.SUCCESS, a -> openTicket(new Ticket(a.getUser().getId(), tickets.size() + 1, false, null))).submit()
                },
                "Click here to open a new Ticket"
        );
    }



    /**
     * Opens a new {@link Ticket}
     *
     * @param ticket the ticket
     */
    public void openTicket(Ticket ticket) {

        Member executor = ticket.getExecutor();

        if (this.tickets.stream().filter(t -> t.getId() == ticket.getId()).findFirst().orElse(null) != null) {
            executor.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(DiscordChat.embedBuilder(Color.RED, "TicketSupport", HytoraDiscordBot.getHytora().getDiscord().getSelfUser(), "Please do not try to open", "more than one ticket at the same time!").build()).queue());
            return;
        }

        this.tickets.add(ticket);

        HytoraDiscordBot.getHytora().getGuild().createTextChannel("ticket-" + ticket.getId(), category)
                .addPermissionOverride(executor, EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(HytoraDiscordBot.getHytora().getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .queue(textChannel -> DiscordChat.preset(textChannel, "TicketSystem", executor.getUser(), message -> {
                }, new Button[]{
                        new DiscordButton(0x01, "German", ButtonStyle.PRIMARY, discordButtonAction -> {
                            if (!discordButtonAction.getUser().getId().equalsIgnoreCase(ticket.getMember())) {
                                return;
                            }
                            ticket.selectLanguage(false, discordButtonAction.getMessage());
                        }).submit(),
                        new DiscordButton(0x02, "English", ButtonStyle.PRIMARY, discordButtonAction -> {
                            if (!discordButtonAction.getUser().getId().equalsIgnoreCase(ticket.getMember())) {
                                return;
                            }
                            ticket.selectLanguage(true, discordButtonAction.getMessage());
                        } ).submit()
                },"Please select your language and remember that our", "supporters are volunteers and might take some time to respond!"));
   }

    /**
     * Claims a {@link Ticket}
     *
     * @param ticket the ticket
     */
    public void claimTicket(Ticket ticket, User claimer) {

        TextChannel channel = ticket.getChannel();

        for (Member member : channel.getMembers()) {

            if (member.getId().equalsIgnoreCase(ticket.getMember()) || member.getId().equalsIgnoreCase(claimer.getId())) {
                continue;
            }

            PermissionOverride permissionOverride = channel.getPermissionOverride(member);

            if (permissionOverride == null) {
                channel.createPermissionOverride(member).setDeny(EnumSet.of(Permission.VIEW_CHANNEL)).queue();
            } else {
                channel.putPermissionOverride(member).setDeny(EnumSet.of(Permission.VIEW_CHANNEL)).queue();
            }

        }


    }
    /**
     * Closes a {@link Ticket}
     *
     * @param ticket the ticket
     */
    public void closeTicket(Ticket ticket) {
        TextChannel textChannel = HytoraDiscordBot.getHytora().getDiscord().getTextChannelsByName("ticket-" + ticket.getId(), true).get(0);

        if (textChannel == null) {
            return;
        }

        textChannel.delete().queue();

        int id = Integer.parseInt(textChannel.getName().split("-")[1]);
        ticket = this.tickets.stream().filter(t -> t.getId() == id).findFirst().orElse(ticket);

        this.tickets.remove(ticket);
    }
}
