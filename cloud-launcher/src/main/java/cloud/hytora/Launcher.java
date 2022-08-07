package cloud.hytora;


import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.DriverVersion;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
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
        new Launcher(args);
    }


    private final Collection<Dependency> dependencies;
    private final Map<String, Repository> repositories;

    private final DependencyLoader dependencyLoader;

    public Launcher(String[] args) {

        this.dependencies = new ArrayList<>();
        this.repositories = new HashMap<>();

        IApplicationContext context = new ApplicationContext(this);

        DriverVersion version = DriverVersion.getCurrentVersion();

        System.out.println("Launching HytoraCloud-Launcher");
        System.out.println("Version: " + version.getVersion());

        this.dependencyLoader = context.getInstance(DependencyLoader.class);

        System.out.println("Loading Script...");
        ScriptLoader loader = ScriptLoader.getInstance();
        loader.registerCommand("runScript", new RunScriptCommand());
        loader.registerCommand("print", new PrintCommand());
        loader.registerCommand("includeDependency", new IncludeDependencyCommand(this.dependencies::add));
        loader.registerCommand("includeRepository", new IncludeRepositoryCommand(repository -> repositories.put(repository.getName(), repository)));

        try {
            loader.executeScriptFromResource("launcher.cloud");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Setting up Files...");
        try {
            LAUNCHER_DIR.toFile().mkdirs();
            LAUNCHER_VERSIONS.toFile().mkdirs();
            LAUNCHER_LIBS.toFile().mkdirs();
        } catch (Exception e) {
            //files already exists
        }

        System.out.println("Checking for Updates...");
        if (!version.isUpToDate() || LAUNCHER_VERSIONS.toFile().listFiles().length == 0) {
            System.out.println("Version is outdated or not existing at all!");
            System.out.println("==> Downloading latest HytoraCloud version...");
            version.update();
            return;
        }
        System.out.println("Cloud is up to date with latest release!");

        Collection<URL> dependencyResources;
        try {
            dependencyResources = this.dependencyLoader.loadDependencyURLs();
        } catch (IOException exception) {
            throw new RuntimeException("Unable to install needed dependencies!", exception);
        }

        try {
            System.out.println("Starting CloudNet...");

            this.startApplication(args, dependencyResources);
        } catch (IOException | ClassNotFoundException | NoSuchMethodException exception) {
            throw new RuntimeException("Failed to start the application!", exception);
        }
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
