package cloud.hytora.node.bootstrap.library;

import cloud.hytora.IdentifiableClassLoader;
import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.context.annotations.ApplicationParticipant;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class DependencyLoader {


    private final Map<String, Repository> repositories;
    private final List<Dependency> includedDependencies;


    public Collection<URL> loadDependencyURLs() throws IOException {
        Collection<URL> dependencyResources = new ArrayList<>();

        for (Dependency dependency : includedDependencies) {
            if (repositories.containsKey(dependency.getRepository())) {
                Path path = Paths.get("libs/").resolve(dependency.toPath());

                this.installLibrary(this.repositories.get(dependency.getRepository()).getUrl(), dependency, path);

                dependencyResources.add(path.toUri().toURL());
                System.out.println(StringUtils.formatMessage("Loaded Dependency[group={}, artifact={}, version={}, repo={}]", dependency.getGroup(), dependency.getName(), dependency.getVersion(), dependency.getRepository()));

                ClassLoader ccl = Thread.currentThread().getContextClassLoader();
                if (ccl instanceof IdentifiableClassLoader) {
                    IdentifiableClassLoader custom = (IdentifiableClassLoader)ccl;
                    custom.addURL(path.toUri().toURL());
                } else {
                    System.out.println("Wrong classLoader");
                }
            } else {
                throw new IllegalArgumentException("Dependency " + dependency + " does not match any registered Repository!");
            }
        }

        return dependencyResources;
    }

    private void installLibrary(String repositoryURL, Dependency dependency, Path path) throws IOException {
        if (!Files.exists(path)) {

            Files.createDirectories(path.getParent());

            String dependencyName = dependency.getGroup() + ":" + dependency.getName() + ":"
                    + dependency.getVersion() + ""
                    + ".jar";

            System.out.println(StringUtils.formatMessage("Installing dependency {} from repository {}...", dependencyName, dependency.getRepository()));

            try (InputStream inputStream = DriverUtility.readInputStreamFromURL(repositoryURL + "/" + dependency.toPath().toString().replace(File.separatorChar, '/'))) {
                Files.copy(inputStream, path);
            }

        }
    }
}