package cloud.hytora.driver.language.def;

import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.document.Document;
import cloud.hytora.document.IEntry;
import cloud.hytora.driver.language.Language;
import cloud.hytora.driver.language.LanguageManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class DriverLanguageManager implements LanguageManager {

    private Language currentLanguage;
    private final Collection<Language> loadedLanguages;

    public DriverLanguageManager(String defaultLanguage) {
        this.loadedLanguages = new ArrayList<>();

        Language language = this.loadLanguage(defaultLanguage);
        this.setCurrentLanguage(language);
    }

    @Override
    public Language loadLanguage(String name) {

        try {
            String languageFileContent = FileUtils.getResourceFileAsString("/languages/" + name + ".json", getClass());
            Document document = Document.gson(languageFileContent);

            String languageName = document.getString("name");
            String languageNameNative = document.getString("nativeName");
            String author = document.getString("author");
            Map<String, String> translations = new HashMap<>();
            Map<String, Collection<String>> translationLists = new HashMap<>();

            Document translationData = document.getDocument("translations");
            for (String section : translationData.keys()) {
                Document sectionData = translationData.getDocument(section);
                for (String key : sectionData.keys()) {
                    IEntry entry = sectionData.get(key);
                    String path = section + "." + key;
                    if (entry.isBundle()) {
                        translationLists.put(path, entry.toBundle().toStrings());
                        continue;
                    }
                    translations.put(path, entry.toString());
                }
            }

            return new DriverLanguage(
                    languageName,
                    languageNameNative,
                    author,
                    translations,
                    translationLists
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Language getLanguage(String name) {
        return loadedLanguages.stream().filter(l -> l.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

}
