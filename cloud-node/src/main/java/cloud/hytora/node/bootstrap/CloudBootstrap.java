package cloud.hytora.node.bootstrap;

import cloud.hytora.IdentifiableClassLoader;
import cloud.hytora.common.function.ExceptionallyConsumer;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.node.bootstrap.library.DependencyLoader;
import cloud.hytora.node.bootstrap.script.IncludeDependencyCommand;
import cloud.hytora.node.bootstrap.script.IncludeRepositoryCommand;
import cloud.hytora.node.bootstrap.library.Dependency;
import cloud.hytora.node.bootstrap.library.Repository;
import cloud.hytora.script.api.IScript;
import cloud.hytora.script.api.IScriptLoader;
import cloud.hytora.script.api.impl.DefaultScriptLoader;
import cloud.hytora.script.defaults.DefaultModifyCommand;
import cloud.hytora.script.defaults.DefaultPrintCommand;
import cloud.hytora.script.defaults.DefaultRunCommand;
import cloud.hytora.script.defaults.DefaultVarCommand;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CloudBootstrap {

    private static URL getCurrentURL() {

        String s = CloudBootstrap.class.getName();
        int i = s.lastIndexOf(".");
        s = s.substring(i + 1);
        s = s + ".class";
        return CloudBootstrap.class.getResource(s);
    }

    public static void main(String[] args) throws URISyntaxException, MalformedURLException {

        Map<String, Repository> repositories = new HashMap<>();
        List<Dependency> includedDependencies = new ArrayList<>();

        IScriptLoader loader = new DefaultScriptLoader();

        loader.registerCommand(new DefaultPrintCommand());
        loader.registerCommand(new DefaultRunCommand());
        loader.registerCommand(new DefaultVarCommand());
        loader.registerCommand(new DefaultModifyCommand());
        loader.registerCommand(new IncludeDependencyCommand(includedDependencies::add));
        loader.registerCommand(new IncludeRepositoryCommand(r -> repositories.put(r.getName(), r)));

        Path launcherFile = Paths.get("node.hc");
        if (!Files.exists(launcherFile)) {
            try {
                FileUtils.copy(
                        ClassLoader.getSystemResourceAsStream("node.hc"),
                        Files.newOutputStream(launcherFile)
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        IScript script = loader.loadScript(Paths.get("node.hc"));
        if (script == null) {
            System.out.println("Couldn't load script!");
            return;
        }
        script.executeAsync()
                .onTaskSucess((ExceptionallyConsumer<Void>) v -> {

                    DependencyLoader dependencyLoader = new DependencyLoader(repositories, includedDependencies);

                    Collection<URL> dependencyResources;
                    try {
                        dependencyResources = dependencyLoader.loadDependencyURLs();
                        dependencyResources.add(getCurrentURL()); //adding current jar file
                    } catch (IOException exception) {
                        throw new RuntimeException("Unable to install needed dependencies!", exception);
                    }


                    IdentifiableClassLoader classLoader = new IdentifiableClassLoader(dependencyResources.toArray(new URL[0]));

                    Thread thread = new Thread(() -> {
                        try {
                            Method method = classLoader.loadClass(InternalBootstrap.class.getName()).getMethod("main", String[].class);
                            method.invoke(null, (Object) args);
                        } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException exception) {
                            exception.printStackTrace();
                        }
                    });

                    try {
                        Thread.currentThread().setContextClassLoader(classLoader);
                        Field scl = ClassLoader.class.getDeclaredField("scl"); // Get system class loader
                        scl.setAccessible(true); // Set accessible
                        scl.set(null, classLoader); // Update it to your class loader
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    thread.setPriority(Thread.MIN_PRIORITY);
                    thread.setContextClassLoader(classLoader);
                    thread.start();


                });


    }

}
