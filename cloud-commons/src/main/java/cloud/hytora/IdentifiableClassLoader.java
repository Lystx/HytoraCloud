package cloud.hytora;

import java.net.URL;
import java.net.URLClassLoader;

public class IdentifiableClassLoader extends URLClassLoader {


    static {
        ClassLoader.registerAsParallelCapable();
    }

    public IdentifiableClassLoader(URL[] urls) {
        super(urls, ClassLoader.getSystemClassLoader());
    }

    public IdentifiableClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
}
