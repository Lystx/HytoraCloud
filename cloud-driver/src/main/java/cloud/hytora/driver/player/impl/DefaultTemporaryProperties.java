package cloud.hytora.driver.player.impl;

import cloud.hytora.driver.player.TemporaryProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DefaultTemporaryProperties implements TemporaryProperties {

    private final Map<String, Property<?>> properties;

    public DefaultTemporaryProperties() {
        this.properties = new HashMap<>();
    }

    @Override
    public void addProperty(String name, Object value, long time, TimeUnit unit) {
        properties.put(name, new Property<Object>(name, value, (System.currentTimeMillis() + unit.toMillis(time))));
    }

    @Override
    public <T> T getProperty(String name, Class<T> typeClass) {
        return properties.get(name) == null ? null : (T) properties.get(name).getValue();
    }

    @Override
    public boolean hasProperty(String name) {
        return this.properties.get(name) != null;
    }

    @Override
    public void removeProperty(String name) {
        this.properties.remove(name);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return this.properties.keySet();
    }

    @Override
    public boolean hasPropertyExpired(String name) {
        Property<?> property = this.properties.get(name);
        if (property == null) {
            return true;
        }


        long currentTimeMillis = System.currentTimeMillis();


        return property.getTime() != -1 && currentTimeMillis > property.getTime();
    }


    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Property<T> {

        private String name;
        private T value;
        private long time;

    }
}
