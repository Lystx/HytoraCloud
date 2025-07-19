package cloud.hytora.simplejson.impl.parser.easy;

import cloud.hytora.simplejson.impl.JsonHelper;
import cloud.hytora.simplejson.api.SimpleProvider;
import cloud.hytora.simplejson.elements.JsonArray;
import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.elements.JsonLiteral;
import cloud.hytora.simplejson.elements.object.JsonEntry;
import cloud.hytora.simplejson.elements.object.JsonObject;
import cloud.hytora.simplejson.impl.parser.json.NormalJsonWriter;
import cloud.hytora.simplejson.impl.SimpleJson;

import java.io.IOException;
import java.io.Writer;

public class SimpleJsonWriter {


    /**
     * Saves a {@link JsonEntity} to a {@link Writer} with a given level
     *
     * @param value the entity to save
     * @param tw    the writer
     * @param level the current level
     * @throws IOException if something goes wrong
     */
    public void saveRecursive(JsonEntity value, Writer tw, int level, String separator, boolean noIndent) throws IOException {
        if (value == null) {
            tw.write(separator);
            tw.write("null");
            return;
        }

        boolean writeArraysSingleLined;

        if (SimpleProvider.getInstance().getSerializerModule() == null) {
            writeArraysSingleLined = false;
        } else {
            writeArraysSingleLined = SimpleProvider.getInstance().getSerializerModule().isWriteArraysSingleLined();
        }

        if (SimpleJson.CHANGED_WRITE_SINGLE_LINE) {
            SimpleJson jsonInstance = (SimpleJson) SimpleProvider.getInstance().getSerializerModule();
            jsonInstance.setWriteArraysSingleLined(SimpleJson.OLD_SINGLE_LINE_VALUE);
            SimpleJson.CHANGED_WRITE_SINGLE_LINE = false;

            SimpleProvider.getInstance().setSerializerModule(jsonInstance);

        }

        switch (value.jsonType()) {

            case OBJECT:
                JsonObject jsonObject = value.asJsonObject();
                if (!noIndent) {
                    if (jsonObject.size() > 0) {
                        this.newLine(tw, level);
                    } else {
                        tw.write(separator);
                    }
                }
                tw.write('{');

                int nullCount = 0;
                for (JsonEntry jsonEntry : jsonObject) {
                    JsonEntity v = jsonEntry.getValue();
                    if (v == null) {
                        v = JsonLiteral.NULL;
                    }
                    if (v == JsonLiteral.NULL && SimpleProvider.getInstance().getSerializerModule() != null && !SimpleProvider.getInstance().getSerializerModule().isSerializeNulls()) {
                        nullCount++;
                        continue;
                    }

                    newLine(tw, level + 1);

                    String escape;
                    String name = jsonEntry.getName();
                    if (name.length() == 0 || JsonHelper.NEED_ESCAPE_NAME.matcher(name).find()) {
                        escape = "\"" + NormalJsonWriter.escapeString(name) + "\"";
                    } else {
                        escape = name;
                    }

                    tw.write(escape);
                    tw.write(":");

                    saveRecursive(jsonEntry.getValue(), tw, level + 1, " ", false);
                }

                if (jsonObject.size() > 0 && nullCount != jsonObject.size()) {
                    newLine(tw, level);
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

                if (allow && writeArraysSingleLined) {
                    noIndent = true;
                }

                if (!noIndent) {
                    if (size > 0) {
                        newLine(tw, level);
                    } else {
                        tw.write(separator);
                    }
                }
                if (jsonArray.isEmpty()) {
                    tw.write("[]");
                } else {
                    if (writeArraysSingleLined && allow) {
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

                        //Saving sub-objects of the array
                        for (int i = 0; i < size; i++) {
                            newLine(tw, level + 1);
                            saveRecursive(jsonArray.get(i), tw, level + 1, "", true);
                        }

                        if (size > 0) {
                            newLine(tw, level);
                        }
                        tw.write(']');
                    }
                }
                break;

            case BOOLEAN:
                tw.write(separator);
                tw.write(value.isTrue() ? "true" : "false");
                break;

            case STRING:

                String string = value.asString();

                if (string.length() == 0) {
                    tw.write(separator + "\"\"");
                    return;
                }

                char left = string.charAt(0), right = string.charAt(string.length() - 1);
                char left1 = string.length() > 1 ? string.charAt(1) : '\0', left2 = string.length() > 2 ? string.charAt(2) : '\0';
                boolean doEscape = false;
                char[] chars = string.toCharArray();
                for (char ch : chars) {

                    boolean need = false;

                    switch (ch) {
                        case '\t':
                        case '\f':
                        case '\b':
                        case '\n':
                        case '\r':
                            need = true;
                            break;
                        default:
                            break;
                    }

                    if (need) {
                        doEscape = true;
                        break;
                    }
                }

                if (doEscape || SimpleJsonParser.isWhiteSpace(left) || SimpleJsonParser.isWhiteSpace(right) || left == '"' || left == '\'' || left == '#' || left == '/' && (left1 == '*' || left1 == '/') || JsonHelper.isPunctuatedChar(left) || SimpleJsonParser.tryParseNumber(string, true) != null || JsonHelper.startsWith(string)) {
                    boolean noEscape = true;
                    for (char ch : chars) {

                        boolean needsEscape;


                        switch (ch) {
                            case '\"':
                            case '\\':
                                needsEscape = true;
                            default:

                                boolean need = false;

                                switch (ch) {
                                    case '\t':
                                    case '\f':
                                    case '\b':
                                    case '\n':
                                    case '\r':
                                        need = true;
                                        break;
                                    default:
                                        break;
                                }

                                needsEscape = need;
                        }

                        if (needsEscape) {
                            noEscape = false;
                            break;
                        }
                    }

                    if (noEscape) {
                        tw.write(separator + "\"" + string + "\"");
                        return;
                    }

                    boolean noEscapeML = true, allWhite = true;
                    for (char ch : chars) {

                        boolean needsEscapeML = false;

                        switch (ch) {
                            case '\n':
                            case '\r':
                            case '\t':
                            default:
                                boolean need = false;

                                switch (ch) {
                                    case '\t':
                                    case '\f':
                                    case '\b':
                                    case '\n':
                                    case '\r':
                                        need = true;
                                        break;
                                    default:
                                        break;
                                }

                                needsEscapeML = need;
                        }

                        if (needsEscapeML) {
                            noEscapeML = false;
                            break;
                        } else if (!SimpleJsonParser.isWhiteSpace(ch)) {
                            allWhite = false;
                        }
                    }
                    if (noEscapeML && !allWhite && !string.contains("'''")) {
                        String[] lines = string.replace("\r", "").split("\n", -1);

                        if (lines.length == 1) {
                            tw.write(separator + "'''");
                            tw.write(lines[0]);
                            tw.write("'''");
                        } else {
                            level++;
                            newLine(tw, level);
                            tw.write("'''");

                            for (String line : lines) {
                                newLine(tw, line.length() > 0 ? level : 0);
                                tw.write(line);
                            }
                            newLine(tw, level);
                            tw.write("'''");
                        }
                    } else {
                        tw.write(separator + "\"" + NormalJsonWriter.escapeString(string) + "\"");
                    }
                } else tw.write(separator + string);
                break;

            default:
                tw.write(separator);
                tw.write(value.toString());
                break;
        }
    }

    /**
     * Creates a new line in this writing process
     *
     * @param tw    the writer
     * @param level the current level
     * @throws IOException if something goes wrong
     */
    public void newLine(Writer tw, int level) throws IOException {
        tw.write(System.getProperty("line.separator"));
        for (int i = 0; i < level; i++) {
            tw.write("  ");
        }
    }

}
