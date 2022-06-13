package cloud.hytora.driver.services.utils.version.impl;

import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.utils.version.VersionFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GlowstoneConfiguration extends VersionFile {

    @Override
    public void applyFile(ServiceInfo serviceInfo, File file) throws IOException {

        FileWriter writer = new FileWriter(file);
        writer.write("# glowstone.yml is the main configuration file for a Glowstone++ server\n" +
                "# It contains everything from server.properties and bukkit.yml in a\n" +
                "# normal CraftBukkit installation.\n" +
                "# \n" +
                "# For help, join us on Gitter: https://gitter.im/GlowstonePlusPlus/GlowstonePlusPlus\n" +
                "server:\n" +
                "  ip: ''\n" +
                "  port: " + serviceInfo.getPort() + "\n" +
                "  name: " + serviceInfo.getName() + "\n" +
                "  log-file: logs/log-%D.txt\n" +
                "  online-mode: " + false + "\n" +
                "  max-players: " + serviceInfo.getTask().getDefaultMaxPlayers() + "\n" +
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
                "  allow-nether: " + !serviceInfo.getProperties().fallbackValue(true).getBoolean("gameServer") + "\n" +
                "  allow-end: " + !serviceInfo.getProperties().fallbackValue(true).getBoolean("gameServer") + "\n" +
                "  keep-spawn-loaded: true\n" +
                "  populate-anchored-chunks: true\n" +
                "database:\n" +
                "  driver: org.sqlite.JDBC\n" +
                "  url: jdbc:sqlite:config/database.db\n" +
                "  username: glowstone\n" +
                "  password: nether\n" +
                "  isolation: SERIALIZABLE\n");

        writer.flush();
        writer.close();
    }

    @Override
    public String getFileName() {
        return "glowstone.yml";
    }
}
