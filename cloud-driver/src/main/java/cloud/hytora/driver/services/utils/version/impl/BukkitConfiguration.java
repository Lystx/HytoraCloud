package cloud.hytora.driver.services.utils.version.impl;

import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.utils.version.VersionFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class BukkitConfiguration extends VersionFile {

    @Override
    public void applyFile(ServiceInfo serviceInfo, File file) throws IOException {

        URL resource = getClass().getResource("/impl/files/bukkit.yml");
        if (resource != null) {
            FileUtils.copyURLToFile(resource, file);
        }
    }

    @Override
    public String getFileName() {
        return "bukkit.yml";
    }
}
