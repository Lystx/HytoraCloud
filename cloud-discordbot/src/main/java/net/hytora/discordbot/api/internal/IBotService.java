package net.hytora.discordbot.api.internal;

import cloud.hytora.document.Document;
import net.dv8tion.jda.api.events.GenericEvent;
import net.hytora.discordbot.api.wrapped.DiscordListener;
import net.hytora.discordbot.api.wrapped.DiscordServer;

import java.util.Collection;

public interface IBotService {

    void addListeners(Collection<DiscordListener<?>> listeners);

    String getIdentifier();

    Document handleSetup(DiscordServer server);

    void handleConfigLoad(Document document);
}
