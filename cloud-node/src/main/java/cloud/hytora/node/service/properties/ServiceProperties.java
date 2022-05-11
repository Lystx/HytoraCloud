package cloud.hytora.node.service.properties;


import lombok.Getter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Getter
public abstract class ServiceProperties {

    private final File file;
    private final int port;
    private String[] properties;

    private final boolean can;

    public ServiceProperties(final File file, final String child, final int port) {
        this.can = new File(file.getPath(), child).exists();
        this.file = new File(file.getPath(), child);

        this.port = port;
    }

    public void setProperties(final String[] properties) {
        this.properties = properties;
    }

    public void writeFile() {
        if (!this.can) {
            try (final FileWriter fileWriter = new FileWriter(this.file)) {
                for (final String line : this.properties) {
                    fileWriter.write(line + "\n");
                }
            } catch (IOException ignored) {
            }
        }
    }

}
