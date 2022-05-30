package cloud.hytora.node.service.properties;

import cloud.hytora.driver.services.CloudServer;

import java.io.File;

public class BungeeProperties extends ServiceProperties {

    public BungeeProperties(File directory, int port, int maxPlayers, CloudServer firstServer) {
        super(directory, "config.yml", port);

        this.setProperties(new String[]{
                "player_limit: " + maxPlayers + "\n" +
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
                        "online_mode: " + true + "\n" +
                        "disabled_commands:\n" +
                        "  - disabledcommandhere\n" +
                        "log_pings: false\n" +
                        "servers:\n" +
                        "  " + (firstServer == null ? "Lobby-1" : firstServer.getName()) + ":\n" +
                        "    motd: '" + "MOTD" + "'\n" +
                        "    address: '127.0.0.1:" + (firstServer == null ? 40000 : firstServer.getPort()) + "'\n" +
                        "    restricted: false\n" +
                        "listeners:\n" +
                        "  - query_port: 25577\n" +
                        "    motd: \"HytoraCloud Proxy Service\"\n" +
                        "    priorities:\n" +
                        "      - Lobby-1\n" +
                        "    bind_local_address: true\n" +
                        "    tab_list: GLOBAL_PING\n" +
                        "    query_enabled: false\n" +
                        "    host: 0.0.0.0:" + port + "\n" +
                        "    forced_hosts:\n" +
                        "      pvp.md-5.net: pvp\n" +
                        "    max_players: 0\n" +
                        "    tab_size: 60\n" +
                        "    ping_passthrough: false\n" +
                        "    force_default_server: false\n" +
                        "    proxy_protocol: " + false + "\n" +
                        "ip_forward: true\n" +
                        "network_compression_threshold: 256\n" +
                        "groups:\n" +
                        "connection_throttle: -1\n" +
                        "stats: 13be5ac9-5731-4502-9ccc-c4a80163f14a\n" +
                        "prevent_proxy_connections: false"
        });

        this.writeFile();
    }

}
