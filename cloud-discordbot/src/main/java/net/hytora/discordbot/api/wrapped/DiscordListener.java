package net.hytora.discordbot.api.wrapped;

import net.dv8tion.jda.api.events.GenericEvent;
import net.hytora.discordbot.bot.HytoraDiscordBot;

public interface DiscordListener<T extends GenericEvent> {

    default Discord discord() {
        return HytoraDiscordBot.getInstance().getDiscord();
    }

    void handleEvent(T event);
}
