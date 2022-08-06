package cloud.hytora.context.utils;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;

public class PackageScanner {
    public Set<Class<?>> findClasses(String packageName) {

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder()
                .addUrls(ClasspathHelper.forPackage(packageName))
                .addScanners(Scanners.SubTypes.filterResultsBy(s -> true));
//                .setExpandSuperTypes(false);

        Reflections reflections = new Reflections(configurationBuilder);

        return reflections.getSubTypesOf(Object.class);
    }
}
