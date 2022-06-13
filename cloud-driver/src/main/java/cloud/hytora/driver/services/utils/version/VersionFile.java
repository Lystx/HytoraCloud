package cloud.hytora.driver.services.utils.version;

import cloud.hytora.driver.services.ServiceInfo;

import java.io.File;
import java.io.IOException;

public abstract class VersionFile {

    public abstract void applyFile(ServiceInfo serviceInfo, File file) throws IOException;

    public abstract String getFileName();
}
