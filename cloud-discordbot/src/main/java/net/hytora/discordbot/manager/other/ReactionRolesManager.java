package net.hytora.discordbot.manager.other;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.misc.StringCreator;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.hytora.discordbot.HytoraDiscordBot;
import net.hytora.discordbot.util.DiscordChat;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ReactionRolesManager extends ListenerAdapter {

    /**
     * The textchannel
     */
    private final TextChannel textChannel;

    /**
     * All Roles saved with a given emote
     */
    private final Map<String, String> rolesAndEmotes;

    public ReactionRolesManager(String channel, Document jsonDocument) {

        this.rolesAndEmotes = new HashMap<>();
        this.textChannel = HytoraDiscordBot.getHytora().getDiscord().getTextChannelById(channel);

        for (String role : jsonDocument.keys().stream().filter(s -> !s.equalsIgnoreCase("channel")).collect(Collectors.toList())) {


            HytoraDiscordBot.getHytora().createRole(
                    DocumentFactory.newJsonDocument()
                            .set("color", "GRAY")
                            .set("name", role)
                            .set("mentionable", true)
                            .set("showOrder", false)
                    , newRole -> {
                        Logger.constantInstance().info("§7ReactionRoles created §b" + newRole.getName() + "§8!");
                    });

            this.rolesAndEmotes.put(role, jsonDocument.getString(role));
        }
        this.checkForChannel();
        HytoraDiscordBot.getHytora().getDiscord().addEventListener(this);
    }

    /**
     * Checks if message exists
     */
    private void checkForChannel() {

        for (Message message : this.textChannel.getIterableHistory()) {
            message.delete().queue();
        }

        StringCreator strings = new StringCreator();

        strings.singleAppend("React with an emote to receive a role");
        strings.singleAppend("Remove a reaction to remove a role");
        strings.singleAppend("\n");
        strings.singleAppend("**Roles**");

        for (String s : rolesAndEmotes.keySet()) {
            strings.singleAppend(rolesAndEmotes.get(s) + " = " + s);
        }

        DiscordChat.preset(textChannel, "ReactionRoles", HytoraDiscordBot.getHytora().getDiscord().getSelfUser(), new Consumer<Message>() {
            @Override
            public void accept(Message message) {
                for (String s : rolesAndEmotes.keySet()) {
                    message.addReaction(rolesAndEmotes.get(s)).queue();
                }
            }
        }, strings.toArray());
    }


    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        TextChannel channel = event.getChannel();

        if (!channel.getId().equalsIgnoreCase(this.textChannel.getId())) {
            return;
        }

        if (event.getUser().getId().equalsIgnoreCase(HytoraDiscordBot.getHytora().getDiscord().getSelfUser().getId())) {
            return;
        }

        MessageReaction.ReactionEmote reactionEmote = event.getReactionEmote();

        String emoji = reactionEmote.getEmoji();
        String role = this.rolesAndEmotes.keySet().stream().filter(s -> this.rolesAndEmotes.get(s).equalsIgnoreCase(emoji)).findFirst().orElse(null);

        if (role == null) {
            return;
        }

        event.getReaction().removeReaction(event.getUser()).queue();

        Role getRole = HytoraDiscordBot.getHytora().getGuild().getRolesByName(role, true).get(0);

        List<Role> roles = new ArrayList<>(HytoraDiscordBot.getHytora().getGuild().getSelfMember().getRoles());

        roles.sort(Comparator.comparingInt(Role::getPosition));
        Role botRole = roles.get(roles.size() - 1);

        Role hasRole = event.getMember().getRoles().stream().filter(r -> r.getName().equalsIgnoreCase(getRole.getName())).findFirst().orElse(null);

        if (hasRole == null) {
            if (getRole.getPosition() < botRole.getPosition()) {
                HytoraDiscordBot.getHytora().getGuild().addRoleToMember(event.getMember(), getRole).queue();
            } else {
                channel.sendMessage(new MessageBuilder("Cant give you the " + getRole.getName() + " because the bot does not have all rights!").build()).queue(message -> message.delete().queueAfter(3L, TimeUnit.SECONDS));
            }
        } else {
            if (getRole.getPosition() < botRole.getPosition()) {
                HytoraDiscordBot.getHytora().getGuild().removeRoleFromMember(event.getMember(), getRole).queue();
            } else {
                channel.sendMessage(new MessageBuilder("Cant remove you the " + getRole.getName() + " because the bot does not have all rights!").build()).queue(message -> message.delete().queueAfter(3L, TimeUnit.SECONDS));
            }

        }

    }
}
