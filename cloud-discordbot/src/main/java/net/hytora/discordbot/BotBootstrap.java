package net.hytora.discordbot;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.formatter.ColoredMessageFormatter;
import cloud.hytora.common.logging.handler.HandledAsyncLogger;
import cloud.hytora.common.logging.handler.HandledLogger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.hytora.discordbot.api.wrapped.DiscordBuilder;
import net.hytora.discordbot.api.wrapped.DiscordListener;
import net.hytora.discordbot.bot.HytoraDiscordBot;
import net.hytora.discordbot.bot.impl.SimpleDiscord;
import org.fusesource.jansi.AnsiConsole;
import org.jetbrains.annotations.NotNull;

public class BotBootstrap {

    public static void main(String[] args) {

        AnsiConsole.systemInstall();
        HandledLogger logger = new HandledAsyncLogger(LogLevel.fromName(System.getProperty("cloud.logging.level", "INFO")));
        Logger.setFactory(logger.addHandler(entry -> {
            String formatted = ColoredMessageFormatter.format(entry);
            System.out.println(formatted);
        }));
        System.setErr(logger.asPrintStream(LogLevel.ERROR));

        DiscordBuilder.setProcessor(builder -> {

            JDA jda = JDABuilder.createDefault(builder.getToken())
                    .setMemberCachePolicy(builder.getMemberCachePolicy()) //Member caching
                    .setStatus(builder.getStatus()) //Status
                    .addEventListeners(new ListenerAdapter() {
                        @Override
                        public void onGenericEvent(@NotNull GenericEvent event) {
                            for (DiscordListener listener : builder.getListeners()) {
                                try {
                                    listener.handleEvent(event);
                                } catch (Exception e) {
                                    //ignore because of wrong generic type
                                }
                            }
                        }
                    })
                    .enableIntents(builder.getIntents())
                    .setActivity(builder.getActivity()).build();
            return new SimpleDiscord(jda);
        });

        new HytoraDiscordBot(logger, args[0]);
    }
}
