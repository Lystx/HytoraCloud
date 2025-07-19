package cloud.hytora.driver.language;

import java.util.Collection;

public interface LanguageManager {

    Collection<Language> getLoadedLanguages();

    Language getLanguage(String name);

    Language loadLanguage(String name);

    void setCurrentLanguage(Language language);

    Language getCurrentLanguage();

}
