package cloud.hytora.driver.common.objects;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.types.BufferState;

/**
 * Objects that implement this interface indicate
 * that they can or need to be stored to a {@link Document} at some point
 *
 * Here you have to write and read every field to and from the {@link Document}
 *
 * @author Lystx
 * @since STABLE-1.0
 * @version STABLE-2.0
 */
public interface CloudJsonEntity {

    static Document toDocument(CloudJsonEntity entity) {
        Document document = Document.gson();
        entity.handleJsonOperation(BufferState.WRITE, document);
        return document;
    }

    /**
     * This method handles the json write/read
     * You need to read and write every important field
     * to or from this document
     *
     * @param state the state (READ/WRITE)
     * @param document the document that you can read or write from
     */
    void handleJsonOperation(BufferState state, Document document);

}
