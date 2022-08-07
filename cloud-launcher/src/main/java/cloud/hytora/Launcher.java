package cloud.hytora;


import cloud.hytora.common.DriverVersion;
import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.formatter.ColoredMessageFormatter;
import cloud.hytora.common.logging.handler.HandledAsyncLogger;
import cloud.hytora.common.logging.handler.HandledLogger;
import cloud.hytora.common.logging.handler.LogEntry;
import cloud.hytora.common.logging.handler.LogHandler;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.common.misc.ZipUtils;
import cloud.hytora.common.progressbar.ProgressBar;
import cloud.hytora.common.progressbar.ProgressBarStyle;
import cloud.hytora.common.task.Task;
import cloud.hytora.context.ApplicationContext;
import cloud.hytora.context.IApplicationContext;
import cloud.hytora.dependency.Dependency;
import cloud.hytora.dependency.DependencyLoader;
import cloud.hytora.dependency.Repository;
import cloud.hytora.script.ScriptLoader;
import cloud.hytora.script.commands.IncludeDependencyCommand;
import cloud.hytora.script.commands.IncludeRepositoryCommand;
import cloud.hytora.script.commands.PrintCommand;
import cloud.hytora.script.commands.RunScriptCommand;
import lombok.Getter;
import org.fusesource.jansi.AnsiColors;
import org.fusesource.jansi.AnsiConsole;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.jar.JarFile;

/**
 * Launcher highly inspired by
 * => <a href="https://github.com/CloudNetService/CloudNet-v3/blob/development/cloudnet-launcher/src/main/java/de/dytanic/cloudnet/launcher/CloudNetLauncher.java">...</a>
 */
@Getter
public class Launcher {

    public static final Path LAUNCHER_DIR = Paths.get("launcher/");
    public static final Path LAUNCHER_LIBS = LAUNCHER_DIR.resolve("libs/");
    public static final Path LAUNCHER_VERSIONS = LAUNCHER_DIR.resolve("versions/");

    public static void main(String[] args) {
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

    public Launcher(Logger logger, String[] args) {
        this.args = args;
        this.logger = logger;
        this.dependencies = new ArrayList<>();
        this.repositories = new HashMap<>();

        DriverVersion version = DriverVersion.getCurrentVersion();

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

        logger.info("Loading 'launcher.cloud'...");
        ScriptLoader loader = ScriptLoader.getInstance();
        loader.registerCommand("runScript", new RunScriptCommand());
        loader.registerCommand("print", new PrintCommand());
        loader.registerCommand("includeDependency", new IncludeDependencyCommand(this.dependencies::add));
        loader.registerCommand("includeRepository", new IncludeRepositoryCommand(repository -> repositories.put(repository.getName(), repository)));

        try {
            loader.executeScriptFromResource("launcher.cloud");

            logger.info("Setting up Files...");
            try {
                LAUNCHER_DIR.toFile().mkdirs();
                LAUNCHER_VERSIONS.toFile().mkdirs();
                LAUNCHER_LIBS.toFile().mkdirs();
            } catch (Exception e) {
                //files already exists
            }

            logger.info("Initialization done!");
            logger.info("==> Now searching for updates...");
            this.checkForUpdates(version, args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private void checkForUpdates(DriverVersion version, String... args) {

        logger.info("Checking for Updates...");
        if (!version.isUpToDate() || LAUNCHER_VERSIONS.toFile().listFiles().length == 0) {
            logger.info("Version (" + version + ") is outdated or your cloud.jar is not existing at all!");
            logger.info("==> Downloading latest HytoraCloud version...");

            Path zippedFile = LAUNCHER_DIR.resolve("hytoraCloud.zip");
            this.downloadNewestVersion(zippedFile).onTaskSucess(v -> {
                logger.info("Downloaded latest RELEASE!");
                logger.info("Unzipping...");

                ZipUtils.unzipDirectory(zippedFile, "unzipped");
                try {
                    Files.delete(zippedFile);
                    Path cloudInFile = Paths.get("unzipped/cloud.jar");
                    Files.copy(cloudInFile, LAUNCHER_VERSIONS.resolve("cloud.jar"));
                    FileUtils.delete(Paths.get("unzipped"));
                    logger.info("Unzipped and moved HytoraCloud-Jar to its folder!");

                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    this.checkForUpdates(version, args);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
            return;
        }

        logger.info("Cloud is up to date with latest release!");

        Collection<URL> dependencyResources;
        try {
            dependencyResources = this.dependencyLoader.loadDependencyURLs();
        } catch (IOException exception) {
            throw new RuntimeException("Unable to install needed dependencies!", exception);
        }

        logger.info("Running CloudApplication with dependencies:");
        for (Dependency dependency : this.dependencies) {
            logger.info("  => " + dependency.toPath());
        }

        logger.info("" );

        try {
            logger.info("Bootstrapping...");
            Thread.sleep(400);
            this.startApplication(args, dependencyResources);
        } catch (IOException | ClassNotFoundException | NoSuchMethodException exception) {
            throw new RuntimeException("Failed to start the application!", exception);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Task<Void> downloadNewestVersion(Path location) {
        Task<Void> task = Task.empty();
        DriverVersion newestVersion = DriverVersion.getNewestVersion();
        try {
            ProgressBar pb = new ProgressBar(ProgressBarStyle.UNICODE_BLOCK, 300);

            pb.setPrintAutomatically(true);
            pb.setExpandingAnimation(false);

            URL url = new URL(("https://github.com/Lystx/HytoraCloud/releases/download/v" + newestVersion.getVersion() + "/RELEASE.zip"));
            String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
            URLConnection con = url.openConnection();
            con.setRequestProperty("User-Agent", USER_AGENT);

            int contentLength = con.getContentLength();
            InputStream inputStream = con.getInputStream();

            OutputStream outputStream = Files.newOutputStream(location);
            byte[] buffer = new byte[2048];
            int length;
            int downloaded = 0;

            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
                downloaded+=length;
                pb.stepTo((long) ((downloaded * 100L) / (contentLength * 1.0)));
            }
            pb.setExtraMessage("Cleaning up...");
            outputStream.close();
            inputStream.close();
            pb.close();
            task.setResult(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return task;
    }

    private void startApplication(String[] args, Collection<URL> dependencyResources) throws IOException, ClassNotFoundException, NoSuchMethodException {
        Path targetPath = LAUNCHER_VERSIONS.resolve("cloud.jar");
        Path driverTargetPath = LAUNCHER_VERSIONS.resolve("api.jar");

        String mainClass;
        try (JarFile jarFile = new JarFile(targetPath.toFile())) {
            mainClass = jarFile.getManifest().getMainAttributes().getValue("Main-Class");
        }

        if (mainClass == null) {
            throw new RuntimeException("Cannot find Main-Class from " + targetPath.toAbsolutePath());
        }

        dependencyResources.add(targetPath.toUri().toURL());
        dependencyResources.add(driverTargetPath.toUri().toURL());

        ClassLoader classLoader = new URLClassLoader(dependencyResources.toArray(new URL[0]));
        Method method = classLoader.loadClass(mainClass).getMethod("main", String[].class);

        Thread thread = new Thread(() -> {
            try {
                method.invoke(null, (Object) args);
            } catch (IllegalAccessException | InvocationTargetException exception) {
                exception.printStackTrace();
            }
        }, "Application-Thread");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setContextClassLoader(classLoader);
        thread.start();
    }
}
