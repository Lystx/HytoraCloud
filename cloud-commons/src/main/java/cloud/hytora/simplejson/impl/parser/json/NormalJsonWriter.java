
package cloud.hytora.simplejson.impl.parser.json;

import cloud.hytora.simplejson.api.SimpleProvider;
import cloud.hytora.simplejson.elements.JsonArray;
import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.elements.JsonLiteral;
import cloud.hytora.simplejson.elements.object.JsonEntry;
import cloud.hytora.simplejson.elements.object.JsonObject;
import cloud.hytora.simplejson.api.enums.JsonType;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.Writer;

@AllArgsConstructor
public class NormalJsonWriter {

    /**
     * If the wrote json should be formatted
     */
    public boolean format;

    /**
     * Writes a new line into this writer if formatted is enabled
     * Otherwise no new line will be added
     * @param tw the writer
     * @param level the level
     * @throws IOException if something goes wrong
     */
    public void newLine(Writer tw, int level) throws IOException {
        if (format) {
            tw.write(System.getProperty("line.separator"));
            for (int i = 0; i < level; i++) {
                tw.write("  ");
            }
        }
    }

    /**
     * Saves a {@link JsonEntity} to a {@link Writer} with a given level
     *
     * @param value the entity to save
     * @param tw the writer
     * @param level the current level
     * @throws IOException if something goes wrong
     */
    public void saveRecursive(JsonEntity value, Writer tw, int level) throws IOException {
        boolean following = false;

        boolean writeArraysSingleLined;

        if (SimpleProvider.getInstance().getSerializerModule() == null) {
            writeArraysSingleLined = false;
        } else {
            writeArraysSingleLined = SimpleProvider.getInstance().getSerializerModule().isWriteArraysSingleLined();
        }
        if (value == null) {
            tw.write("null");
            return;
        }

        switch (value.jsonType()) {
            case OBJECT:

                JsonObject obj = value.asJsonObject();
                if (obj.size() > 0) {
                    this.newLine(tw, level);
                }
                tw.write('{');

                for (JsonEntry jsonEntry : obj) {
                    JsonEntity v = jsonEntry.getValue();
                    if (v == null) {
                        v = JsonLiteral.NULL;
                    }
                    if (v == JsonLiteral.NULL && SimpleProvider.getInstance().getSerializerModule() != null && !SimpleProvider.getInstance().getSerializerModule().isSerializeNulls()) {
                        continue;
                    }

                    if (following) {
                        tw.write(",");
                    }
                    this.newLine(tw, level+1);
                    tw.write('\"');
                    tw.write(escapeString(jsonEntry.getName()));
                    tw.write("\":");
                    JsonType vType = v.jsonType();
                    if (format && vType != JsonType.ARRAY && vType != JsonType.OBJECT) {
                        tw.write(" ");
                    }
                    this.saveRecursive(v, tw, level+1);
                    following = true;
                }

                if (following) {
                    this.newLine(tw, level);
                }

                tw.write('}');
                break;
            case ARRAY:
                JsonArray jsonArray = value.asJsonArray();
                int size = jsonArray.size();
                boolean allow = true;
                for (JsonEntity entity : jsonArray) {
                    if (entity != null) {
                        if (!entity.isPrimitive()) {
                            allow = false;
                            break;
                        }
                    }
                }

                if (size > 0 && !jsonArray.isEmpty() && !(allow && writeArraysSingleLined)) {
                    this.newLine(tw, level);
                }

                if (jsonArray.isEmpty()) {
                    tw.write("[]");
                } else {
                    if (allow && writeArraysSingleLined) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(" ");
                        stringBuilder.append("[");

                        for (int i = 0; i < size; i++) {
                            JsonEntity entity = jsonArray.get(i);
                            stringBuilder.append(entity.toString());
                            if ((i + 1) != size) {
                                stringBuilder.append(", ");
                            }
                        }
                        stringBuilder.append("]");
                        tw.write(stringBuilder.toString());
                    } else {
                        tw.write('[');
                        for (int i = 0; i < size; i++) {
                            if (following) {
                                tw.write(",");
                            }
                            JsonEntity v = jsonArray.get(i);
                            JsonType vType = v.jsonType();
                            if (vType != JsonType.ARRAY && vType != JsonType.OBJECT) {
                                this.newLine(tw, level + 1);
                            }
                            this.saveRecursive(v, tw, level + 1);
                            following = true;
                        }
                        if (following) {
                            this.newLine(tw, level);
                        }
                        tw.write(']');
                    }
                }
                break;
            case STRING:
                tw.write('"');
                tw.write(escapeString(value.asString()));
                tw.write('"');
                break;
            case BOOLEAN:
                tw.write(value.isTrue() ? "true" : "false");
                break;
            default:
                tw.write(value.toString());
                break;
        }
    }

    public static String escapeString(String src) {
        if (src == null) {
            return null;
        }

        for (int i = 0; i < src.length(); i++) {
            if (getEscapedChar(src.charAt(i)) != null) {
                StringBuilder sb = new StringBuilder();
                if (i > 0) {
                    sb.append(src, 0, i);
                }
                return doEscapeString(sb, src, i);
            }
        }
        return src;
    }

    private static String doEscapeString(StringBuilder sb, String src, int cur) {
        int start = cur;
        for (int i = cur; i < src.length(); i++) {
            String escaped = getEscapedChar(src.charAt(i));
            if (escaped != null) {
                sb.append(src, start, i);
                sb.append(escaped);
                start = i + 1;
            }
        }
        sb.append(src, start, src.length());
        return sb.toString();
    }

    private static String getEscapedChar(char c) {
        switch (c) {
            case '\"': return "\\\"";
            case '\t': return "\\t";
            case '\n': return "\\n";
            case '\r': return "\\r";
            case '\f': return "\\f";
            case '\b': return "\\b";
            case '\\': return "\\\\";
            default: return null;
        }
    }
}
