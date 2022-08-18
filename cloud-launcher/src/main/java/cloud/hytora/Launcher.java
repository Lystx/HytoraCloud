package cloud.hytora;


import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.VersionInfo;
import cloud.hytora.common.collection.ThreadRunnable;
import cloud.hytora.common.collection.WrappedException;
import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.formatter.ColoredMessageFormatter;
import cloud.hytora.common.logging.handler.HandledAsyncLogger;
import cloud.hytora.common.logging.handler.HandledLogger;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.common.misc.ZipUtils;
import cloud.hytora.context.ApplicationContext;
import cloud.hytora.context.IApplicationContext;
import cloud.hytora.script.dependency.Dependency;
import cloud.hytora.script.dependency.DependencyLoader;
import cloud.hytora.script.dependency.Repository;
import cloud.hytora.script.module.ModuleUpdater;
import cloud.hytora.script.ScriptLoader;
import cloud.hytora.script.commands.IncludeDependencyCommandI;
import cloud.hytora.script.commands.IncludeRepositoryCommandI;
import cloud.hytora.script.commands.PrintCommandI;
import cloud.hytora.script.commands.RunIScriptCommand;
import lombok.Getter;
import org.fusesource.jansi.AnsiConsole;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarFile;

/**
 * Launcher highly inspired by
 * => <a href="https://github.com/CloudNetService/CloudNet-v3/blob/development/cloudnet-launcher/src/main/java/de/dytanic/cloudnet/launcher/CloudNetLauncher.java">CloudNet V3</a>
 */
@Getter
public class Launcher extends DriverUtility {

    public static final Path LAUNCHER_DIR = Paths.get("launcher/");
    public static final Path LAUNCHER_LIBS = LAUNCHER_DIR.resolve("libs/");
    public static final Path LAUNCHER_MODULES = LAUNCHER_DIR.resolve("modules/");
    public static final Path LAUNCHER_VERSIONS = LAUNCHER_DIR.resolve("versions/");


    public static String APPLICATION_FILE_URL;
    public static String DOWNLOAD_URL;
    public static String BASE_URL;
    public static String CUSTOM_VERSION;
    public static boolean USE_AUTO_UPDATER, USE_MODULE_AUTO_UPDATER;

    public static void main(String[] args) throws IOException {
        AnsiConsole.systemInstall();
        HandledLogger logger = new HandledAsyncLogger(LogLevel.fromName(System.getProperty("cloud.logging.level", "INFO")));
        Logger.setFactory(logger.addHandler(entry -> {
            String formatted = ColoredMessageFormatter.format(entry);
            System.out.println(formatted);
        }));
        System.setErr(logger.asPrintStream(LogLevel.ERROR));
        new Launcher(logger, args);
    }

    private final String[] args;
    private final Logger logger;

    private final Collection<Dependency> dependencies;
    private final Map<String, Repository> repositories;

    private final DependencyLoader dependencyLoader;
    private final ModuleUpdater moduleUpdater;

    public Launcher(Logger logger, String[] args) throws IOException {
        this.args = args;
        this.logger = logger;
        this.dependencies = newList();
        this.repositories = new HashMap<>();

        VersionInfo version = VersionInfo.getCurrentVersion();

        logger.log(LogLevel.NULL, "         ██▓    ▄▄▄       █    ██  ███▄    █  ▄████▄   ██░ ██ ▓█████  ██▀███  ");
        logger.log(LogLevel.NULL, "        ▓██▒   ▒████▄     ██  ▓██▒ ██ ▀█   █ ▒██▀ ▀█  ▓██░ ██▒▓█   ▀ ▓██ ▒ ██▒");
        logger.log(LogLevel.NULL, "        ▒██░   ▒██  ▀█▄  ▓██  ▒██░▓██  ▀█ ██▒▒▓█    ▄ ▒██▀▀██░▒███   ▓██ ░▄█ ▒");
        logger.log(LogLevel.NULL, "        ▒██░   ░██▄▄▄▄██ ▓▓█  ░██░▓██▒  ▐▌██▒▒▓▓▄ ▄██▒░▓█ ░██ ▒▓█  ▄ ▒██▀▀█▄  ");
        logger.log(LogLevel.NULL, "        ░██████▒▓█   ▓██▒▒▒█████▓ ▒██░   ▓██░▒ ▓███▀ ░░▓█▒░██▓░▒████▒░██▓ ▒██▒");
        logger.log(LogLevel.NULL, "        ░ ▒░▓  ░▒▒   ▓▒█░░▒▓▒ ▒ ▒ ░ ▒░   ▒ ▒ ░ ░▒ ▒  ░ ▒ ░░▒░▒░░ ▒░ ░░ ▒▓ ░▒▓░");
        logger.log(LogLevel.NULL, "        ░ ░ ▒  ░ ▒   ▒▒ ░░░▒░ ░ ░ ░ ░░   ░ ▒░  ░  ▒    ▒ ░▒░ ░ ░ ░  ░  ░▒ ░ ▒░");
        logger.log(LogLevel.NULL, "          ░ ░    ░   ▒    ░░░ ░ ░    ░   ░ ░ ░         ░  ░░ ░   ░     ░░   ░ ");
        logger.log(LogLevel.NULL, "            ░  ░     ░  ░   ░              ░ ░ ░       ░  ░  ░   ░  ░   ░     ");
        logger.log(LogLevel.NULL, "                                             ░                                ");

        logger.log(LogLevel.NULL, "        Launching your CloudSystem and checking for Updates...");
        logger.log(LogLevel.NULL, "            |=>    Your version: " + version + "   <=|        ");
        logger.log(LogLevel.NULL, "");

        logger.info("Loading ApplicationContext...");
        IApplicationContext context = new ApplicationContext(this);
        logger.info("Loaded ApplicationContext!");

        this.dependencyLoader = context.getInstance(DependencyLoader.class);
        this.moduleUpdater = context.getInstance(ModuleUpdater.class);

        logger.info("Loading 'launcher.cloud'...");
        ScriptLoader loader = ScriptLoader.getInstance();
        loader.registerCommand("runScript", new RunIScriptCommand());
        loader.registerCommand("print", new PrintCommandI());
        loader.registerCommand("includeDependency", new IncludeDependencyCommandI(this.dependencies::add));
        loader.registerCommand("includeRepository", new IncludeRepositoryCommandI(repository -> repositories.put(repository.getName(), repository)));

        Path launcherFile = Paths.get("launcher.cloud");
        if (!Files.exists(launcherFile)) {
            try {
                FileUtils.copy(
                        ClassLoader.getSystemResourceAsStream("launcher.cloud"),
                        Files.newOutputStream(launcherFile)
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        loader.executeScript(launcherFile)
                .onTaskSucess(n -> {
                    APPLICATION_FILE_URL = System.getProperty("cloud.hytora.launcher.application.file");
                    BASE_URL = System.getProperty("cloud.hytora.launcher.updater.baseUrl");
                    DOWNLOAD_URL = System.getProperty("cloud.hytora.launcher.updater.url").replace("{cloud.baseUrl}", BASE_URL);
                    USE_AUTO_UPDATER = System.getProperty("cloud.hytora.launcher.autoupdater").equalsIgnoreCase("true");
                    USE_MODULE_AUTO_UPDATER = System.getProperty("cloud.hytora.launcher.module.autoupdater").equalsIgnoreCase("true");
                    CUSTOM_VERSION = System.getProperty("cloud.hytora.launcher.customVersion");
                    logger.info("Script-Task was successful!");
                    logger.info("Setting up Files...");
                    try {
                        LAUNCHER_DIR.toFile().mkdirs();
                        LAUNCHER_VERSIONS.toFile().mkdirs();
                        LAUNCHER_LIBS.toFile().mkdirs();
                        LAUNCHER_MODULES.toFile().mkdirs();
                    } catch (Exception e) {
                        //files already exists
                    }

                    logger.info("Initialization done!");
                    logger.info("==> Now searching for updates...");
                    this.checkForUpdates(version, args);
                });
    }


    private void checkForUpdates(VersionInfo version, String... args) {
        if (USE_AUTO_UPDATER) {
            logger.info("Checking for Updates...");
            if (!version.isUpToDate() || LAUNCHER_VERSIONS.toFile().listFiles().length == 0) {
                logger.info("Version (" + version + ") is outdated or your cloud.jar is not existing at all!");
                logger.info("==> Downloading latest HytoraCloud version...");

                Path zippedFile = LAUNCHER_DIR.resolve("hytoraCloud.zip");
                LauncherUtils.downloadVersion(getNewestVersionDownloadUrl(), zippedFile).onTaskSucess(v -> {
                    logger.info("Downloaded latest RELEASE!");
                    logger.info("Unzipping...");

                    ZipUtils.unzipDirectory(zippedFile, "unzipped");
                    try {
                        Files.delete(zippedFile);
                        Path cloudInFile = Paths.get("unzipped/cloud.jar");
                        Files.copy(cloudInFile, LAUNCHER_VERSIONS.resolve(VersionInfo.getNewestVersion().formatCloudJarName()));
                        FileUtils.delete(Paths.get("unzipped"));
                        logger.info("Unzipped and moved HytoraCloud-Jar to its folder!");

                        try {
                            Thread.sleep(1500);
                            this.checkForUpdates(version, args);
                        } catch (InterruptedException e) {
                            throw new WrappedException(e);
                        }
                    } catch (IOException e) {
                        throw new WrappedException(e);
                    }

                });
                return;
            }

        }
        
        if (!USE_AUTO_UPDATER) {
            logger.info("AutoUpdater has been disabled!");
            logger.info("Directly starting CloudNode...");
        } else if (!CUSTOM_VERSION.equalsIgnoreCase("null")) {
            logger.info("Custom version [val={}] has been selected! Skipping AutoUpdater", VersionInfo.fromString(CUSTOM_VERSION));
        } else {
            logger.info("Cloud is up to date with latest release!");
        }

        ThreadRunnable runnable = new ThreadRunnable(() -> {
            Collection<URL> dependencyResources;
            try {
                dependencyResources = dependencyLoader.loadDependencyURLs();
            } catch (IOException exception) {
                throw new RuntimeException("Unable to install needed dependencies!", exception);
            }

            if (dependencies.isEmpty()) {
                logger.error("==> Error: No dependencies found to start Application with! Please restart Launcher!");
                logger.error("==> Error: If the error occurs again, please contact the Developer!");
                return;
            }
            logger.info("");

            try {
                startApplication(args, dependencyResources);
            } catch (Throwable exception) {
                throw new RuntimeException("Failed to start the application!", exception);
            }
        });

        if (USE_MODULE_AUTO_UPDATER) {
            logger.info("Checking for Module-Updates!");
            moduleUpdater.updateModules()
                    .onTaskSucess(n -> {
                        if (n > 0) {
                            logger.info("Updated {} Modules!", n);
                        } else {
                            logger.info("All Modules are up to date!");
                        }
                        logger.info("Continuing to cloud process...");
                        runnable.runAsync();
                    })
                    .onTaskFailed(e -> {
                        logger.info("Something went wrong whilst trying to update Modules!");
                        WrappedException.throwWrapped(e);
                    });
        } else {
            logger.info("Module Updating is disabled! Skipping...");
            runnable.runAsync();
        }

    }


    public String getNewestVersionDownloadUrl() {
        VersionInfo newestVersion = VersionInfo.getNewestVersion();

        String urlString = DOWNLOAD_URL;
        urlString = urlString.replace("{version}", String.valueOf(newestVersion.getVersion()));
        urlString = urlString.replace("{type}", String.valueOf(newestVersion.getType()));

        return urlString;
    }


    public String getBaseUrl() {
        VersionInfo newestVersion = VersionInfo.getNewestVersion();

        String urlString = BASE_URL;
        urlString = urlString.replace("{version}", String.valueOf(newestVersion.getVersion()));
        urlString = urlString.replace("{type}", String.valueOf(newestVersion.getType()));

        return urlString;
    }

    private void startApplication(String[] args, Collection<URL> dependencyResources) throws Throwable {
        
        String jarName;
        if (!CUSTOM_VERSION.equalsIgnoreCase("null")) {
            jarName = VersionInfo.fromString(CUSTOM_VERSION).formatCloudJarName();
        } else {
            jarName = VersionInfo.getNewestVersion().formatCloudJarName();
        }
        Path targetPath = LAUNCHER_VERSIONS.resolve(jarName);
        Path driverTargetPath = LAUNCHER_VERSIONS.resolve("api.jar");

        String mainClass;
        try (JarFile jarFile = new JarFile(targetPath.toFile())) {
            mainClass = jarFile.getManifest().getMainAttributes().getValue("Main-Class");
        }

        if (mainClass == null) {
            throw new RuntimeException("Cannot find Main-Class from " + targetPath.toAbsolutePath());
        }

        dependencyResources.add(targetPath.toUri().toURL());
       // dependencyResources.add(driverTargetPath.toUri().toURL());

        IdentifiableClassLoader classLoader = new IdentifiableClassLoader(dependencyResources.toArray(new URL[0]));
        Method method = classLoader.loadClass(mainClass).getMethod("main", String[].class);

        Collection<String> arguments = listOf(args);
        arguments.add("--moduleFolder=" + LAUNCHER_MODULES.toString());

        Thread thread = new Thread(() -> {
            try {

                try {
                    method.invoke(null, (Object) arguments.toArray(new String[0]));
                } catch (IllegalAccessException | InvocationTargetException exception) {
                    exception.printStackTrace();
                }
            } catch (Exception e) {

            }
        }, "Application-Thread");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setContextClassLoader(classLoader);
        thread.start();

    }
}
