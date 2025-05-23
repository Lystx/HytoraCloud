package cloud.hytora.driver.services.utils.version.impl;

import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.utils.version.VersionFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class MinecraftProperties extends VersionFile {

    @Override
    public void applyFile(ICloudService cloudService, File file) throws IOException {

        if (!file.exists()) {
            URL resource = getClass().getResource("/impl/files/server.properties");
            if (resource != null) {
                FileUtils.copyURLToFile(resource, file);
            }
        }

        try {
            FileInputStream stream = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(stream);
            properties.setProperty("server-port", String.valueOf(cloudService.getPort()));
            properties.setProperty("server-ip", "127.0.0.1");
            properties.setProperty("max-players", String.valueOf(cloudService.getMaxPlayers()));
            properties.setProperty("allow-nether", String.valueOf(!cloudService.getProperties().fallbackValue(true).getBoolean("gameServer")));
            properties.setProperty("server-name", cloudService.getName());
            properties.setProperty("online-mode", "false");
            properties.setProperty("motd", cloudService.getMotd());
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            properties.save(fileOutputStream, "Edit by Cloud");
            fileOutputStream.close();
            stream.close();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public String getFileName() {
        return "server.properties";
    }
}
