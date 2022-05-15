package cloud.hytora.driver.services.utils;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import com.google.gson.reflect.TypeToken;
import cloud.hytora.driver.CloudDriver;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter @AllArgsConstructor
public enum ServiceVersion {

    BUNGEE("https://ci.md-5.net/job/BungeeCord/lastBuild/artifact/bootstrap/target/BungeeCord.jar", "BungeeCord", "latest", ServiceTypes.PROXY),
    WATERFALL("waterfall", "latest", ServiceTypes.PROXY),
    PAPER_1_18_1("paper", "1.18.1", ServiceTypes.SERVER),
    PAPER_1_1_8("paper", "1.8.8", ServiceTypes.SERVER),

    //Normal Spigot
    SPIGOT_1_18_1("https://download.getbukkit.org/spigot/spigot-1.18.1.jar", "Spigot", "1.18.1", ServiceTypes.SERVER),
    SPIGOT_1_18("https://download.getbukkit.org/spigot/spigot-1.17.jar", "Spigot", "1.18", ServiceTypes.SERVER),
    SPIGOT_1_17_1("https://download.getbukkit.org/spigot/spigot-1.17.1.jar", "Spigot", "1.17.1", ServiceTypes.SERVER),
    SPIGOT_1_17("https://cdn.getbukkit.org/spigot/spigot-1.17.jar", "Spigot", "1.17", ServiceTypes.SERVER),
    SPIGOT_1_16_5("https://cdn.getbukkit.org/spigot/spigot-1.16.5.jar", "Spigot", "1.16.5", ServiceTypes.SERVER),
    SPIGOT_1_16_4("https://cdn.getbukkit.org/spigot/spigot-1.16.4.jar", "Spigot", "1.16.4", ServiceTypes.SERVER),
    SPIGOT_1_16_3("https://cdn.getbukkit.org/spigot/spigot-1.16.3.jar", "Spigot", "1.16.3", ServiceTypes.SERVER),
    SPIGOT_1_16_2("https://cdn.getbukkit.org/spigot/spigot-1.16.2.jar", "Spigot", "1.16.2", ServiceTypes.SERVER),
    SPIGOT_1_16_1("https://cdn.getbukkit.org/spigot/spigot-1.16.1.jar", "Spigot", "1.16.1", ServiceTypes.SERVER),
    SPIGOT_1_16("https://cdn.getbukkit.org/spigot/spigot-1.16.jar", "Spigot", "1.16", ServiceTypes.SERVER),
    SPIGOT_1_15_2("https://cdn.getbukkit.org/spigot/spigot-1.15.2.jar", "Spigot", "1.15.2", ServiceTypes.SERVER),
    SPIGOT_1_15_1("https://cdn.getbukkit.org/spigot/spigot-1.15.1.jar", "Spigot", "1.15.1", ServiceTypes.SERVER),
    SPIGOT_1_15("https://cdn.getbukkit.org/spigot/spigot-1.15.jar", "Spigot", "1.15", ServiceTypes.SERVER),
    SPIGOT_1_14_4("https://cdn.getbukkit.org/spigot/spigot-1.14.4.jar", "Spigot", "1.14.4", ServiceTypes.SERVER),
    SPIGOT_1_14_3("https://cdn.getbukkit.org/spigot/spigot-1.14.3.jar", "Spigot", "1.14.3", ServiceTypes.SERVER),
    SPIGOT_1_14_2("https://cdn.getbukkit.org/spigot/spigot-1.14.2.jar", "Spigot", "1.14.2", ServiceTypes.SERVER),
    SPIGOT_1_14("https://cdn.getbukkit.org/spigot/spigot-1.14.jar", "Spigot", "1.14", ServiceTypes.SERVER),
    SPIGOT_1_13_2("https://cdn.getbukkit.org/spigot/spigot-1.13.2.jar", "Spigot", "1.13.2", ServiceTypes.SERVER),
    SPIGOT_1_13_1("https://cdn.getbukkit.org/spigot/spigot-1.13.1.jar", "Spigot", "1.13.1", ServiceTypes.SERVER),
    SPIGOT_1_13("https://cdn.getbukkit.org/spigot/spigot-1.13.jar", "Spigot", "1.13", ServiceTypes.SERVER),
    SPIGOT_1_12_2("https://cdn.getbukkit.org/spigot/spigot-1.12.2.jar", "Spigot", "1.12.2", ServiceTypes.SERVER),
    SPIGOT_1_12_1("https://cdn.getbukkit.org/spigot/spigot-1.12.1.jar", "Spigot", "1.12.1", ServiceTypes.SERVER),
    SPIGOT_1_12("https://cdn.getbukkit.org/spigot/spigot-1.12.jar", "Spigot", "1.12", ServiceTypes.SERVER),
    SPIGOT_1_11_1("https://cdn.getbukkit.org/spigot/spigot-1.11.1.jar", "Spigot", "1.11.1", ServiceTypes.SERVER),
    SPIGOT_1_11("https://cdn.getbukkit.org/spigot/spigot-1.11.jar", "Spigot", "1.11", ServiceTypes.SERVER),
    SPIGOT_1_10("https://cdn.getbukkit.org/spigot/spigot-1.10-R0.1-SNAPSHOT-latest.jar", "Spigot", "1.10", ServiceTypes.SERVER),
    SPIGOT_1_9_4("https://cdn.getbukkit.org/spigot/spigot-1.9.4-R0.1-SNAPSHOT-latest.jar", "Spigot", "1.9.4", ServiceTypes.SERVER),
    SPIGOT_1_9_2("https://cdn.getbukkit.org/spigot/spigot-1.9.2-R0.1-SNAPSHOT-latest.jar", "Spigot", "1.9.2", ServiceTypes.SERVER),
    SPIGOT_1_9("https://cdn.getbukkit.org/spigot/spigot-1.9-R0.1-SNAPSHOT-latest.jar", "Spigot", "1.9", ServiceTypes.SERVER),
    SPIGOT_1_8_8("https://cdn.getbukkit.org/spigot/spigot-1.8.8-R0.1-SNAPSHOT-latest.jar", "Spigot", "1.8.8", ServiceTypes.SERVER),
    ;

    private final String url;
    private final String title;
    private final String version;
    private final ServiceTypes serviceTypes;


    ServiceVersion(String title, String version, ServiceTypes serviceTypes) {
        int build = this.getBuildNumber(title, version);
        String paperVersion = version;
        if (paperVersion.equals("latest")) {
            paperVersion = this.getLatestVersion(title);
        }
        this.url = "https://papermc.io/api/v2/projects/" + title + "/versions/" + paperVersion + "/builds/" + build + "/downloads/" + title + "-" + paperVersion + "-" + build + ".jar";
        this.title = title;
        this.version = version;
        this.serviceTypes = serviceTypes;
    }

    public boolean isProxy() {
        return this.serviceTypes == ServiceTypes.PROXY;
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
            Document document = DocumentFactory.newJsonDocument(inputStreamReader);
            inputStreamReader.close();
            inputStream.close();
            return document;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

