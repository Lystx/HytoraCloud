package cloud.hytora.module;

import cloud.hytora.Launcher;
import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.VersionInfo;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.common.progressbar.ProgressBar;
import cloud.hytora.common.progressbar.ProgressBarStyle;
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


    public Task<ModuleInfo> updateModule(ModuleInfo module) {
        Task<ModuleInfo> task = Task.empty();
        Task.runAsync(() -> {

            String url = module.getUrl();
            String name = module.getName();
            VersionInfo currentVersion = module.getVersion();

            //replace url place holders
            url = url.replace("{cloud.baseUrl}", launcher.getBaseUrl());
            url = url.replace("{module.name}", name);
            url = url.replace("{module.version}", currentVersion.toString());

            ModuleInfo localModule = findCurrentModule(name, url);
            if (localModule == null || module.getVersion().isNewerAs(localModule.getVersion())) {
                Logger.constantInstance().info("'{}' is either not existing or needs to be updated to Version '{}' [Current-Version: {}]", module.getName(), module.getVersion(), (localModule == null ? "Not existing" : localModule.getVersion()));
                downloadModule(module, url)
                        .onTaskSucess(e -> task.setResult(module))
                        .onTaskFailed(task::setFailure);
            } else {
                Logger.constantInstance().info("Module[name={}, ver={}] is up to date", module.getName(), module.getVersion());
                task.setResult(module);
            }
        });
        return task;
    }


    public Task<Integer> updateModules() {
        Task<Integer> task = Task.empty();
        Collection<ModuleInfo> modules = loadProvidedModules();

        AtomicInteger updateCount = new AtomicInteger(0);
        for (ModuleInfo module : modules) {
            this.updateModule(module)
                    .onTaskSucess(m -> {
                        if (updateCount.incrementAndGet() >= modules.size()) {
                            task.setResult(updateCount.get());
                        }
                    })
                    .onTaskFailed(task::setFailure);
        }
        return task;
    }


    public Task<Path> downloadModule(ModuleInfo module, String url) {
        Task<Path> task = Task.empty();
        ProgressBar pb = new ProgressBar(ProgressBarStyle.ASCII, 100L);

        pb.setAppendProgress(false);
        pb.setFakePercentage(30, 100);
        pb.setPrintAutomatically(true);
        pb.setExpandingAnimation(true);
        pb.setTaskName("Downloading " + module.getName());


        DriverUtility.downloadVersion(url, Launcher.LAUNCHER_MODULES.resolve(module.getName() + "-" + module.getVersion() + ".jar"), pb)
                .onTaskSucess(v -> {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pb.close("=> Downloaded '{}-{}.jar'!", module.getName(), module.getVersion());
                    task.setResult(v);
                })
                .onTaskFailed(e -> {
                    task.setFailure(e);
                    Logger.constantInstance().error("Couldn't download Module[val={}, url={}] Error: {}", module.getName(), url, e);
                });

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
        Document document = loadDocument(moduleFile.toFile(), "config.json");

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