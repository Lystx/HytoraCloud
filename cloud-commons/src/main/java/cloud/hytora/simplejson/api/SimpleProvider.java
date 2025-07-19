package cloud.hytora.simplejson.api;

import cloud.hytora.simplejson.api.modules.ParserModule;
import cloud.hytora.simplejson.api.modules.SerializerModule;
import lombok.Getter;
import lombok.Setter;

public class SimpleProvider {

    /**
     * The static instance
     */
    private static SimpleProvider instance;

    @Getter @Setter
    private ParserModule parserModule;

    @Getter @Setter
    private SerializerModule serializerModule;

    /**
     * Returns the current instance
     * If the instance is null a new one will be created
     * and directly returned
     *
     * @return the current instance
     */
    public static SimpleProvider getInstance() {
        return instance == null ? (instance = new SimpleProvider()) : instance;
    }
}
