package cloud.hytora.driver.entity.services.utils.version;

import cloud.hytora.common.misc.ReflectionUtils;
import cloud.hytora.document.Document;
import cloud.hytora.driver.entity.player.connection.ProtocolVersion;
import cloud.hytora.driver.entity.services.utils.version.impl.*;
import cloud.hytora.driver.entity.services.utils.SpecificDriverEnvironment;
import com.google.gson.reflect.TypeToken;


import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Getter
public enum ServiceVersion {

    //=======================================================
    //
    //           PROXY SOFTWARE
    //
    //
    //=========================================================

    BUNGEECORD(
            VersionType.BUNGEE,
            "https://ci.md-5.net/job/BungeeCord/lastSuccessfulBuild/artifact/bootstrap/target/BungeeCord.jar",
            ProtocolVersion.UNKNOWN, "BungeeCord",
            "latest",
            BungeeConfiguration.class, ServerIconFile.class //configurations
    ),
    VERAPROXY(
            VersionType.VERA,
            "",
            ProtocolVersion.UNKNOWN, "VeraProxy",
            "latest",
            VeraConfiguration.class, ServerIconFile.class //configurations
    ),
    /*WATERFALL(
            VersionType.BUNGEE,
            null, //auto build url link
            "Waterfall",
            "latest",
            BungeeConfiguration.class, ServerIconFile.class //configurations
    ),
    VELOCITY_3(
            VersionType.VELOCITY,
            "https://download1525.mediafire.com/li1r4i4glplg/srbg7o66ig2oei1/velocity-3.0.1.jar",
            "Velocity",
            "3.0.1",
            VelocityConfiguration.class, ServerIconFile.class //configurations
    ),*/

    //=======================================================
    //
    //           SPIGOT SOFTWARE
    //
    //
    //=========================================================

    SPIGOT_1_18_1(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.18.1.jar",
            ProtocolVersion.v1_18, "Spigot",
            "1.18.1",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_18(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.18.jar",
            ProtocolVersion.v1_18, "Spigot",
            "1.18",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_17_1(
            VersionType.SPIGOT,
            "https://download.getbukkit.org/spigot/spigot-1.17.1.jar",
            ProtocolVersion.v1_17_1, "Spigot",
            "1.17.1",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_17(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.17.jar",
            ProtocolVersion.v1_17, "Spigot",
            "1.17",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_16_5(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.16.5.jar",
            ProtocolVersion.v1_16_4, "Spigot",
            "1.16.5",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_16_4(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.16.4.jar",
            ProtocolVersion.v1_16_4, "Spigot",
            "1.16.4",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_16_3(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.16.3.jar",
            ProtocolVersion.v1_16_3, "Spigot",
            "1.16.3",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_16_2(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.16.2.jar",
            ProtocolVersion.v1_16_2, "Spigot",
            "1.16.2",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_16_1(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.16.1.jar",
            ProtocolVersion.v1_16_1, "Spigot",
            "1.16.1",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_16(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.16.jar",
            ProtocolVersion.v1_16, "Spigot",
            "1.16",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_15_2(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.15.2.jar",
            ProtocolVersion.v1_15_2, "Spigot",
            "1.15.2",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_15_1(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.15.1.jar",
            ProtocolVersion.v1_15_1, "Spigot",
            "1.15.1",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_15(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.15.jar",
            ProtocolVersion.v1_15, "Spigot",
            "1.15",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_14_4(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.14.4.jar",
            ProtocolVersion.v1_14_4, "Spigot",
            "1.14.4",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_14_3(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.14.3.jar",
            ProtocolVersion.v1_14_3, "Spigot",
            "1.14.3",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_14_2(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.14.2.jar",
            ProtocolVersion.v1_14_2, "Spigot",
            "1.14.2",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_14(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.14.jar",
            ProtocolVersion.v1_14, "Spigot",
            "1.14",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_13_2(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.13.2.jar",
            ProtocolVersion.v1_13_2, "Spigot",
            "1.13.2",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_13_1(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.13.1.jar",
            ProtocolVersion.v1_13_1, "Spigot",
            "1.13.1",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_13(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.13.jar",
            ProtocolVersion.v1_13, "Spigot",
            "1.13",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_12_2(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.12.2.jar",
            ProtocolVersion.v1_12_2, "Spigot",
            "1.12.2",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_12_1(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.12.1.jar",
            ProtocolVersion.v1_12_1, "Spigot",
            "1.12.1",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_12(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.12.jar",
            ProtocolVersion.v1_12, "Spigot",
            "1.12",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_11_1(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.11.1.jar",
            ProtocolVersion.v1_11_1, "Spigot",
            "1.11.1",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_11(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.11.jar",
            ProtocolVersion.v1_11, "Spigot",
            "1.11",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_10(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.10-R0.1-SNAPSHOT-latest.jar",
            ProtocolVersion.v1_10, "Spigot",
            "1.10",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_9_3(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.9.3-R0.1-SNAPSHOT-latest.jar",
            ProtocolVersion.v1_9_3, "Spigot",
            "1.9.3",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_9_2(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.9.2-R0.1-SNAPSHOT-latest.jar",
            ProtocolVersion.v1_9_2, "Spigot",
            "1.9.2",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_9(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.9-R0.1-SNAPSHOT-latest.jar",
            ProtocolVersion.v1_9, "Spigot",
            "1.9",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    SPIGOT_1_8_8(
            VersionType.SPIGOT,
            "https://cdn.getbukkit.org/spigot/spigot-1.8.8-R0.1-SNAPSHOT-latest.jar",
            ProtocolVersion.v1_8, "Spigot",
            "1.8.8",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),

    //<======================== CUT GLOWSTONE & SPIGIOT ============================>

    GLOWSTONE_1_16_5(
            VersionType.GLOWSTONE,
            "https://repo.glowstone.net/service/rest/v1/search/assets/download?group=net.glowstone&name=glowstone&sort=version&repository=snapshots&maven.baseVersion=2021.7.1-SNAPSHOT&direction=desc&maven.classifier=",
            ProtocolVersion.v1_16_4, "Glowstone",
            "1.16.5",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class //configurations
    ),
    GLOWSTONE_1_12_2(
            VersionType.GLOWSTONE,
            "https://github.com/GlowstoneMC/Glowstone/releases/download/2021.7.0/glowstone.jar",
            ProtocolVersion.v1_12_2, "Glowstone",
            "1.12.2",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class, GlowstoneConfiguration.class //configurations
    ),
    GLOWSTONE_1_8_8(
            VersionType.GLOWSTONE,
            "https://github.com/GlowstoneMC/Glowstone/releases/download/2021.8.0/glowstone.jar",
            ProtocolVersion.v1_8, "Glowstone",
            "1.8.8",
            BukkitConfiguration.class, SpigotConfiguration.class, MinecraftProperties.class, GlowstoneConfiguration.class //configurations
    );

    private final VersionType type;
    private final ProtocolVersion protocolVersion;
    private final SpecificDriverEnvironment environment;
    private final String url;
    private final String title;
    private final String version;
    private final Class<? extends VersionFile>[] versionFiles;

    private VersionFile[] cachedInstantiatedVersionFiles;

    @SafeVarargs
    ServiceVersion(VersionType type, String url, ProtocolVersion protocolVersion, String title, String version, Class<? extends VersionFile>... versionFiles) {
        this.type = type;
        this.environment = type.getEnvironment();
        this.protocolVersion = protocolVersion;
        this.title = title;
        this.version = version;
        this.versionFiles = versionFiles;

        String finalUrl;
        if (url == null) {
            int build = this.getBuildNumber(title, version);
            String paperVersion = version;
            if (paperVersion.equals("latest")) {
                paperVersion = this.getLatestVersion(title);
            }
            finalUrl = "https://papermc.io/api/v2/projects/" + title + "/versions/" + paperVersion + "/builds/" + build + "/downloads/" + title + "-" + paperVersion + "-" + build + ".jar";
        } else {
            finalUrl = url;
        }
        this.url = finalUrl;
    }


    public VersionFile[] instantiateVersionFiles() {
        if (cachedInstantiatedVersionFiles == null) {
            cachedInstantiatedVersionFiles = new VersionFile[this.versionFiles.length];
            for (int i = 0; i < this.versionFiles.length; i++) {
                cachedInstantiatedVersionFiles[i] = ReflectionUtils.createEmpty(this.versionFiles[i]);
            }
        }
        return cachedInstantiatedVersionFiles;
    }


    public static ServiceVersion[] valuesOf(VersionType type) {
        return Arrays.stream(values()).filter(v -> v.getType() == type).toArray(ServiceVersion[]::new);
    }

    public boolean isProxy() {
        return this.environment == SpecificDriverEnvironment.PROXY;
    }

    public boolean isMinecraft() {
        return this.environment != SpecificDriverEnvironment.PROXY;
    }

    public String getJar() {
        return this.title + (!this.version.equalsIgnoreCase("latest") ? "-" + this.version : "") + ".jar";
    }

    private int getBuildNumber(@NotNull String title, final @NotNull String version) {
        String paperVersion = version;
        if (paperVersion.equals("latest")) {
            paperVersion = this.getLatestVersion(title);
        }
        Document document = this.paperApiRequest("https://papermc.io/api/v2/projects/" + title + "/versions/" + paperVersion + "/");
        if (document != null) {
            List<Integer> buildNumbers = document.getInstance("builds", TypeToken.getParameterized(List.class, Integer.class).getType());
            return buildNumbers.get(buildNumbers.size() - 1);
        } else {
            return -1;
        }
    }

    private String getLatestVersion(final @NotNull String title) {
        Document document = this.paperApiRequest("https://papermc.io/api/v2/projects/" + title);
        if (document != null) {
            List<String> versions = document.getInstance("versions", TypeToken.getParameterized(List.class, String.class).getType());
            return versions.get(versions.size() - 1);
        } else {
            return "Unknown";
        }
    }

    private Document paperApiRequest(final @NotNull String urlString) {
        try {
            URL url = new URL(urlString);
            InputStream inputStream = url.openStream();
            InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());
            Document document = Document.gson(inputStreamReader);
            inputStreamReader.close();
            inputStream.close();
            return document;
        } catch (IOException e) {
        }
        return null;
    }

}

