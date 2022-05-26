package cloud.hytora.document;

public interface DocumentWrapper<T> extends Document {

    T getWrapper();

}
