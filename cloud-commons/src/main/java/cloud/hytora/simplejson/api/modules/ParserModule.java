package cloud.hytora.simplejson.api.modules;

import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.api.enums.JsonFormat;

import java.io.File;
import java.io.Reader;

public interface ParserModule {

    String toString(JsonEntity entity, JsonFormat format);

    JsonEntity parse(String input);

    JsonEntity parse(File file);

    JsonEntity parse(Reader reader);
}
