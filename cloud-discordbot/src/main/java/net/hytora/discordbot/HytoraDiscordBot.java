package net.hytora.discordbot;


import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.common.task.IPromise;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import lombok.Getter;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.hytora.discordbot.commands.*;
import net.hytora.discordbot.listener.*;
import net.hytora.discordbot.manager.command.CommandCategory;
import net.hytora.discordbot.manager.command.CommandManager;
import net.hytora.discordbot.manager.conversation.ConversationManager;
import net.hytora.discordbot.manager.other.ReactionRolesManager;
import net.hytora.discordbot.manager.suggestion.SuggestionManager;
import net.hytora.discordbot.manager.ticket.TicketManager;
import net.hytora.discordbot.util.DiscordChat;
import net.hytora.discordbot.util.button.DiscordButton;
import net.hytora.discordbot.util.button.DiscordButtonAction;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class HytoraDiscordBot {

    /**
     * The instance
     */
    @Getter
    private static HytoraDiscordBot hytora;

    /**
     * The manager for commands
     */
    private CommandManager commandManager;

    /**
     * Manager for suggestions
     */
    private SuggestionManager suggestionManager;

    /**
     * Manager for tickets
     */
    private TicketManager ticketManager;

    /**
     * Manager for roles
     */
    private ReactionRolesManager reactionRolesManager;

    /**
     * For managing conversations
     */
    private final ConversationManager conversationManager;

    /**
     * The config where all values are stored
     */
    private Document config;

    /**
     * The JDA to manage all discord stuff
     */
    private JDA discord;

    /**
     * The cloud guild
     */
    private Guild guild;

    /**
     * The botManagingChannel
     */
    private TextChannel botManaging;

    /**
     * All {@link DiscordButton}s
     */
    private final List<DiscordButton> discordButtons;

    private final Logger logger;

    private final IPromise<Void> guildReadyTask;

    public HytoraDiscordBot(Logger logger) {
        hytora = this;

        //starting bootup time
        long start = System.currentTimeMillis();

        this.logger = logger;
        this.guildReadyTask = IPromise.empty();
        this.conversationManager = new ConversationManager();
        this.discordButtons = new ArrayList<>();

        this.loadConfig();
        this.loadJDA(
                new ListenerAdapter() {
                    @Override
                    public void onGuildReady(@NotNull GuildReadyEvent event) {
                        guildReadyTask.setResult(null);
                    }

                },
                new CommandListener(),
                new JoinListener(),
                new DiscordButtonListener(),
                new ConversationListener()
        );

        this.logger.log(LogLevel.NULL, "\n  _    _       _                  \n" +
                " | |  | |     | |                 \n" +
                " | |__| |_   _| |_ ___  _ __ __ _ \n" +
                " |  __  | | | | __/ _ \\| '__/ _` |\n" +
                " | |  | | |_| | || (_) | | | (_| |\n" +
                " |_|  |_|\\__, |\\__\\___/|_|  \\__,_|\n" +
                "          __/ |                   \n" +
                "         |___/                    \n");
        this.logger.log(LogLevel.NULL, "§8");
        this.logger.info("§7Loading §3HytoraBot §7by §bLystx§8...");
        this.logger.info("§7Waiting for GuildStartup§8...");
        //if guild is ready to use
        this.guildReadyTask.onTaskSucess(v -> {
            this.loadGuild();

            this.commandManager = new CommandManager(this.config.getString("command"));
            this.registerCommands();

            this.logger.info("WELCOME", "§7Bot logged in as §3" + this.discord.getSelfUser().getAsTag() + " §7in §b" + (System.currentTimeMillis() - start) + "ms");
            this.logger.info("WELCOME", "§7Logged in on Guild §3" + this.guild.getName() + " §7@ §b" + this.guild.getId());
            this.logger.info("WELCOME", "§7On the guild are §b" + this.guild.getMembers().size() + "§8/§b" + this.guild.getMaxMembers() + " Members!");
            this.logger.info("§8");

            if (this.checkOtherValues()) {
                this.registerConversations();
                this.manageDefaultRoles();

                DiscordChat.preset(this.botManaging, "Welcome", this.discord.getSelfUser(), message -> {

                }, new Button[]{
                        new DiscordButton(0x00, "Stop DiscordBot", ButtonStyle.DANGER, new Consumer<DiscordButtonAction>() {
                            @Override
                            public void accept(DiscordButtonAction discordButtonAction) {
                                TextChannel textChannel = discordButtonAction.getTextChannel();
                                Message message = discordButtonAction.getMessage();
                                if (textChannel.getId().equalsIgnoreCase(HytoraDiscordBot.getHytora().getBotManaging().getId())) {
                                    EmbedBuilder embedBuilder = DiscordChat.embedBuilder(Color.DARK_GRAY, "Shutdown", discordButtonAction.getUser(), "The HytoraCloud Bot", "Is shutting down in 1 Second...");
                                    message.editMessage(embedBuilder.build()
                                    ).queue(message1 -> {
                                        Scheduler.runTimeScheduler().scheduleDelayedTask(() -> {
                                            message1.delete().queue(unused -> HytoraDiscordBot.getHytora().shutdown());
                                        }, 20L);
                                    });
                                }
                            }
                        }).submit()
                }, "HytoraCloud DiscordBot", "Is now active and may be used!", "----------", "Click the stop-button", "to stop the bot at any time!");

            } else {
                this.logger.info("ERROR", "§cCouldn't load the §econfig.json properly!");
                this.shutdown();
            }
        });
    }

    /**
     * Shuts down the bot
     */
    public void shutdown() {
        this.logger.info("ERROR", "§cShutting down....");

        if (this.suggestionManager != null) {
            this.suggestionManager.save();
        }

        if (this.discord != null) {
            this.discord.shutdown();
        }
        System.exit(1);
    }

    /**
     * Registers all triggers for a Conversation
     */
    private void registerConversations() {

        //HEllo triggers
        this.conversationManager.registerAnswer(
                "Hello",
                0.70,
                (s, user, message) -> s.replace("%user%", user.getAsMention()),
                "Hello %user%!", "Hey %user%! How was your day?", "Bonjour %user% you had a nice day?"
        );
        this.conversationManager.registerAnswer(
                "Hey",
                0.70,
                (s, user, message) -> s.replace("%user%", user.getAsMention()),
                "Hello %user%!", "Hey %user%! How was your day?", "Bonjour %user% you had a nice day?"
        );
        this.conversationManager.registerAnswer(
                "Hallo",
                0.70,
                (s, user, message) -> s.replace("%user%", user.getAsMention()),
                "Hello %user%!", "Hey %user%! How was your day?", "Bonjour %user% you had a nice day?"
        );
        this.conversationManager.registerAnswer(
                "Hi",
                0.70,
                (s, user, message) -> s.replace("%user%", user.getAsMention()),
                "Hello %user%!", "Hey %user%! How was your day?", "Bonjour %user% you had a nice day?"
        );

        //Other
        this.conversationManager.registerAnswer(
                "Whats the Time?",
                0.50,
                (s, user, message) -> s.replace("%time%", new SimpleDateFormat("hh:mm:ss").format(new Date())),
                "Its currently %time%!", "Oh bro I got you!\nIt's currently %time%!");
        this.conversationManager.registerAnswer(
                "How are you?",
                0.50,
                (s, user, message) -> s.replace("%user%", user.getAsMention()),
                "Thanks %user%, I'm fine!",
                "Whoa I'm not that fine today to be honest...",
                "That's not important! How was your day?",
                "Oh, could have been better!",
                "Well, I'm not even awake long enough to say xD"
        );
    }

    /**
     * Manages the default roles
     * and gives everyone the default role
     * if they don't have any
     */

    public void manageDefaultRoles() {

        //Roles
        Document roles = this.config.getDocument("roles");
        Document defaultRole = roles.getDocument("default");
        Document supportRole = roles.getDocument("support");

        this.createRole(defaultRole, df -> {

            logger.info("§7Created §b" + df.getName() + "§7-Role§8!");
            for (Member member : guild.getMembers()) {
                Role memberRole = member.getRoles().stream().filter(role1 -> role1.getName().equalsIgnoreCase(df.getName())).findFirst().orElse(null);
                if (memberRole == null) {
                    guild.addRoleToMember(member, df).queue();
                }
            }

        });
        this.createRole(supportRole, sr -> {
            for (Member member : guild.getMembers()) {
                if (member.hasPermission(Permission.ADMINISTRATOR)) {
                    guild.addRoleToMember(member, sr).queue();
                }
            }
        });
    }

    /**
     * Creates a new {@link Role} and accepts the consumer
     *
     * @param jsonDocument the data for the role
     * @param consumer     the consumer
     */
    public void createRole(Document jsonDocument, Consumer<Role> consumer) {

        String name = jsonDocument.getString("name");
        String color = jsonDocument.getString("color");
        boolean showOrder = jsonDocument.getBoolean("showOrder");
        boolean mentionable = jsonDocument.getBoolean("mentionable");

        int[] rgb = new int[3];

        if (color.startsWith("RGB,")) {
            String[] split = color.split(",");

            rgb[0] = Integer.parseInt(split[1]);
            rgb[1] = Integer.parseInt(split[2]);
            rgb[2] = Integer.parseInt(split[3]);
        }

        Role role = guild.getRoles().stream().filter(role1 -> role1.getName().equalsIgnoreCase(name)).findFirst().orElse(null);

        if (role == null) {
            try {
                guild.createRole()
                        .setName(name)
                        .setColor(rgb[0] == 0 ? (Color) Color.class.getDeclaredField(color).get(Color.WHITE) : new Color(rgb[0], rgb[1], rgb[2]))
                        .setHoisted(showOrder)
                        .setMentionable(mentionable)
                        .queue(consumer);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Registers all {@link net.hytora.discordbot.manager.command.CommandHandler}s
     */
    public void registerCommands() {
        this.commandManager.registerCommand(new HelpCommand("help", "Shows this message", CommandCategory.GENERAL, "?", "hilfe"));
        this.commandManager.registerCommand(new EmbedCommand("embed", "Creates an Embed", CommandCategory.ADMINISTRATION, "em", "bc"));
        this.commandManager.registerCommand(new SuggestCommand("suggest", "Creates a suggestion", CommandCategory.OTHER));
        this.commandManager.registerCommand(new StopCommand("stop", "Stops the bot", CommandCategory.GENERAL));
        this.commandManager.registerCommand(new MemeCommand("meme", "Shows you a meme", CommandCategory.FUN));
    }

    /**
     * Checks for all other config values
     * if they're set and exist
     *
     * @return boolean
     */
    public boolean checkOtherValues() {
        try {

            //Bot Managing channel
            String botManagingId = this.config.getString("botManagingId");
            this.botManaging = this.guild.getTextChannelById(botManagingId);

            //Suggestions
            Document suggestions = this.config.getDocument("suggestions");
            String commands = suggestions.getString("commands");
            String suggestionsChannel = suggestions.getString("suggestions");

            this.suggestionManager = new SuggestionManager(suggestionsChannel);


            Document tickets = this.config.getDocument("tickets");
            String channel = tickets.getString("channel");
            this.ticketManager = new TicketManager(channel);


            Document reactionRoles = this.config.getDocument("reactionRoles");
            this.reactionRolesManager = new ReactionRolesManager(reactionRoles.getString("channel"), reactionRoles);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Loads the config.json
     */
    public Document loadConfig() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(HytoraDiscordBot.class.getResourceAsStream("/config.json"))));
        this.config = DocumentFactory.newJsonDocument(reader);
        return config;
    }

    /**
     * Loads and builds the jda
     */
    public void loadJDA(ListenerAdapter... listenerAdapters) {
        String token = this.config.getString("token");

        if (token.trim().isEmpty()) {
            this.logger.info("§cCan't connect with §eempty token§c!");
            return;
        }

        this.logger.info("§7Trying to connect to §3Hytora-Bot with token §b" + token + "§8...");

        JDABuilder api = JDABuilder.createDefault(token)
                .setMemberCachePolicy(MemberCachePolicy.ALL) //Member caching
                .setStatus(OnlineStatus.ONLINE) //Status
                .addEventListeners((Object[]) listenerAdapters)
                .enableIntents(
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_BANS,
                        GatewayIntent.GUILD_MESSAGES
                )
                .setActivity(Activity.playing(" HytoraCloud")); //activity
        try {
            this.discord = api.build(); //Building
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the current guild
     *
     * @return if success
     */
    public void loadGuild() {

        String guildID = this.config.getString("guildID");

        if (guildID.trim().isEmpty()) {
            this.logger.info("ERROR", "§cCan't search for a guild with an §empty id§c!");
            return;
        }

        System.out.println("==");
        for (Guild discordGuild : this.discord.getGuilds()) {
            System.out.println(discordGuild.getName() + " - " + discordGuild.getName());
        }
        System.out.println("==");

        this.guild = this.discord.getGuildById(guildID);
    }

}
