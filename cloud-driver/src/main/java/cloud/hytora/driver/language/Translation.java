package cloud.hytora.driver.language;

import cloud.hytora.driver.CloudDriver;

import java.util.Collection;
import java.util.Collections;

public interface Translation {


    static TranslatedEntry entryOf(String path, Object... args) {
        return new TranslatedEntry(of(path, args));
    }

    static String of(String path, Object... args) {
        CloudDriver driver = CloudDriver.getInstance();
        if (driver == null) {
            return "driver.not.initialized";
        }
        Language currentLanguage = driver.getLanguageManager().getCurrentLanguage();
        if (currentLanguage == null) {
            return "language.not.set";
        }
        return currentLanguage.translate(path, args);
    }

    static Collection<String> listOf(String path, Object... args) {
        CloudDriver driver = CloudDriver.getInstance();
        if (driver == null) {
            return Collections.singleton( "driver.not.initialized");
        }
        Language currentLanguage = driver.getLanguageManager().getCurrentLanguage();
        if (currentLanguage == null) {
            return Collections.singleton("language.not.set");
        }
        return currentLanguage.translateList(path, args);
    }

}
