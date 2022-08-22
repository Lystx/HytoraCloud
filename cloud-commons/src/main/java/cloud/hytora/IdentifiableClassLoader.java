package cloud.hytora;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;

public class IdentifiableClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    private final Collection<URL> addedUrls = new ArrayList<>();

    public IdentifiableClassLoader(URL[] urls) {
        super(urls, ClassLoader.getSystemClassLoader());
    }

    public IdentifiableClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public void addURL(URL url) {
        addedUrls.add(url);
        super.addURL(url);
    }

    public boolean containsUrl(URL url) {
        return this.addedUrls.stream().anyMatch(u -> u.toString().equalsIgnoreCase(url.toString()));
    }

}
