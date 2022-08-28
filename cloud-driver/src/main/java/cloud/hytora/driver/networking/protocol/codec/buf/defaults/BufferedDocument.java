package cloud.hytora.driver.networking.protocol.codec.buf.defaults;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BufferedDocument implements AbstractBuffered<BufferedDocument, Document> {

    private Document wrapped;

    @Override
    public void setWrapped(BufferedDocument wrapped) {
        this.wrapped = wrapped.getWrapped();
    }

    @Override
    public Class<BufferedDocument> getWrapperClass() {
        return BufferedDocument.class;
    }

    @Override
    public Document read(PacketBuffer buffer) {
        return buffer.readDocument();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeDocument(wrapped);
    }
}
