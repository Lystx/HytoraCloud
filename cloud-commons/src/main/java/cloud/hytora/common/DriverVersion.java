package cloud.hytora.common;

import cloud.hytora.common.progressbar.ProgressBar;
import cloud.hytora.common.progressbar.ProgressBarStyle;
import cloud.hytora.common.progressbar.ProgressPrinter;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import com.sun.org.apache.bcel.internal.generic.NEW;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Getter
@AllArgsConstructor
public class DriverVersion {

    // TODO: 07.08.2022 important
    public static DriverVersion getCurrentVersion() {
        return new DriverVersion(Type.SNAPSHOT, 1.5);
    }

    private static DriverVersion NEWEST_VERSION;

    public static DriverVersion getNewestVersion() {
        if (NEWEST_VERSION == null) {
            try {
                Document document = DocumentFactory.newJsonDocumentByURL("https://raw.githubusercontent.com/Lystx/HytoraCloud/master/application.json");

                String versionString = document.getEntry("version").toString();
                return (NEWEST_VERSION = fromString(versionString));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return NEWEST_VERSION;
    }

    private final Type type;
    private final double version;

    public static DriverVersion fromString(String input) {
        if (input.contains("-")) {
            String[] data = input.split("-");
            Type type = Type.fromName(data[0]);
            try {
                double version = Double.parseDouble(data[1]);

                return new DriverVersion(type, version);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("DriverVersion needs to be following schema: 'TYPE-VERSION' (version is double)");
            }
        } else {
            throw new UnsupportedOperationException("DriverVersion needs to be following schema: 'TYPE-VERSION'");
        }
    }


    public boolean isUpToDate() {

        return isNewerOrSameAs(getNewestVersion());
    }

    public boolean isNewerOrSameAs(DriverVersion version) {
        return this.getVersion() >= version.getVersion();
    }

    @Override
    public String toString() {
        String name = type.name();
        return ((name.substring(0, 1).toUpperCase() + name.substring(1)) + version);
    }

    public enum Type {

        SNAPSHOT,

        EXPERIMENTAL,

        STABLE,

        CUSTOM;


        public static Type fromName(String name) {
            return Arrays.stream(values()).filter(e -> e.name().equalsIgnoreCase(name)).findFirst().orElse(CUSTOM);
        }
    }
}
