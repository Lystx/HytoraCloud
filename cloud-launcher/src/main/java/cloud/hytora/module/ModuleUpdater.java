package cloud.hytora.module;

import cloud.hytora.Launcher;
import cloud.hytora.LauncherUtils;
import cloud.hytora.common.VersionInfo;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.common.task.Task;
import cloud.hytora.context.annotations.CacheContext;
import cloud.hytora.context.annotations.ApplicationParticipant;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.document.IEntry;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@ApplicationParticipant
public class ModuleUpdater {

    @CacheContext
    private Launcher launcher;
    private Collection<ModuleInfo> cachedModules;

    @SneakyThrows
    public Collection<ModuleInfo> loadProvidedModules() {
        if (cachedModules != null) {
            return cachedModules;
        }
        Collection<ModuleInfo> modules = new ArrayList<>();

        Document document = Document.newJsonDocumentByURL(Launcher.APPLICATION_FILE_URL);
        for (IEntry entry : document.getBundle("modules")) {
            Document doc = entry.toDocument();
            if (!doc.has("name") || !doc.has("url") || !doc.has("version")) {
                launcher.getLogger().error("Couldn't find attributes for ModuleInfo 'name' or 'url' or 'version' in following document:");
                launcher.getLogger().error(doc.asRawJsonString());
                continue;
            }
            System.out.println(doc);
            ModuleInfo moduleInfo = new ModuleInfo(
                    doc.get("name").toString(),
                    doc.get("url").toString(),
                    VersionInfo.fromString(doc.get("version").toString())
            );
            launcher.getLogger().debug("Loaded ModuleInfo[name={}, url={}, version={}]", moduleInfo.getName(), moduleInfo.getUrl(), moduleInfo.getVersion());
            modules.add(moduleInfo);
        }
        return (cachedModules = modules);
    }

    public Task<Integer> updateModules() {
        Task<Integer> task = Task.empty();
        Collection<ModuleInfo> modules = loadProvidedModules();

        AtomicInteger updateCount = new AtomicInteger(0);
        for (ModuleInfo module : modules) {
            String url = module.getUrl();
            String name = module.getName();
            VersionInfo currentVersion = module.getVersion();

            //replace url place holders
            url = url.replace("{cloud.baseUrl}", launcher.getBaseUrl());
            url = url.replace("{module.name}", name);
            url = url.replace("{module.version}", currentVersion.toString());

            ModuleInfo localModule = findCurrentModule(name, url);
            if (localModule == null || module.getVersion().isNewerAs(localModule.getVersion())) {
                launcher.getLogger().info("Module[val={}] is either not existing or needs to be updated to Version[val={}, url={}]", module.getName(), module.getVersion(), url);
                String finalUrl = url;
                LauncherUtils.downloadVersion(url, Launcher.LAUNCHER_MODULES.resolve(module.getName() + "-" + module.getVersion() + ".jar"))
                        .onTaskSucess(v -> {
                            launcher.getLogger().info("Downloaded " + module.getName());
                            updateCount.set((updateCount.get() + 1));
                        })
                        .onTaskFailed(e -> {
                            launcher.getLogger().error("Couldn't download Module[val={}, url={}] Error: {}", module.getName(), finalUrl, e);
                        });
            } else {

            }
        }

        return task;
    }

    private ModuleInfo findCurrentModule(String name, String url) {
        Path moduleFile = FileUtils.list(Launcher.LAUNCHER_MODULES)
                .filter(path -> path.getFileName().toString().endsWith(".jar"))
                .filter(path -> path.getFileName().toString().contains(name))
                .findFirst()
                .orElse(null);
        if (moduleFile == null) {
            return null;
        }
        Document document = loadDocument(moduleFile.toFile(), "version.json");

        return document == null ? null
                : new ModuleInfo(
                name,
                url,
                VersionInfo.fromString(document.getString("version"))
        );
    }


    private String loadJson(File jarFile, String filename) {
        try {
            JarFile jf = new JarFile(jarFile);
            JarEntry je = jf.getJarEntry(filename);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(jf.getInputStream(je)))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    builder.append(line);
                }
                jf.close();
                br.close();
                return builder.toString();
            } catch (Exception e) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    private Document loadDocument(File jarFile, String filename) {
        String jsonInput = this.loadJson(jarFile, filename);
        if (jsonInput == null) {
            return null;
        }
        return DocumentFactory.newJsonDocument(jsonInput);
    }

}