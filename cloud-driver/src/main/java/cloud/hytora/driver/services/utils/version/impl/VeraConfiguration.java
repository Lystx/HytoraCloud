package cloud.hytora.driver.services.utils.version.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import cloud.hytora.driver.services.utils.version.VersionFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class VeraConfiguration extends VersionFile {

    @Override
    public void applyFile(ICloudService server, File file) throws IOException {

        FileWriter writer = new FileWriter(file);

        List<ICloudService> services = CloudDriver.getInstance().getServiceManager().getAllServicesByEnvironment(SpecificDriverEnvironment.MINECRAFT);
        ICloudService firstService = services.isEmpty() ? null : services.get(0);

        String firstServerName = firstService == null ? "fallback": firstService.getName();
        String firstServerMotd = firstService == null ? "Default HytoraCloud Fallback" : firstService.getMotd();
        int firstServerPort = firstService == null ? 50000 : firstService.getPort();


        writer.write("{\n" +
                "  \"proxyPort\": " + server.getPort() + ",\n" +
                "  \"proxyName\": \"" + server.getName() + "\",\n" +
                "  \"webPort\": 80,\n" +
                "  \"maxPlayerSlots\": " + server.getMaxPlayers() + ",\n" +
                "  \"compression\": 255,\n" +
                "  \"ipForwarding\": true,\n" +
                "  \"privateMode\": false,\n" +
                "  \"targetPingAddress\": null,\n" +
                "  \"servers\": [\n" +
                "    {\n" +
                "      \"name\": \"" + firstServerName + "\",\n" +
                "      \"address\": {\n" +
                "        \"rawHost\": \"127.0.0.1\",\n" +
                "        \"host\": \"127.0.0.1\",\n" +
                "        \"port\": " + firstServerPort + "\n" +
                "      },\n" +
                "      \"accessPermission\": null\n" +
                "    }\n" +
                "  ],\n" +
                "  \"motd\": {\n" +
                "    \"version\": {\n" +
                "      \"name\": \"&8» &c&oLightweight\",\n" +
                "      \"protocol\": 47\n" +
                "    },\n" +
                "    \"players\": {\n" +
                "      \"online\": 0,\n" +
                "      \"max\": 250,\n" +
                "      \"sample\": null\n" +
                "    },\n" +
                "    \"description\": \"§8» §bVeraProxy §8§l‴§7§l‴ §7your §bproxy §8[§f1.8§7-§f1.18§8]\\n§8» §3Status §8× §cMaintenance §8┃ §7Proxy §8× §3Proxy-1\",\n" +
                "    \"favicon\": null\n" +
                "  }\n" +
                "}");


        writer.flush();
        writer.close();

    }

    @Override
    public String getFileName() {
        return "config.json";
    }
}
