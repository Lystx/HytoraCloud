package cloud.hytora;


import cloud.hytora.commands.IncludeDependencyCommand;
import cloud.hytora.commands.IncludeRepositoryCommand;
import cloud.hytora.commands.LoggerCommand;
import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.Snowflake;
import cloud.hytora.common.VersionInfo;
import cloud.hytora.common.collection.ThreadRunnable;
import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.formatter.ColoredMessageFormatter;
import cloud.hytora.common.logging.handler.HandledAsyncLogger;
import cloud.hytora.common.logging.handler.HandledLogger;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.dependency.Dependency;
import cloud.hytora.dependency.DependencyLoader;
import cloud.hytora.dependency.Repository;
import cloud.hytora.document.Document;
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
import java.net.URLClassLoader;
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


    public static String CUSTOM_VERSION;
    public static boolean USE_AUTO_UPDATER, USE_MODULE_AUTO_UPDATER, FAST_START, DISABLE_SERVER_START;

    public static void main(String[] args) throws IOException {
        AnsiConsole.systemInstall();
        HandledLogger logger = new HandledAsyncLogger(LogLevel.fromName(System.getProperty("cloud.logging.level", "INFO")));
        logger.setCacheEntires(true);
        Logger.setFactory(logger.addHandler(entry -> {
            String formatted = ColoredMessageFormatter.format(entry);
            System.out.println(formatted);
        }));
        FAST_START = Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("--fastStart"));
        DISABLE_SERVER_START = Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("--disableServerStart"));
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


    public boolean isFastStart() {
        return Arrays.stream(this.args).anyMatch(s -> s.equalsIgnoreCase("--fastStart"));
    }

    public Launcher(Logger logger, String[] args) throws IOException {
        this.args = args;
        this.logger = logger;
        this.dependencies = DriverUtility.newList();
        this.repositories = new HashMap<>();

        Document document = Document.gson(new File(LAUNCHER_DIR.toFile(), "auto_updater.json"));

        if (document.isEmpty()) {
            document.set("lastVersion", VersionInfo.getCurrentVersion().toString());
            document.saveToFile(new File(LAUNCHER_DIR.toFile(), "auto_updater.json"));
        }

        String lastVersion = document.getString("lastVersion");
        VersionInfo version = VersionInfo.fromString(lastVersion);
        VersionInfo.setCurrentVersion(version);


        logger.info("         ██▓    ▄▄▄       █    ██  ███▄    █  ▄████▄   ██░ ██ ▓█████  ██▀███  ");
        logger.info("        ▓██▒   ▒████▄     ██  ▓██▒ ██ ▀█   █ ▒██▀ ▀█  ▓██░ ██▒▓█   ▀ ▓██ ▒ ██▒");
        logger.info("        ▒██░   ▒██  ▀█▄  ▓██  ▒██░▓██  ▀█ ██▒▒▓█    ▄ ▒██▀▀██░▒███   ▓██ ░▄█ ▒");
        logger.info("        ▒██░   ░██▄▄▄▄██ ▓▓█  ░██░▓██▒  ▐▌██▒▒▓▓▄ ▄██▒░▓█ ░██ ▒▓█  ▄ ▒██▀▀█▄  ");
        logger.info("        ░██████▒▓█   ▓██▒▒▒█████▓ ▒██░   ▓██░▒ ▓███▀ ░░▓█▒░██▓░▒████▒░██▓ ▒██▒");
        logger.info("        ░ ▒░▓  ░▒▒   ▓▒█░░▒▓▒ ▒ ▒ ░ ▒░   ▒ ▒ ░ ░▒ ▒  ░ ▒ ░░▒░▒░░ ▒░ ░░ ▒▓ ░▒▓░");
        logger.info("        ░ ░ ▒  ░ ▒   ▒▒ ░░░▒░ ░ ░ ░ ░░   ░ ▒░  ░  ▒    ▒ ░▒░ ░ ░ ░  ░  ░▒ ░ ▒░");
        logger.info("          ░ ░    ░   ▒    ░░░ ░ ░    ░   ░ ░ ░         ░  ░░ ░   ░     ░░   ░ ");
        logger.info("            ░  ░     ░  ░   ░              ░ ░ ░       ░  ░  ░   ░  ░   ░     ");
        logger.info("                                             ░                                ");

        logger.info("        §7Launching your CloudSystem §7and checking for §pdates§8...");
        logger.info("            §8|=>    §7Your version: " + version + "   §8<=|        ");
        logger.info(" ");
        logger.info(" ");


        this.dependencyLoader = new DependencyLoader(this);
        this.moduleUpdater = new ModuleUpdater(this);

        if (!this.isFastStart()) {
            sleep(1000L);
        }
        logger.debug("Loading launcher.cloud§8...");
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
        if (!this.isFastStart()) {
            sleep(2000L);
        }
        loader.loadScript(launcherFile)
                .runScript().syncUninterruptedly().get();
        if (run.get()) {
            return;
        }
        run.set(true);
        if (DISABLE_SERVER_START) {
            System.setProperty("cloud.hytora.launcher.disableServerStart", "true");
        }
        USE_AUTO_UPDATER = System.getProperty("cloud.hytora.launcher.autoupdater").equalsIgnoreCase("true");
        USE_MODULE_AUTO_UPDATER = System.getProperty("cloud.hytora.launcher.module.autoupdater").equalsIgnoreCase("true");
        CUSTOM_VERSION = System.getProperty("cloud.hytora.launcher.customVersion");
        logger.debug("Setting up Files...");
        try {
            LAUNCHER_DIR.toFile().mkdirs();
            LAUNCHER_VERSIONS.toFile().mkdirs();
            LAUNCHER_LIBS.toFile().mkdirs();
            LAUNCHER_MODULES.toFile().mkdirs();
        } catch (Exception e) {
            //files already exists
        }

        logger.info("§7Initialization §adone§7!");
        logger.info("§7Now searching for §eupdates§8...");
        this.checkForUpdates(version, true, args);
    }


    private void checkForUpdates(VersionInfo version, boolean print, String... args) {
        if (!this.isFastStart()) {
            sleep(2000L);
        }
        if (print) {
            logger.info("§7Checking §eAutoUpdater §7for HytoraCloud-Node§8...");
        }
        if (USE_AUTO_UPDATER) {
            VersionInfo newestVersion = VersionInfo.getNewestVersion("UNKNOWN");
            if (!version.isUpToDate() || Objects.requireNonNull(LAUNCHER_VERSIONS.toFile().listFiles()).length == 0) {
                logger.info("  §8=> §7Version §8(§e" + version + "§8) §7is §coutdated §7or §cnot existing §7at all§8!");
                logger.info("  §8=> §7Updating to latest HytoraCloud §7release §8[§ever§8=§e{}§8]...", newestVersion.toString());

                if (!this.isFastStart()) {
                    sleep(1000L);
                }

                String startBatURL = getNewestVersionDownloadUrl("start.bat");
                File startBat = new File("start.bat");

                String startSHURL = getNewestVersionDownloadUrl("start.sh");
                File startSH = new File("start.sh");

                String cloudFileURL = getNewestVersionDownloadUrl(newestVersion.toString().toUpperCase() + ".jar");
                File cloudFile = new File(LAUNCHER_VERSIONS.toFile(), newestVersion.formatCloudJarName());

                if (!startBat.exists()) {
                    logger.info("  §8=> §7Downloading §8'§e{}§8'§8...", "start.bat");
                    LauncherUtils.downloadVersion(startBatURL, startBat.toPath(), "  §8=> §7Downloaded §8'§astart.bat§8").syncUninterruptedly();
                }
                if (!startSH.exists()) {
                    logger.info("  §8=> §7Downloading §8'§e{}§8'§8...", "start.sh");
                    LauncherUtils.downloadVersion(startSHURL, startSH.toPath(), "  §8=> §7Downloaded §8'§astart.sh§8'").syncUninterruptedly();
                }
                if (!cloudFile.exists()) {
                    logger.info("  §8=> §7Downloading §8'§e{}§8'§8...", newestVersion.formatCloudJarName());
                    LauncherUtils.downloadVersion(cloudFileURL, cloudFile.toPath(), "  §8=> §7Downloaded §8'§a" + newestVersion.formatCloudJarName() + "§8'").onTaskSucess(e -> {
                        logger.info("", newestVersion.formatCloudJarName());
                        try {
                            Document document = Document.gson(new File(LAUNCHER_DIR.toFile(), "auto_updater.json"));
                            document.set("lastVersion", newestVersion.toString());
                            document.saveToFile(new File(LAUNCHER_DIR.toFile(), "auto_updater.json"));
                            VersionInfo.setCurrentVersion(newestVersion);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        this.checkForUpdates(newestVersion, false, args);
                    }).onTaskFailed(e -> {
                        logger.info("  §8=> §cFailed to Download CloudFile");
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
            if (print) {
                logger.info("  §8=> §7AutoUpdater is §cdisabled§8! §7Directly starting §bCloudNode§8...");
            }
        } else if (!CUSTOM_VERSION.equalsIgnoreCase("null")) {
            if (print) {
                logger.info("  §8=> §7Custom version §8[§eval={}§8] §7has been selected§8! Skipping §eAutoUpdater§8...", VersionInfo.fromString(CUSTOM_VERSION));
            }
        } else {
            if (print) {
                logger.info("  §8=>§7Your §bCloudSystem §7is §aup to date §7with latest release! §8[§e{}§8]", VersionInfo.getCurrentVersion().toString());
            }
        }
        if (!this.isFastStart()) {
            sleep(2000L);
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
            logger.info(" ");
            logger.info(" ");

            try {
                startApplication(args, dependencyResources);
            } catch (Throwable exception) {
                throw new RuntimeException("Failed to start the application!", exception);
            }
        });

        logger.info("§7Checking for §eModule§8-§eUpdates§8!");
        if (USE_MODULE_AUTO_UPDATER) {
            moduleUpdater.updateModules().registerListener(task -> {
                Integer n = task.get();

                if (n > 0) {
                    logger.info("  §8=> §eModule§8-§eUpdater §7updated a total of §e{} §7Modules§8!", n);
                } else {
                    logger.info("  §8=> §7All Modules are §aup to date§8!");
                }
                logger.info("§7Continuing to §3cloud process§8...");
                if (!this.isFastStart()) {
                    sleep(2000L);
                }
                runnable.runAsync();
            }).syncUninterruptedly();

        } else {
            logger.info("  §8=>§7Module Updating is §cdisabled§8! §7Skipping§8...");
            if (!this.isFastStart()) {
                sleep(2000L);
            }
            runnable.runAsync();
        }

    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public String getNewestVersionDownloadUrl(String data) {
        VersionInfo newestVersion = VersionInfo.getNewestVersion(VersionInfo.getCurrentVersion().toString());

        return "https://raw.github.com/Lystx/HytoraCloud/master/hytoraCloud-updater/" + newestVersion.getVersion() + "/" + data;
    }


    private void startApplication(String[] args, Collection<URL> dependencyResources) throws Throwable {

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



        //Loading mainclass

        IdentifiableClassLoader classLoader = new IdentifiableClassLoader(dependencyResources.toArray(new URL[0]));
        Method method = classLoader.loadClass(mainClass).getMethod("main", String[].class);

        URLClassLoader urlClassLoader = new URLClassLoader(((URLClassLoader) (Thread.currentThread().getContextClassLoader())).getURLs());
        Thread.currentThread().setContextClassLoader(classLoader);

        Collection<String> arguments = DriverUtility.listOf(args);
        arguments.add("--moduleFolder=" + LAUNCHER_MODULES.toString());

        logger.info("Starting HytoraCloud-InternalNodeComplex...");
        logger.info(" => SessionId: " + UUID.randomUUID());
        logger.info(" => Snowflake: " + Snowflake.getInstance().nextId());
        logger.info("...");
        Thread thread = new Thread(() -> {
            try {

                try {
                    urlClassLoader.close();
                    method.invoke(null, (Object) arguments.toArray(new String[0]));
                } catch (IllegalAccessException | InvocationTargetException exception) {
                    exception.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Application-Thread");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setContextClassLoader(classLoader);
        thread.start();

    }
}
