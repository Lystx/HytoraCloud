package cloud.hytora.module;

import cloud.hytora.Launcher;
import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.VersionInfo;
import cloud.hytora.common.collection.WrappedException;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.common.progressbar.HytoraProgressBar;
import cloud.hytora.common.progressbar.ProgressBarStyle;
import cloud.hytora.common.task.Task;

import cloud.hytora.document.Document;
import cloud.hytora.document.IEntry;
import lombok.SneakyThrows;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;


public class ModuleUpdater {

    private Launcher launcher;
    private Collection<ModuleInfo> cachedModules;

    public ModuleUpdater(Launcher launcher) {
        this.launcher = launcher;
    }

    @SneakyThrows
    public Collection<ModuleInfo> loadProvidedModules() {
        if (cachedModules != null) {
            return cachedModules;
        }
        Collection<ModuleInfo> modules = new ArrayList<>();

        Document document = Document.gsonByUrl("https://raw.githubusercontent.com/Lystx/HytoraCloud/master/hytoraCloud-updater/application.json");
        for (IEntry entry : document.getBundle("modules")) {
            Document doc = entry.toDocument();
            if (!doc.has("name") || !doc.has("version")) {
                launcher.getLogger().error("Couldn't find attributes for ModuleInfo 'name' or 'version' in following document:");
                launcher.getLogger().error(doc.asRawJsonString());
                continue;
            }
            ModuleInfo moduleInfo = new ModuleInfo(
                    doc.get("name").toString(),
                    VersionInfo.fromString(doc.get("version").toString())
            );
            launcher.getLogger().debug("Loaded ModuleInfo[name={}, url={}, version={}]", moduleInfo.getName(), moduleInfo.getUrl(), moduleInfo.getVersion());
            modules.add(moduleInfo);
        }
        return (cachedModules = modules);
    }


    public Task<ModuleInfo> updateModule(ModuleInfo module, String msg) {
        Task<ModuleInfo> task = Task.empty();
        String url = module.getUrl();
        String name = module.getName();

        ModuleInfo localModule = findCurrentModule(name);
        if (localModule == null || module.getVersion().isNewerAs(localModule.getVersion())) {
            Logger.constantInstance().info("  §8=> §7Downloading §8'§e{}§8'... §8[§eCurrent§8: §e{} | §eNewest§8: §e{}§8]", module.getName(), (localModule == null ? "Not existing" : localModule.getVersion()), module.getVersion());
            downloadModule(module, url, msg)
                    .onTaskSucess(e -> task.setResult(module))
                    .onTaskFailed(e -> {
                        if (e instanceof FileNotFoundException) {
                            launcher.getLogger().error("  §8=> §cCould not find updater-file §e{}'§c!", module.getVersion().formatModuleJarName(name));
                        } else {
                            WrappedException.throwWrapped(e);
                        }
                    });
        } else {
            Logger.constantInstance().info("  §8=> §7Module§8[§7name§8=§e{}§8, §7ver§8=§e{}§8] §7is §aup to date§8!", module.getName(), module.getVersion());
            task.setResult(module);
        }
        return task;
    }


    public static final AtomicBoolean HAS_UPDATED = new AtomicBoolean(false);

    public Task<Integer> updateModules() {
        Task<Integer> task = Task.empty();
        if (!HAS_UPDATED.get()) {
            HAS_UPDATED.set(true);
            Collection<ModuleInfo> modules = loadProvidedModules();

            int maxSize = modules.size();

            launcher.getLogger().info("  §8=> §7Found §e{} §7provided Modules§8!", maxSize);
            if (!Launcher.FAST_START) {
                Launcher.sleep(2000L);
            }

            int updateCount = 0;
            for (ModuleInfo module : modules) {
                if (updateCount >= maxSize) {
                    continue;
                }
                ModuleInfo moduleInfo = this.updateModule(module, "  §8=> §7Updated §e" + module.getName() + " §8[§ever§8=§e" + module.getVersion() + "§8]").syncUninterruptedly().orElse(null);
                if (moduleInfo != null) {
                    updateCount++;
                    if (!Launcher.FAST_START) {
                        Launcher.sleep(2000L);
                    }
                    if (updateCount >= maxSize) {
                        task.setResult(updateCount);
                    }
                }
            }
        } else {
            task.setFailure(new BootstrapMethodError());
        }
        return task;
    }


    public Task<Path> downloadModule(ModuleInfo module, String url, String msg) {
        Task<Path> task = Task.empty();
        HytoraProgressBar pb = new HytoraProgressBar(ProgressBarStyle.COLORED_UNICODE_BLOCK);

        pb.setTaskName("Downloading " + module.getName() + "...");

        for (File file : Arrays.stream(Objects.requireNonNull(Launcher.LAUNCHER_MODULES.toFile().listFiles()))
                .filter(file -> file.getName().endsWith(".jar"))
                .filter(file -> file.getName().contains(module.getName()))
                .collect(Collectors.toList())) {

            FileUtils.deleteFile(file.toPath());
        }

        DriverUtility.downloadVersion(url, Launcher.LAUNCHER_MODULES.resolve(module.getName() + "-" + module.getVersion() + ".jar"), pb, msg)
                .onTaskSucess(v -> {
                    if (!Launcher.FAST_START) {
                        Launcher.sleep(500L);
                    }
                    pb.close( " ");
                    task.setResult(v);
                })
                .onTaskFailed(task::setFailure);

        return task;
    }


    private ModuleInfo findCurrentModule(String name) {
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
        return Document.gson(jsonInput);
    }

}