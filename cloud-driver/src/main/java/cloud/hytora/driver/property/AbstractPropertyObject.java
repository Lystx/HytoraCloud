package cloud.hytora.driver.property;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public abstract class AbstractPropertyObject implements IPropertyObject {

    /**
     * The properties
     */
    protected Document properties = DocumentFactory.newJsonDocument();

    @Override
    public <T> T getProperty(@NotNull String name, @NotNull Class<T> typeClass) {
        return properties.getInstance(name, typeClass);
    }

    @Override
    public void setProperty(@NotNull String name, Object value) {
        this.properties.set(name, value);
    }
}
