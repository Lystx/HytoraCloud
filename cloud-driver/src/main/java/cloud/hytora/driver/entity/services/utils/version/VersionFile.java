package cloud.hytora.driver.entity.services.utils.version;

import cloud.hytora.driver.entity.services.CloudService;

import java.io.File;
import java.io.IOException;

public abstract class VersionFile {

    public abstract void applyFile(CloudService ICloudServer, File file) throws IOException;

    public abstract String getFileName();
}
