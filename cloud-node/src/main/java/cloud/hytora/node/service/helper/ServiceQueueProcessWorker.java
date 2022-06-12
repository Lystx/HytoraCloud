package cloud.hytora.node.service.helper;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.wrapper.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.DestructiveListener;
import cloud.hytora.driver.event.defaults.server.CloudServerCacheUnregisterEvent;
import cloud.hytora.driver.event.defaults.server.CloudServerRequestScreenLeaveEvent;
import cloud.hytora.driver.exception.CloudException;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.base.ModuleConfig;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.node.config.JavaVersion;
import cloud.hytora.driver.services.task.TaskDownloadEntry;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.TemplateStorage;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVersion;
import cloud.hytora.node.impl.config.MainConfiguration;
import cloud.hytora.node.impl.event.ServiceOutputLineAddEvent;
import cloud.hytora.node.service.NodeServiceManager;
import cloud.hytora.node.NodeDriver;


import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarInputStream;


public class ServiceQueueProcessWorker {

    private final ServiceInfo service;
    private final NodeServiceManager serviceManager;


    @SneakyThrows
    public ServiceQueueProcessWorker(NodeServiceManager serviceManager, ServiceInfo service) {
        this.serviceManager = serviceManager;
        this.service = service;
        this.service.setServiceState(ServiceState.STARTING);

        // add statistic to service
        NodeDriver.getInstance().getExecutor().registerStats(this.service);

        this.downloadServiceVersion(this.service.getTask().getVersion());

        // create server dir
        File parent = (service.getTask().getTaskGroup().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File serverDir = new File(parent, service.getName() + "/");

        FileUtils.forceMkdir(serverDir);

        // load all current task templates
        ServiceTask serviceTask = service.getTask();

        //all templates for this service
        Collection<ServiceTemplate> templates = serviceTask.getTaskGroup().getTemplates(); //parent templates
        templates.addAll(serviceTask.getTemplates()); //task templates

        for (ServiceTemplate template : templates) {
            TemplateStorage storage = template.getStorage();
            if (storage != null) {
                storage.copyTemplate(service, template, serverDir);
            }
        }

        String jar = service.getTask().getVersion().getJar();
        FileUtils.copyFile(new File(NodeDriver.STORAGE_VERSIONS_FOLDER, jar), new File(serverDir, jar));

        // copy plugin
        FileUtils.copyFile(new File(NodeDriver.STORAGE_VERSIONS_FOLDER, "plugin.jar"), new File(serverDir, "plugins/plugin.jar"));

        // TODO: 11.04.2022 change address if other node
        RemoteIdentity identity = new RemoteIdentity(NodeDriver.getInstance().getConfig().getAuthKey(), service.getTask().getNode(), NodeDriver.getInstance().getExecutor().getHostName(), service.getName(), NodeDriver.getInstance().getExecutor().getPort());

        // write property for identify service
        identity.save(new File(serverDir, "property.json"));

        //copy extra downloads
        for (TaskDownloadEntry entry : service.getTask().getTaskGroup().getDownloadEntries()) {
            CloudDriver.getInstance().getLogger().log(LogLevel.INFO, "Downloading entry for '{}' [url={}, dest={}]", service.getName(), entry.getUrl(), entry.getDestination());
            String url = entry.getUrl();
            FileUtils.copyURLToFile(new URL(url), new File(serverDir, entry.getDestination()));
        }

        //copying modules
        for (ModuleController module : CloudDriver.getInstance().getModuleManager().getModules()) {
            ModuleConfig config = module.getModuleConfig();
            if (config.getCopyType().applies(serviceTask.getTaskGroup().getEnvironment())) {
                Path jarFile = module.getJarFile();
                FileUtils.copyFile(jarFile.toFile(), new File(new File(serverDir, "plugins/"), jarFile.toFile().getName()));
            }
        }

        ServiceVersion version = service.getTask().getVersion();

        Boolean onlineMode = service.getTask().getProperty("onlineMode", Boolean.class);
        Boolean proxyProtocol = service.getTask().getProperty("proxyProtocol", Boolean.class);
        Boolean gameServer = service.getTask().getProperty("gameServer", Boolean.class);

        if (onlineMode == null) onlineMode = true;
        if (proxyProtocol == null) proxyProtocol = false;
        if (gameServer == null) gameServer = true;


        File serverIcon = new File(serverDir, "server-icon.png");
        if (!serverIcon.exists()) {
            copyFileWithURL("/impl/files/server-icon.png", new File(serverDir, "server-icon.png")); //copying server icon if none already provided
        }

        if (version == ServiceVersion.VELOCITY) {

            FileWriter writer = new FileWriter(new File(serverDir, "velocity.toml"));

            writer.write("# Config version. Do not change this\n" +
                    "config-version = \"1.0\"\n" +
                    "\n" +
                    "# What port should the proxy be bound to? By default, we'll bind to all addresses on port 25577.\n" +
                    "bind = \"0.0.0.0:" + service.getPort() + "\"\n" +
                    "\n" +
                    "# What should be the MOTD? This gets displayed when the player adds your server to\n" +
                    "# their server list. Legacy color codes and JSON are accepted.\n" +
                    "motd = \"&3A Velocity Server\"\n" +
                    "\n" +
                    "# What should we display for the maximum number of players? (Velocity does not support a cap\n" +
                    "# on the number of players online.)\n" +
                    "show-max-players = " + service.getMaxPlayers() + "\n" +
                    "\n" +
                    "# Should we authenticate players with Mojang? By default, this is on.\n" +
                    "online-mode = " + onlineMode + "\n" +
                    "\n" +
                    "# Should we forward IP addresses and other data to backend servers?\n" +
                    "# Available options:\n" +
                    "# - \"none\":   No forwarding will be done. All players will appear to be connecting from the\n" +
                    "#             proxy and will have offline-mode UUIDs.\n" +
                    "# - \"legacy\": Forward player IPs and UUIDs in a BungeeCord-compatible format. Use this if\n" +
                    "#             you run servers using Minecraft 1.12 or lower.\n" +
                    "# - \"modern\": Forward player IPs and UUIDs as part of the login process using Velocity's\n" +
                    "#             native forwarding. Only applicable for Minecraft 1.13 or higher.\n" +
                    "player-info-forwarding-mode = \"NONE\"\n" +
                    "\n" +
                    "# If you are using modern IP forwarding, configure an unique secret here.\n" +
                    "forwarding-secret = \"5L7eb15i6yie\"\n" +
                    "\n" +
                    "# Announce whether or not your server supports Forge. If you run a modded server, we\n" +
                    "# suggest turning this on.\n" +
                    "announce-forge = false\n" +
                    "\n" +
                    "[servers]\n" +
                    "\n" +
                    "# In what order we should try servers when a player logs in or is kicked from aserver.\n" +
                    "try = []\n" +
                    "\n" +
                    "[forced-hosts]\n" +
                    "# Configure your forced hosts here.\n" +
                    "\n" +
                    "[advanced]\n" +
                    "# How large a Minecraft packet has to be before we compress it. Setting this to zero will\n" +
                    "# compress all packets, and setting it to -1 will disable compression entirely.\n" +
                    "compression-threshold = 256\n" +
                    "\n" +
                    "# How much compression should be done (from 0-9). The default is -1, which uses the\n" +
                    "# default level of 6.\n" +
                    "compression-level = -1\n" +
                    "\n" +
                    "# How fast (in milliseconds) are clients allowed to connect after the last connection? By\n" +
                    "# default, this is three seconds. Disable this by setting this to 0.\n" +
                    "login-ratelimit = 3000\n" +
                    "\n" +
                    "# Specify a custom timeout for connection timeouts here. The default is five seconds.\n" +
                    "connection-timeout = 5000\n" +
                    "\n" +
                    "# Specify a read timeout for connections here. The default is 30 seconds.\n" +
                    "read-timeout = 30000\n" +
                    "\n" +
                    "# Enables compatibility with HAProxy.\n" +
                    "proxy-protocol = " + proxyProtocol + "\n" +
                    "\n" +
                    "[query]\n" +
                    "# Whether to enable responding to GameSpy 4 query responses or not.\n" +
                    "enabled = false\n" +
                    "\n" +
                    "# If query is enabled, on what port should the query protocol listen on?\n" +
                    "port = 25577\n" +
                    "\n" +
                    "# This is the map name that is reported to the query services.\n" +
                    "map = \"Velocity\"\n" +
                    "\n" +
                    "# Whether plugins should be shown in query response by default or not\n" +
                    "show-plugins = false\n" +
                    "\n" +
                    "[metrics]\n" +
                    "# Whether metrics will be reported to bStats (https://bstats.org).\n" +
                    "# bStats collects some basic information, like how many people use Velocity and their\n" +
                    "# player count. We recommend keeping bStats enabled, but if you're not comfortable with\n" +
                    "# this, you can turn this setting off. There is no performance penalty associated with\n" +
                    "# having metrics enabled, and data sent to bStats can't identify your server.\n" +
                    "enabled = false\n" +
                    "\n" +
                    "# A unique, anonymous ID to identify this proxy with.\n" +
                    "id = \"9cc04bee-691b-450b-94dc-5f5de5b6847b\"\n" +
                    "\n" +
                    "log-failure = false");

            writer.flush();
            writer.close();

        } else if (version == ServiceVersion.BUNGEE || version == ServiceVersion.WATERFALL) {

            FileWriter writer = new FileWriter(new File(serverDir, "config.yml"));

            writer.write("player_limit: " + service.getMaxPlayers() + "\n" +
                    "permissions:\n" +
                    "  default: []\n" +
                    "  admin:\n" +
                    "    - bungeecord.command.alert\n" +
                    "    - bungeecord.command.end\n" +
                    "    - bungeecord.command.ip\n" +
                    "    - bungeecord.command.reload\n" +
                    "    - bungeecord.command.send\n" +
                    "    - bungeecord.command.server\n" +
                    "    - bungeecord.command.list\n" +
                    "timeout: 30000\n" +
                    "log_commands: false\n" +
                    "online_mode: " + onlineMode + "\n" +
                    "disabled_commands:\n" +
                    "  - disabledcommandhere\n" +
                    "log_pings: false\n" +
                    "servers:\n" +
                    "  Lobby-1:\n" +
                    "    motd: '" + "MOTD" + "'\n" +
                    "    address: '127.0.0.1:" + MainConfiguration.getInstance().getSpigotStartPort() + "'\n" +
                    "    restricted: false\n" +
                    "listeners:\n" +
                    "  - query_port: 25577\n" +
                    "    motd: \"&bHytoraCloud &7Default Motd &7by Lystx\"\n" +
                    "    priorities:\n" +
                    "      - Lobby-1\n" +
                    "    bind_local_address: true\n" +
                    "    tab_list: GLOBAL_PING\n" +
                    "    query_enabled: false\n" +
                    "    host: 0.0.0.0:" + service.getPort() + "\n" +
                    "    forced_hosts:\n" +
                    "      pvp.md-5.net: pvp\n" +
                    "    max_players: 0\n" +
                    "    tab_size: 60\n" +
                    "    ping_passthrough: false\n" +
                    "    force_default_server: false\n" +
                    "    proxy_protocol: " + proxyProtocol + "\n" +
                    "ip_forward: true\n" +
                    "network_compression_threshold: 256\n" +
                    "groups:\n" +
                    "connection_throttle: -1\n" +
                    "stats: 13be5ac9-5731-4502-9ccc-c4a80163f14a\n" +
                    "prevent_proxy_connections: false");

            writer.flush();
            writer.close();

        } else if (!version.isProxy()) {// is spigot

            //copying spigot.yml
            copyFileWithURL("/impl/files/spigot.yml", new File(serverDir, "spigot.yml"));

            //copying bukkit.yml
            copyFileWithURL("/impl/files/bukkit.yml", new File(serverDir, "bukkit.yml"));

            //managing server.properties
            File pp = new File(serverDir, "server.properties");
            if (!pp.exists())
                copyFileWithURL("/impl/files/server.properties", pp);


            try {
                FileInputStream stream = new FileInputStream(pp);
                Properties properties = new Properties();
                properties.load(stream);
                properties.setProperty("server-port", service.getPort() + "");
                properties.setProperty("server-ip", "0");
                properties.setProperty("max-players", String.valueOf(service.getMaxPlayers()));
                properties.setProperty("allow-nether", String.valueOf(!gameServer));
                properties.setProperty("server-name", service.getName());
                properties.setProperty("online-mode", "false");
                properties.setProperty("motd", service.getMotd());
                FileOutputStream fileOutputStream = new FileOutputStream(pp);
                properties.save(fileOutputStream, "Edit by Cloud");
                fileOutputStream.close();
                stream.close();

            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        if (version.name().startsWith("GLOWSTONE")) { // TODO: 06.06.2022 add glowstone support

            cloud.hytora.common.misc.FileUtils.writeToFile(new File(serverDir, "glowstone.yml"), "# glowstone.yml is the main configuration file for a Glowstone++ server\n" +
                    "# It contains everything from server.properties and bukkit.yml in a\n" +
                    "# normal CraftBukkit installation.\n" +
                    "# \n" +
                    "# For help, join us on Gitter: https://gitter.im/GlowstonePlusPlus/GlowstonePlusPlus\n" +
                    "server:\n" +
                    "  ip: ''\n" +
                    "  port: " + service.getPort() + "\n" +
                    "  name: " + service.getName() + "\n" +
                    "  log-file: logs/log-%D.txt\n" +
                    "  online-mode: " + false + "\n" +
                    "  max-players: " + service.getTask().getDefaultMaxPlayers() + "\n" +
                    "  whitelisted: false\n" +
                    "  motd: 'HytoraCloud Minecraft Service'\n" +
                    "  shutdown-message: Server shutting down..\n" +
                    "  allow-client-mods: true\n" +
                    "  snooper-enabled: false\n" +
                    "console:\n" +
                    "  use-jline: false\n" +
                    "  prompt: ''\n" +
                    "  date-format: HH:mm:ss\n" +
                    "  log-date-format: yyyy/MM/dd HH:mm:ss\n" +
                    "game:\n" +
                    "  gamemode: SURVIVAL\n" +
                    "  gamemode-force: 'false'\n" +
                    "  difficulty: NORMAL\n" +
                    "  hardcore: false\n" +
                    "  pvp: true\n" +
                    "  max-build-height: 256\n" +
                    "  announce-achievements: true\n" +
                    "  allow-flight: false\n" +
                    "  command-blocks: false\n" +
                    "  resource-pack: ''\n" +
                    "  resource-pack-hash: ''\n" +
                    "creatures:\n" +
                    "  enable:\n" +
                    "    monsters: true\n" +
                    "    animals: true\n" +
                    "    npcs: true\n" +
                    "  limit:\n" +
                    "    monsters: 70\n" +
                    "    animals: 15\n" +
                    "    water: 5\n" +
                    "    ambient: 15\n" +
                    "  ticks:\n" +
                    "    monsters: 1\n" +
                    "    animal: 400\n" +
                    "folders:\n" +
                    "  plugins: plugins\n" +
                    "  update: update\n" +
                    "  worlds: worlds\n" +
                    "files:\n" +
                    "  permissions: permissions.yml\n" +
                    "  commands: commands.yml\n" +
                    "  help: help.yml\n" +
                    "advanced:\n" +
                    "  connection-throttle: 0\n" +
                    "  idle-timeout: 0\n" +
                    "  warn-on-overload: true\n" +
                    "  exact-login-location: false\n" +
                    "  plugin-profiling: false\n" +
                    "  deprecated-verbose: 'false'\n" +
                    "  compression-threshold: 256\n" +
                    "  proxy-support: true\n" +
                    "  player-sample-count: 12\n" +
                    "extras:\n" +
                    "  query-enabled: false\n" +
                    "  query-port: 25614\n" +
                    "  query-plugins: true\n" +
                    "  rcon-enabled: false\n" +
                    "  rcon-password: glowstone\n" +
                    "  rcon-port: 25575\n" +
                    "  rcon-colors: true\n" +
                    "world:\n" +
                    "  name: world\n" +
                    "  seed: ''\n" +
                    "  level-type: MINECRAFT_SERVER\n" +
                    "  spawn-radius: 16\n" +
                    "  view-distance: 8\n" +
                    "  gen-structures: true\n" +
                    "  gen-settings: ''\n" +
                    "  allow-nether: " + !gameServer + "\n" +
                    "  allow-end: " + !gameServer + "\n" +
                    "  keep-spawn-loaded: true\n" +
                    "  populate-anchored-chunks: true\n" +
                    "database:\n" +
                    "  driver: org.sqlite.JDBC\n" +
                    "  url: jdbc:sqlite:config/database.db\n" +
                    "  username: glowstone\n" +
                    "  password: nether\n" +
                    "  isolation: SERIALIZABLE\n");
        }


    }

    @SneakyThrows
    public Task<ServiceInfo> processService() {
        Task<ServiceInfo> task = Task.empty(ServiceInfo.class).denyNull();

        File parent = (service.getTask().getTaskGroup().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File folder = new File(parent, service.getName() + "/");

        StartedProcess result = new ProcessExecutor()
                .command(this.args(this.service))
                .directory(folder)
                .redirectOutput(new LogOutputStream() {
                    @Override
                    protected void processLine(String line) {

                        DestructiveListener listener = CloudDriver.getInstance().getEventManager().registerSelfDestructiveHandler(ServiceOutputLineAddEvent.class, event -> {
                            String line1 = event.getLine();
                            if (serviceManager.getCachedServiceOutputs().get(service.getName()) == null) {
                                return;
                            }
                            serviceManager.getCachedServiceOutputs().get(service.getName()).add(line1);
                            if (service.asCloudServer().isScreenServer()) {
                                CloudDriver.getInstance().getCommandSender().sendMessage(line1);
                            }
                        });
                        CloudDriver.getInstance().getEventManager().registerSelfDestructiveHandler(CloudServerCacheUnregisterEvent.class, e -> {
                            if (service.asCloudServer().isScreenServer()) {
                                CloudDriver.getInstance().getEventManager().callEventGlobally(new CloudServerRequestScreenLeaveEvent(CloudDriver.getInstance().getCommandManager(), CloudDriver.getInstance().getConsole(), CloudDriver.getInstance().getCommandSender(), service));
                            }
                            listener.destroy();
                        });

                        CloudDriver.getInstance().getEventManager().callEventGlobally(new ServiceOutputLineAddEvent(service, line));
                    }
                })
                .start();

        Process process = result.getProcess();
        this.service.asCloudServer().setProcess(process);
        this.service.asCloudServer().setWorkingDirectory(folder);
        task.setResult(this.service);

        return task;
    }


    private String[] args(ServiceInfo service) {
        ServiceTask task = service.getTask();
        List<String> arguments = new ArrayList<>(Arrays.asList("java"));
        int javaVersion = task.getJavaVersion();


        if (javaVersion != -1) {

            INodeConfig config = NodeDriver.getInstance().getConfig();
            JavaVersion version = config.getJavaVersions().stream().filter(jv -> jv.getId() == javaVersion).findFirst().orElse(null);

            if (version != null) {
                arguments.add(version.getPath()); //adding path to custom java version
            }
        }

        //adding pre defined arguments
        arguments.addAll(Arrays.asList(
                "-DIReallyKnowWhatIAmDoingISwear",
                "-Dcom.mojang.eula.agree=true",
                "-Xms" + service.getTask().getMemory() + "M",
                "-Xmx" + service.getTask().getMemory() + "M")
        );

        //adding custom task arguments
        if (task.getTaskGroup().getJavaArguments() != null && task.getTaskGroup().getJavaArguments().length > 0) {
            arguments.addAll(Arrays.asList(task.getTaskGroup().getJavaArguments()));
        }

        Path remoteFile = new File(NodeDriver.STORAGE_VERSIONS_FOLDER, "remote.jar").toPath();

        File parent = (service.getTask().getTaskGroup().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File applicationFile = new File(parent, service.getName() + "/" + service.getTask().getVersion().getJar());


        arguments.addAll(Arrays.asList(
                "-cp", remoteFile.toAbsolutePath() + File.pathSeparator + applicationFile.toPath().toAbsolutePath()));

        try (JarInputStream jarInputStream = new JarInputStream(Files.newInputStream(remoteFile))) {
            arguments.add(jarInputStream.getManifest().getMainAttributes().getValue("Main-Class"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (JarInputStream jarInputStream = new JarInputStream(Files.newInputStream(applicationFile.toPath()))) {
            arguments.add(jarInputStream.getManifest().getMainAttributes().getValue("Main-Class"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (service.getTask().getVersion().getWrapperEnvironment() == SpecificDriverEnvironment.MINECRAFT_SERVER) {
            arguments.add("nogui");
        }

        return arguments.toArray(new String[]{});
    }


    private void copyFileWithURL(String filename, File location) {
        try {
            URL inputUrl = getClass().getResource(filename);
            if (location.exists()) {
                return;
            }
            if (inputUrl == null) {
                throw new CloudException("Couldn't copy " + filename + " to Location: " + location.toString());
            }
            try {
                FileUtils.copyURLToFile(inputUrl, location);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    private void downloadServiceVersion(ServiceVersion version) {
        File file = new File(NodeDriver.STORAGE_VERSIONS_FOLDER, version.getJar());

        if (file.exists()) {
            return;
        }

        CloudDriver.getInstance().getLogger().info("§7Downloading §bVersion§7... (§3" + version.getTitle() + "§7)");

        file.getParentFile().mkdirs();

        try {
            String url = version.getUrl();
            FileUtils.copyURLToFile(new URL(url), file);

            if (version.getTitle().equals("paper")) {
                Process process = new ProcessBuilder("java", "-jar", version.getJar()).directory(file.getParentFile()).start();
                InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                bufferedReader.readLine();
                bufferedReader.readLine();
                bufferedReader.readLine();
                process.destroyForcibly();
                bufferedReader.close();
                inputStreamReader.close();
                FileUtils.copyFile(new File(NodeDriver.STORAGE_VERSIONS_FOLDER, "cache/patched_" + version.getVersion() + ".jar"), file);
                FileUtils.deleteDirectory(new File(NodeDriver.STORAGE_VERSIONS_FOLDER, "cache/"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            CloudDriver.getInstance().getLogger().error("§cFailed to download version§7... (§3" + version.getTitle() + "§7)");
            return;
        }
        CloudDriver.getInstance().getLogger().info("Downloading of (§3" + version.getTitle() + "§7)§a successfully §7completed.");
    }


}
