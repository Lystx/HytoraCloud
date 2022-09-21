package cloud.hytora;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A wrapper class loader to ensure dependency loading is still available in JDK > 8
 *
 * @author Lystx
 * @since SNAPSHOT-1.4
 */
public class IdentifiableClassLoader extends URLClassLoader {

    /*
     * Automatically registers this as a parallel classLoader
     */
    static {
        ClassLoader.registerAsParallelCapable();
    }

    /**
     * All added Urls to this classLoader (manually)
     */
    private final Collection<URL> addedUrls = new ArrayList<>();

    /**
     * Constructs this class loader with pre-defined urls this loader should load into runtime
     *
     * @param urls the url array
     */
    public IdentifiableClassLoader(URL[] urls) {
        super(urls, ClassLoader.getSystemClassLoader());
    }

    /**
     * Override method to add an url to this classLoader
     * and automatically puts this into the cache of loaded urls
     *
     * @param url the url to add
     */
    @Override
    public void addURL(URL url) {
        addedUrls.add(url);
        super.addURL(url);
    }

    /**
     * Checks if the provided url has already been added manually to this loader
     *
     * @param url the url to check
     * @return if has been loaded
     */
    public boolean containsUrl(URL url) {
        return this.addedUrls.stream().anyMatch(u -> u.toString().equalsIgnoreCase(url.toString()));
    }

}
