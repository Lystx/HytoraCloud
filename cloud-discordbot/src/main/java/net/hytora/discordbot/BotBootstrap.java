package net.hytora.discordbot;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.formatter.ColoredMessageFormatter;
import cloud.hytora.common.logging.handler.HandledAsyncLogger;
import cloud.hytora.common.logging.handler.HandledLogger;
import org.fusesource.jansi.AnsiConsole;

import java.net.URISyntaxException;

public class BotBootstrap {

    public static void main(String[] args) {

        AnsiConsole.systemInstall();
        HandledLogger logger = new HandledAsyncLogger(LogLevel.fromName(System.getProperty("cloud.logging.level", "INFO")));
        Logger.setFactory(logger.addHandler(entry -> {
            String formatted = ColoredMessageFormatter.format(entry);
            System.out.println(formatted);
        }));
        System.setErr(logger.asPrintStream(LogLevel.ERROR));
        new HytoraDiscordBot(logger);
    }
}
