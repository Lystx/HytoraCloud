package cloud.hytora.common;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public class VersionInfo {


    @Getter
    @Setter
    private static VersionInfo currentVersion = new VersionInfo(Type.STABLE, 2.0);


    private final Type type;
    private final double version;

    public static VersionInfo fromString(String input) {
        if (input == null) {
            throw new NullPointerException("Can't parse Null-String to VersionInfo!");
        }
        if (input.contains("-")) {
            String[] data = input.split("-");
            Type type = Type.fromName(data[0]);
            try {
                double version = Double.parseDouble(data[1]);

                return new VersionInfo(type, version);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("DriverVersion needs to be following schema: 'TYPE-VERSION' (version is double)");
            }
        } else {
            throw new UnsupportedOperationException("DriverVersion needs to be following schema: 'TYPE-VERSION'");
        }
    }


    public boolean isUpToDate() {
        VersionInfo newestVersion = getNewestVersion("UNKNOWN");
        if (newestVersion.isUnknown()) {
            return true;
        }
        return isNewerOrSameAs(newestVersion);
    }

    public boolean isNewerOrSameAs(VersionInfo version) {
        return this.getVersion() >= version.getVersion();
    }

    public String formatCloudJarName() {
        return "HytoraCloud" + "-" + toString() + ".jar";
    }

    public boolean isNewerAs(VersionInfo version) {
        return this.getVersion() > version.getVersion();
    }

    @Override
    public String toString() {
        String name = type.name();
        return ((name.substring(0, 1).toUpperCase() + name.substring(1)) + "-" + version);
    }

    public boolean isUnknown() {
        return type == Type.UNKNOWN && version == 0.1;
    }

    public enum Type {

        UNKNOWN,

        SNAPSHOT,

        EXPERIMENTAL,

        STABLE,

        CUSTOM;


        public static Type fromName(String name) {
            return Arrays.stream(values()).filter(e -> e.name().equalsIgnoreCase(name)).findFirst().orElse(CUSTOM);
        }
    }


    private static VersionInfo NEWEST_VERSION;

    public static VersionInfo getNewestVersion(String offlineVersion) {
        if (NEWEST_VERSION == null) {
            if (!DriverUtility.hasInternetConnection()) {
                return new VersionInfo(Type.UNKNOWN, Integer.parseInt(offlineVersion));
            }
            try {
                Document document = DocumentFactory.newJsonDocumentByURL("https://raw.githubusercontent.com/Lystx/HytoraCloud/master/hytoraCloud-updater/application.json");
                String versionString = document.get("version").toString();
                return (NEWEST_VERSION = fromString(versionString));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return NEWEST_VERSION;
    }

}
