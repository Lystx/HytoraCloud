package cloud.hytora.driver.entity.services.utils.version.impl;

import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.utils.version.VersionFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ServerIconFile extends VersionFile {
    @Override
    public void applyFile(CloudService ICloudServer, File file) throws IOException {

        if (!file.exists()) { //copying server icon if none already provided
            URL resource = getClass().getResource("/impl/files/server-icon.png");
            if (resource != null) {
                FileUtils.copyURLToFile(resource, file);
            }
        }
    }

    @Override
    public String getFileName() {
        return "server-icon.png";
    }
}
