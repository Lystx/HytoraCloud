
package cloud.hytora.simplejson.exceptions;


import lombok.Getter;

@Getter
public class JsonDeserializeException extends RuntimeException {

    public JsonDeserializeException(String message) {
        super(message);
    }
}
