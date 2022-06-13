package cloud.hytora.driver.services.utils.version.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import cloud.hytora.driver.services.utils.version.VersionFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class BungeeConfiguration extends VersionFile {

    @Override
    public void applyFile(ServiceInfo serviceInfo, File file) throws IOException {

        FileWriter writer = new FileWriter(file);

        List<ServiceInfo> services = CloudDriver.getInstance().getServiceManager().getAllServicesByEnvironment(SpecificDriverEnvironment.MINECRAFT);
        ServiceInfo firstService = services.isEmpty() ? null : services.get(0);

        String firstServerName = firstService == null ? "fallback": firstService.getName();
        String firstServerMotd = firstService == null ? "Default HytoraCloud Fallback" : firstService.getMotd();
        int firstServerPort = firstService == null ? 50000 : firstService.getPort();

        writer.write("player_limit: " + serviceInfo.getMaxPlayers() + "\n" +
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
                "online_mode: " + serviceInfo.getProperties().fallbackValue(true).getBoolean("onlineMode") + "\n" +
                "disabled_commands:\n" +
                "  - disabledcommandhere\n" +
                "log_pings: false\n" +
                "servers:\n" +
                "  " + firstServerName + ":\n" +
                "    motd: '" + firstServerMotd + "'\n" +
                "    address: '127.0.0.1:" + firstServerPort + "'\n" +
                "    restricted: false\n" +
                "listeners:\n" +
                "  - query_port: 25577\n" +
                "    motd: \"&bHytoraCloud &7Default Motd &7by Lystx\"\n" +
                "    priorities:\n" +
                "      - " + firstServerName + "\n" +
                "    bind_local_address: true\n" +
                "    tab_list: GLOBAL_PING\n" +
                "    query_enabled: false\n" +
                "    host: 0.0.0.0:" + serviceInfo.getPort() + "\n" +
                "    forced_hosts:\n" +
                "      pvp.md-5.net: pvp\n" +
                "    max_players: 0\n" +
                "    tab_size: 60\n" +
                "    ping_passthrough: false\n" +
                "    force_default_server: false\n" +
                "    proxy_protocol: " + serviceInfo.getProperties().fallbackValue(false).getBoolean("proxyProtocol") + "\n" +
                "ip_forward: true\n" +
                "network_compression_threshold: 256\n" +
                "groups:\n" +
                "connection_throttle: -1\n" +
                "stats: 13be5ac9-5731-4502-9ccc-c4a80163f14a\n" +
                "prevent_proxy_connections: false");

        writer.flush();
        writer.close();

    }

    @Override
    public String getFileName() {
        return "config.yml";
    }
}
