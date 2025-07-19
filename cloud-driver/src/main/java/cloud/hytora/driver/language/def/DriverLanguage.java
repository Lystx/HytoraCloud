package cloud.hytora.driver.language.def;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.driver.language.Language;
import cloud.hytora.driver.language.TranslationNotSetException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class DriverLanguage implements Language {

    private final String name;
    private final String nativeName;
    private final String author;
    private final Map<String, String> translations;
    private final Map<String, Collection<String>> translationLists;


    @Override
    public Collection<String> translateList(String path, Object... args) {
        if (translationLists.keySet().stream().noneMatch(s -> s.equalsIgnoreCase(path))) {
            throw new TranslationNotSetException("No Translation set for path " + path);
        }
        Collection<String> strings = translationLists.get(path);
        return strings.stream()
                .map(s -> DriverUtility.args(s, args))
                .collect(Collectors.toList());
    }

    @Override
    public String translate(String path, Object... args) {
        if (translations.keySet().stream().noneMatch(s -> s.equalsIgnoreCase(path))) {
            throw new TranslationNotSetException("No Translation set for path " + path);
        }
        String rawTranslation = translations.get(path);
        String finalTranslation = DriverUtility.args(rawTranslation, args);
        return finalTranslation.replace("&", "§");
    }
}
