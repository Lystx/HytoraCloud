
package cloud.hytora.simplejson.impl.parser.json;

import cloud.hytora.simplejson.impl.JsonHelper;
import cloud.hytora.simplejson.elements.*;
import cloud.hytora.simplejson.elements.object.JsonObject;
import cloud.hytora.simplejson.exceptions.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;


public class NormalJsonParser {

    /**
     * The reader object
     */
    private final Reader reader;

    /**
     * The input as char[]
     */
    private final char[] buffer;

    /**
     * The offset of the buffer
     */
    private int bufferOffset;

    /**
     * The current index of the buffer
     */
    private int index;

    /**
     * The filling count
     */
    private int fill;

    /**
     * The current line
     */
    private int line;

    /**
     * The current line offset
     */
    private int lineOffset;

    /**
     * The current count
     */
    private int current;

    /**
     * The capturing string builder
     */
    private StringBuilder captureBuffer;

    /**
     * Where the capturing starts
     */
    private int captureStart;

    /**
     * Constructs this parser with a {@link String} input
     *
     * @param string the input to parse
     */
    public NormalJsonParser(String string) {
        this(new StringReader(string), Math.max(JsonHelper.MIN_BUFFER_SIZE, Math.min(JsonHelper.DEFAULT_BUFFER_SIZE, string.length())));
    }

    /**
     * Constructs this parser with a {@link Reader} instance
     *
     * @param reader the reader
     */
    public NormalJsonParser(Reader reader) {
        this(reader, JsonHelper.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs this parser with a {@link Reader} instance
     * and already a provided size of the buffer
     *
     * @param reader the reader
     * @param size the buffer size
     */
    public NormalJsonParser(Reader reader, int size) {
        this.reader = reader;
        this.buffer = new char[size];
        this.line = 1;
        this.captureStart = -1;
    }

    /**
     * Parses the given input into {@link JsonEntity}
     *
     * @return entity
     * @throws IOException if something goes wrong
     */
    public JsonEntity parse() throws IOException {

        //Reading and skipping spaces
        this.read();
        this.unusedSpaces();

        //Setting value
        JsonEntity parsedEntity = readValue();

        //Skipping spaces again
        this.unusedSpaces();

        //Throwing error if end of file
        if (!(current == -1)) {
            throw throwError("Unexpected character");
        }
        return parsedEntity;
    }

    /**
     * Reads a {@link JsonEntity} from the current char
     *
     * @return entity or throws error
     */
    private JsonEntity readValue() throws IOException {
        switch (current) {

            case 'n':
                //Is null instance
                read();
                checkChar('u');
                checkChar('l');
                checkChar('l');
                return JsonLiteral.NULL;

            case 't':
                //Is boolean instance (true)
                read();
                checkChar('r');
                checkChar('u');
                checkChar('e');
                return JsonLiteral.TRUE;

            case 'f':
                //Is boolean instance (false)
                read();
                checkChar('a');
                checkChar('l');
                checkChar('s');
                checkChar('e');
                return JsonLiteral.FALSE;

            case '"':
                //Is empty String instance
                return new JsonString(readString());

            case '[':
                //Is array instance

                //Reading and skipping spaces
                this.read();
                this.unusedSpaces();

                //Creating new array
                JsonArray array = new JsonArray();

                //Ending array if "]" reached
                if (this.readIf(']')) {
                    return array;
                } do {

                //Adding new value by reading it
                this.unusedSpaces();
                array.add(this.readValue());
                this.unusedSpaces();

            } while (this.readIf(','));

                //Didn't end with a closing char throwing error
                if (!this.readIf(']')) {
                    throw this.throwExpected("',' or ']'");
                }
                return array;

            case '{':
                //Is object instance
                //Reading and skipping spaces
                this.read();
                this.unusedSpaces();

                //Creating new object
                JsonObject object = new JsonObject();


                //Ending array if "}" reached
                if (this.readIf('}')) {
                    return object;
                } do {
                this.unusedSpaces();

                String name;

                if (current != '"') {
                    throw this.throwExpected("name");
                }

                name = readString();
                this.unusedSpaces();

                //Wrong formatting throwing error
                if (!this.readIf(':')) {
                    throw this.throwExpected("':'");
                }
                //Read name and now reading value
                this.unusedSpaces();
                object.addProperty(name, this.readValue());
                this.unusedSpaces();

            } while (readIf(',')); //Reading while a new value is following


                //Didn't end with a closing char throwing error
                if (!this.readIf('}')) {
                    throw this.throwExpected("',' or '}'");
                }
                return object;

            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                //Is number instance
                if (captureBuffer == null) {
                    captureBuffer = new StringBuilder();
                }
                captureStart = index-1;
                readIf('-');
                int firstDigit = current;
                if (!readDigit()) {
                    throw throwExpected("digit");
                }
                if (firstDigit != '0') {
                    while (readDigit());
                }
                if (readIf('.')) {
                    if (!readDigit()) {
                        throw throwExpected("digit");
                    }
                    while (readDigit());
                }
                if (!(!readIf('e') && !readIf('E'))) {
                    if (!readIf('+')) {
                        readIf('-');
                    }
                    if (!readDigit()) {
                        throw throwExpected("digit");
                    }
                    while (readDigit());
                }
                return new JsonNumber(Double.parseDouble(endCapture()));
            default:
                //Nothing found throwing error
                throw throwExpected("value");
        }
    }

    /**
     * Checks if the next char is the provided one
     * Otherwise an exception will be thrown
     *
     * @param ch the char
     * @throws IOException if something goes wrong
     */
    private void checkChar(char ch) throws IOException {
        if (!readIf(ch)) {
            throw throwExpected("'" + ch + "'");
        }
    }

    /**
     * Reads the current {@link String} from the buffer
     *
     * @return read string
     * @throws IOException if something goes wrong
     */
    private String readString() throws IOException {
        read();
        if (captureBuffer == null) {
            captureBuffer = new StringBuilder();
        }
        captureStart = index-1;
        while (current != '"') {
            if (current == '\\') {
                int end = index - 1;
                captureBuffer.append(buffer, captureStart, end - captureStart);
                captureStart = -1;
                read();
                switch(current) {
                    case '"':
                    case '/':
                    case '\\':
                        captureBuffer.append((char)current);
                        break;
                    case 'b':
                        captureBuffer.append('\b');
                        break;
                    case 'f':
                        captureBuffer.append('\f');
                        break;
                    case 'n':
                        captureBuffer.append('\n');
                        break;
                    case 'r':
                        captureBuffer.append('\r');
                        break;
                    case 't':
                        captureBuffer.append('\t');
                        break;
                    case 'u':
                        char[] hexChars = new char[4];
                        for (int i = 0; i < 4; i++) {
                            read();
                            if (!(current >= '0' && current <= '9' || current >= 'a' && current <= 'f' || current >= 'A' && current <= 'F')) {
                                throw throwExpected("hexadecimal digit");
                            }
                            hexChars[i] = (char) current;
                        }
                        captureBuffer.append( (char) Integer.parseInt(new String(hexChars), 16));
                        break;
                    default:
                        throw throwExpected("valid escape sequence");
                }
                read();
                if (captureBuffer == null) {
                    captureBuffer = new StringBuilder();
                }
                captureStart = index-1;
            } else if (current < 0x20) {
                throw throwExpected("valid string character");
            } else {
                read();
            }
        }

        String string = endCapture();
        read();
        return string;
    }


    /**
     * Reads if the current char is the provided one
     *
     * @param ch the char to check
     * @return if success
     * @throws IOException if something goes wrong
     */
    private boolean readIf(char ch) throws IOException {
        if (current != ch) {
            return false;
        }
        read();
        return true;
    }

    /**
     * Reads a digit from this parser
     * @throws IOException if something goes wrong
     */
    private boolean readDigit() throws IOException {
        if (!(current >= '0' && current <= '9')) {
            return false;
        }
        read();
        return true;
    }

    /**
     * Reads while there is a space or empty section
     * @throws IOException if something goes wrong
     */
    private void unusedSpaces() throws IOException {
        while ((current == ' ' || current == '\t' || current == '\n' || current == '\r')) {
            read();
        }
    }

    /**
     * Reads the current buffer
     * And sets all attributes to the reader index
     *
     * @throws IOException if something goes wrong
     */
    private void read() throws IOException {
        if (index == fill) {
            if (captureStart !=- 1) {
                captureBuffer.append(buffer, captureStart, fill - captureStart);
                captureStart=0;
            }
            bufferOffset += fill;
            fill = reader.read(buffer, 0, buffer.length);
            index = 0;
            if (fill == -1) {
                current=-1;
                return;
            }
        }
        if (current == '\n') {
            line++;
            lineOffset=bufferOffset + index;
        }
        current = buffer[index++];
    }

    /**
     * Ends the capture for this current reader
     */
    private String endCapture() {
        int end = current == -1 ? index : index -1;
        String captured;
        if (captureBuffer.length() > 0) {
            captureBuffer.append(buffer, captureStart, end - captureStart);
            captured=captureBuffer.toString();
            captureBuffer.setLength(0);
        } else {
            captured = new String(buffer, captureStart, end - captureStart);
        }
        captureStart = -1;
        return captured;
    }

    /**
     * Throws a {@link JsonParseException} that something was expected
     *
     * @param expected the message
     * @return exception
     */
    private JsonParseException throwExpected(String expected) {
        if ((current == -1)) {
            return throwError("Unexpected end of input");
        }
        return throwError("Expected " + expected);
    }

    /**
     * Throws a {@link JsonParseException} with all details about the current
     * line, colum and offset
     *
     * @param message the message to provide
     * @return exception
     */
    private JsonParseException throwError(String message) {
        int absIndex = bufferOffset + index;
        int column = absIndex - lineOffset;
        int offset = current == -1 ? absIndex : absIndex-1;
        return new JsonParseException(message, offset, line, column-1);
    }

}
