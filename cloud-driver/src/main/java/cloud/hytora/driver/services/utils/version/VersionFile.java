package cloud.hytora.driver.services.utils.version;

import cloud.hytora.driver.services.ICloudService;

import java.io.File;
import java.io.IOException;

public abstract class VersionFile {

    public abstract void applyFile(ICloudService ICloudServer, File file) throws IOException;

    public abstract String getFileName();
}
