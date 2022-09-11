package cloud.hytora.database.api;

import cloud.hytora.document.Bundle;
import cloud.hytora.document.Document;

public interface IPayLoad {

    boolean isSuccess();

    int getStatusCode();

    String getResponse();

    Document toDocument();

    Bundle toBundle();

    default <T> T as(Class<T> typeClass) {
        return toDocument().toInstance(typeClass);
    }
}
