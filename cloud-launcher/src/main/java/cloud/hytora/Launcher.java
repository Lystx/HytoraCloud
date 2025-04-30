package cloud.hytora;


import cloud.hytora.commands.IncludeDependencyCommand;
import cloud.hytora.commands.IncludeRepositoryCommand;
import cloud.hytora.commands.LoggerCommand;
import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.VersionInfo;
import cloud.hytora.common.collection.ThreadRunnable;
import cloud.hytora.common.collection.WrappedException;
import cloud.hytora.common.logging.ConsoleColor;
import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.formatter.ColoredMessageFormatter;
import cloud.hytora.common.logging.handler.HandledAsyncLogger;
import cloud.hytora.common.logging.handler.HandledLogger;
import cloud.hytora.common.logging.handler.LogEntry;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.common.progressbar.ProgressBar;
import cloud.hytora.common.progressbar.ProgressBarStyle;
import cloud.hytora.common.progressbar.ProgressPrinter;
import cloud.hytora.context.ApplicationContext;
import cloud.hytora.context.IApplicationContext;
import cloud.hytora.dependency.Dependency;
import cloud.hytora.dependency.DependencyLoader;
import cloud.hytora.dependency.Repository;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.module.ModuleUpdater;
import cloud.hytora.script.api.IScriptLoader;
import cloud.hytora.script.api.impl.DefaultScriptLoader;
import cloud.hytora.script.defaults.DefaultModifyCommand;
import cloud.hytora.script.defaults.DefaultPrintCommand;
import cloud.hytora.script.defaults.DefaultRunCommand;
import cloud.hytora.script.defaults.DefaultVarCommand;
import lombok.Getter;
import org.fusesource.jansi.AnsiConsole;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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

    AtomicBoolean run = new AtomicBoolean(false);

    public Launcher(Logger logger, String[] args) throws IOException {
        this.args = args;
        this.logger = logger;
        this.dependencies = DriverUtility.newList();
        this.repositories = new HashMap<>();

        Document document = DocumentFactory.newJsonDocument(new File(LAUNCHER_DIR.toFile(), "auto_updater.json"));

        if (document.isEmpty()) {
            document.set("lastVersion", VersionInfo.getCurrentVersion().toString());
            document.saveToFile(new File(LAUNCHER_DIR.toFile(), "auto_updater.json"));
        }

        String lastVersion = document.getString("lastVersion");
        VersionInfo version = VersionInfo.fromString(lastVersion);
        VersionInfo.setCurrentVersion(version);


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
        IScriptLoader loader = new DefaultScriptLoader();
        loader.registerCommand(new DefaultRunCommand());
        loader.registerCommand(new DefaultPrintCommand());
        loader.registerCommand(new DefaultModifyCommand());
        loader.registerCommand(new DefaultVarCommand());

        loader.registerCommand(new LoggerCommand());
        loader.registerCommand(new IncludeDependencyCommand(this.dependencies::add));
        loader.registerCommand(new IncludeRepositoryCommand(repository -> repositories.put(repository.getName(), repository)));

        Path launcherFile = Paths.get("launcher.cloud");
        if (!Files.exists(launcherFile)) {
            try {
                FileUtils.copy(
                        ClassLoader.getSystemResourceAsStream("launcher.cloud"),
                        Files.newOutputStream(launcherFile)
                );
            } catch (IOException e) {
                System.out.println("ERORR: " + e.getMessage());
            }
        }

        if (run.get()) {
            return;
        }
        System.out.println("Loading script...");
        loader.loadScript(launcherFile)
                .runScript()
                .onTaskSucess(n -> {
                    if (run.get()) {
                        return;
                    }
                    run.set(true);
                    APPLICATION_FILE_URL = System.getProperty("cloud.hytora.launcher.application.file");
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
            VersionInfo newestVersion = VersionInfo.getNewestVersion("UNKNOWN");
            logger.info("Input: " + version + " | Your version: " + VersionInfo.getCurrentVersion() + " | Newest Version: " + newestVersion);
            if (!version.isUpToDate() || Objects.requireNonNull(LAUNCHER_VERSIONS.toFile().listFiles()).length == 0) {
                logger.info("Version (" + version + ") is outdated or your cloud.jar is not existing at all!");
                logger.info("==> Downloading latest HytoraCloud version [ver={}]...", newestVersion.toString());



                String startBatURL = getNewestVersionDownloadUrl("start.bat");
                File startBat = new File("start.bat");

                String startSHURL = getNewestVersionDownloadUrl("start.sh");
                File startSH = new File("start.sh");

                String cloudFileURL = getNewestVersionDownloadUrl(newestVersion.toString().toUpperCase() + ".jar");
                File cloudFile = new File(LAUNCHER_VERSIONS.toFile(), newestVersion.formatCloudJarName());

                if (!startBat.exists()) {
                    LauncherUtils.downloadVersion(startBatURL, startBat.toPath()).onTaskSucess(e -> {
                        logger.info("§8[§e1§8/§e3§8] Downloaded §8'§a{}§8'", "start.bat");
                    });
                }
                if (!startSH.exists()) {
                    LauncherUtils.downloadVersion(startSHURL, startSH.toPath()).onTaskSucess(e -> {
                        logger.info("§8[§e2§8/§e3§8] Downloaded §8'§a{}§8'", "start.sh");
                    });
                }
                if (!cloudFile.exists()) {
                    LauncherUtils.downloadVersion(cloudFileURL, cloudFile.toPath()).onTaskSucess(e -> {
                        logger.info("§8[§e3§8/§e3§8] Downloaded §8'§a{}§8'", newestVersion.formatCloudJarName());
                        try {
                            Document document = DocumentFactory.newJsonDocument(new File(LAUNCHER_DIR.toFile(), "auto_updater.json"));
                            document.set("lastVersion", newestVersion.toString());
                            document.saveToFile(new File(LAUNCHER_DIR.toFile(), "auto_updater.json"));
                            VersionInfo.setCurrentVersion(newestVersion);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                        try {
                            Thread.sleep(1500);
                            this.checkForUpdates(newestVersion, args);
                        } catch (InterruptedException e2) {
                            throw new WrappedException(e2);
                        }

                    }).onTaskFailed(e -> {
                        logger.info("§cFailed to Download CloudFile");
                        try {
                            throw e;
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                        }
                    });
                }

                return;
            }
        }

        if (!USE_AUTO_UPDATER) {
            logger.info("AutoUpdater has been disabled!");
            logger.info("Directly starting CloudNode...");
        } else if (!CUSTOM_VERSION.equalsIgnoreCase("null")) {
            logger.info("Custom version [val={}] has been selected! Skipping AutoUpdater", VersionInfo.fromString(CUSTOM_VERSION));
        } else {
            logger.info("Your CloudSystem is up to date with latest release! §8[§e{}§8]", VersionInfo.getCurrentVersion().toString());
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


    public static String getNewestVersionDownloadUrl(String data) {
        VersionInfo newestVersion = VersionInfo.getNewestVersion(VersionInfo.getCurrentVersion().toString());

        return "https://raw.github.com/Lystx/HytoraCloud/master/hytoraCloud-updater/" + newestVersion.getVersion() + "/" + data;
    }


    public String getBaseUrl() {
        VersionInfo newestVersion = VersionInfo.getNewestVersion("1.5");

        String urlString = BASE_URL;
        urlString = urlString.replace("{version}", String.valueOf(newestVersion.getVersion()));
        urlString = urlString.replace("{type}", String.valueOf(newestVersion.getType()));

        return urlString;
    }

    private void startApplication(String[] args, Collection<URL> dependencyResources) throws Throwable {
        LauncherUtils.clearConsole();
        System.out.println("\n");
        System.out.println("  _                     _ _                     _                     _____                                                 \n" +
                " | |                   | (_)                   | |                   |  __ \\                                                \n" +
                " | |     ___   __ _  __| |_ _ __   __ _        | | __ ___   ____ _   | |__) |___ ___ ___  ___  _   _ _ __ ___ ___ ___       \n" +
                " | |    / _ \\ / _` |/ _` | | '_ \\ / _` |   _   | |/ _` \\ \\ / / _` |  |  _  // _ / __/ __|/ _ \\| | | | '__/ __/ _ / __|      \n" +
                " | |___| (_) | (_| | (_| | | | | | (_| |  | |__| | (_| |\\ V | (_| |  | | \\ |  __\\__ \\__ | (_) | |_| | | | (_|  __\\__ \\_ _ _ \n" +
                " |______\\___/ \\__,_|\\__,_|_|_| |_|\\__, |   \\____/ \\__,_| \\_/ \\__,_|  |_|  \\_\\___|___|___/\\___/ \\__,_|_|  \\___\\___|___(_(_(_)\n" +
                "                                   __/ |                                                                                    \n" +
                "                                  |___/                                                                                     ");
        System.out.println("\n");


        ProgressBar pb = new ProgressBar(ProgressBarStyle.COLORED_UNICODE_BLOCK, 100);
        pb.setAppendProgress(false);
        pb.setBarLength(65);
        pb.setPrintAutomatically(true);
        pb.setExpandingAnimation(true);

        for (int i = 0; i < 65; i++) {
            if (pb.step()) {
                Thread.sleep(100);
            }
        }

        pb.setExtraMessage("Successfully downloaded! Cleaning up unused bytes...");
        pb.close("");
        System.out.println("\n");

        String jarName;
        if (!CUSTOM_VERSION.equalsIgnoreCase("null")) {
            jarName = VersionInfo.fromString(CUSTOM_VERSION).formatCloudJarName();
        } else {
            jarName = VersionInfo.getNewestVersion(CUSTOM_VERSION).formatCloudJarName();
        }
        Path targetPath = LAUNCHER_VERSIONS.resolve(jarName);

        String mainClass;
        try (JarFile jarFile = new JarFile(targetPath.toFile())) {
            mainClass = jarFile.getManifest().getMainAttributes().getValue("Main-Class");
        }

        if (mainClass == null) {
            throw new RuntimeException("Cannot find Main-Class from " + targetPath.toAbsolutePath());
        }

        dependencyResources.add(targetPath.toUri().toURL());

        IdentifiableClassLoader classLoader = new IdentifiableClassLoader(dependencyResources.toArray(new URL[0]));
        Method method = classLoader.loadClass(mainClass).getMethod("main", String[].class);

        Collection<String> arguments = DriverUtility.listOf(args);
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
