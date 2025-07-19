package cloud.hytora.driver.common.property;

import cloud.hytora.document.Document;
import cloud.hytora.document.IEntry;
import cloud.hytora.document.gson.adapter.ExcludeJsonField;
import cloud.hytora.simplejson.api.annotation.JsonExcludeField;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public abstract class AbstractPropertyHolder implements IPropertyHolder {

    /**
     * The properties
     */
    @ExcludeJsonField
    @JsonExcludeField
    protected Document properties = Document.gson();

    public IEntry getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public boolean hasProperty(String name) {
        return properties.has(name);
    }

    @Override
    public void setProperty(@NotNull String name, Object value) {
        this.properties.set(name, value);
    }
}
