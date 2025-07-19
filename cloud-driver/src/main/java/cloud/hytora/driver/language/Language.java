package cloud.hytora.driver.language;

import cloud.hytora.driver.CloudDriver;

import java.util.Collection;

public interface Language {


    /**
     * @return the english name of the language
     */
    String getName();

    /**
     *
     * @return the native name of the language
     */
    String getNativeName();

    /**
     * @return credits to the person who translated this language
     */
    String getAuthor();

    /**
     * Translates the given path and replaces {} with given arguments
     *
     * @param path the path
     * @param args the arguments
     * @return translated text
     */
    String translate(String path, Object... args);

    Collection<String> translateList(String path, Object... args);

}
