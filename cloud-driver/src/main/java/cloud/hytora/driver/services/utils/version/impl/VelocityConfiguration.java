package cloud.hytora.driver.services.utils.version.impl;

import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.utils.version.VersionFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VelocityConfiguration extends VersionFile {

    @Override
    public void applyFile(ICloudService ICloudServer, File file) throws IOException {

        FileWriter writer = new FileWriter(file);

        writer.write("# Config version. Do not change this\n" +
                "config-version = \"1.0\"\n" +
                "\n" +
                "# What port should the proxy be bound to? By default, we'll bind to all addresses on port 25577.\n" +
                "bind = \"0.0.0.0:" + ICloudServer.getPort() + "\"\n" +
                "\n" +
                "# What should be the MOTD? This gets displayed when the player adds your server to\n" +
                "# their server list. Legacy color codes and JSON are accepted.\n" +
                "motd = \"&3A Velocity Server\"\n" +
                "\n" +
                "# What should we display for the maximum number of players? (Velocity does not support a cap\n" +
                "# on the number of players online.)\n" +
                "show-max-players = " + ICloudServer.getMaxPlayers() + "\n" +
                "\n" +
                "# Should we authenticate players with Mojang? By default, this is on.\n" +
                "online-mode = " + ICloudServer.getProperties().fallbackValue(true).getBoolean("onlineMode") + "\n" +
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
                "proxy-protocol = " + ICloudServer.getProperties().fallbackValue(false).getBoolean("proxyProtocol") + "\n" +
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
    }

    @Override
    public String getFileName() {
        return "velocity.toml";
    }
}
