
package cloud.hytora.simplejson.exceptions;


import lombok.Getter;

@Getter
public class JsonParseException extends RuntimeException {

    /**
     * The offset of the exception
     */
    private final int offset;

    /**
     * The line
     */
    private final int line;

    /**
     * The column of the line
     */
    private final int column;

    public JsonParseException(String message, int offset, int line, int column) {
        super(message + " at " + line + ":" + column);
        this.offset = offset;
        this.line = line;
        this.column = column;
    }

}
