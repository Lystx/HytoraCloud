package net.hytora.discordbot.bot;


import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.document.IEntry;
import cloud.hytora.document.wrapped.StorableDocument;
import cloud.hytora.driver.commands.ICommandManager;
import cloud.hytora.remote.impl.RemoteCommandManager;
import lombok.Getter;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.hytora.discordbot.api.wrapped.Discord;
import net.hytora.discordbot.api.wrapped.DiscordListener;
import net.hytora.discordbot.api.wrapped.DiscordServer;
import net.hytora.discordbot.api.wrapped.DiscordBuilder;
import net.hytora.discordbot.api.internal.IBotService;
import net.hytora.discordbot.bot.service.GeneralService;
import net.hytora.discordbot.bot.service.command.CommandService;
import net.hytora.discordbot.bot.service.rules.RuleService;
import net.hytora.discordbot.bot.service.suggestion.SuggestionService;
import net.hytora.discordbot.bot.service.welcome.WelcomeService;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


// TODO: 19.09.2022 default roles
// TODO: 19.09.2022 auto role

@Getter
public class HytoraDiscordBot {

    @Getter
    private static HytoraDiscordBot instance;

    private final Discord discord;
    private final ICommandManager commandManager;
    private final Logger logger;
    private final Task<Void> guildReadyTask;
    private final Map<String, IBotService> services;

    public HytoraDiscordBot(Logger logger, String token) {
        instance = this;

        //starting bootup time
        long start = System.currentTimeMillis();


        this.logger = logger;
        this.guildReadyTask = Task.empty();
        this.services = new HashMap<>();
        this.commandManager = new RemoteCommandManager();


        //setting services
        this.setService(new GeneralService());
        this.setService(new CommandService());
        this.setService(new RuleService());
        this.setService(new WelcomeService());
        this.setService(new SuggestionService());

        Collection<DiscordListener<?>> listeners = new ArrayList<>();
        listeners.add((DiscordListener<GuildReadyEvent>) event -> guildReadyTask.setResult(null));
        for (IBotService value : services.values()) {
            value.addListeners(listeners);
        }

        //loading jda wrapper
        this.discord = DiscordBuilder.newBuilder()
                .token(token) //setting token

                //setting intents
                .appendIntent(GatewayIntent.GUILD_PRESENCES)
                .appendIntent(GatewayIntent.GUILD_MEMBERS)
                .appendIntent(GatewayIntent.DIRECT_MESSAGES)
                .appendIntent(GatewayIntent.GUILD_VOICE_STATES)
                .appendIntent(GatewayIntent.GUILD_BANS)
                .appendIntent(GatewayIntent.GUILD_MESSAGES)

                //other values
                .cachePolicy(MemberCachePolicy.ONLINE)
                .status(OnlineStatus.ONLINE)

                //appending listeners
                .listeners(listeners.toArray(new DiscordListener[0]))
                .build();

        this.logger.log(LogLevel.NULL, "§8");
        this.logger.info("§7Loading §3HytoraBot §7by §bLystx§8...");
        this.logger.info("§7Waiting for GuildStartup§8...");
        //if guild is ready to use
        this.guildReadyTask.onTaskSucess(v -> {

            DiscordServer firstServer = discord.getFirstServer();

            this.logger.info("§7Bot logged in as §3" + this.discord.getBotUser().getAsTag() + " §7in §b" + (System.currentTimeMillis() - start) + "ms");
            this.logger.info("§7Logged in on Guild §3" + firstServer.getName() + " §7@ §b" + firstServer.getId());
            this.logger.info("§7On the guild are §b" + firstServer.getMembers().size() + "§8/§b" + firstServer.getMaxMembers() + " Members!");
            this.logger.info("§8");

            this.setup(firstServer);
        });
    }


    public StorableDocument setup(DiscordServer server) {
        File baseDirectory = new File("discord/");
        baseDirectory.mkdirs();

        File serverDirectory = new File(baseDirectory, server.getId() + "/");
        serverDirectory.mkdirs();

        File configFile = new File(serverDirectory, "config.json");
        if (!configFile.exists()) {

            StorableDocument document = Document.newStorableJsonDocumentUnchecked(configFile.toPath());

            for (IBotService service : this.services.values()) {
                try {
                    Document setupDocument = service.handleSetup(server);
                    if (setupDocument != null) {
                        service.handleConfigLoad(setupDocument);
                        document.set(service.getIdentifier(), setupDocument);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            document.save();
            return document;
        } else {
            StorableDocument document = Document.newStorableJsonDocumentUnchecked(configFile.toPath());
            for (String serviceName : services.keySet()) {
                IBotService service = getService(serviceName);
                if (!document.has(serviceName)) {
                    Document doc = service.handleSetup(server);
                    if (doc != null) {
                        service.handleConfigLoad(doc);
                        document.set(service.getIdentifier(), doc);
                    }

                    continue;
                }
                IEntry entry = document.get(serviceName);
                service.handleConfigLoad(entry.toDocument());
            }
            document.save();
            return document;
        }
    }


    /**
     * Shuts down the bot
     */
    public void shutdown() {
        this.logger.info("ERROR", "§cShutting down....");
        this.discord.shutdown();
        System.exit(1);
    }

    public void setService(IBotService service) {
        this.services.put(service.getIdentifier(), service);
    }

    public <T extends IBotService> T getService(String name) {
        return (T) this.services.get(name);
    }


}
