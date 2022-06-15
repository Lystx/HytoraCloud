
package cloud.hytora.driver.provider.defaults;

import cloud.hytora.driver.provider.ProviderEntry;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultProviderEntry<T> implements ProviderEntry<T> {

    private final Class<T> service;
    private final T provider;
    private final boolean immutable;
    private final boolean needsReplacement;

    @Override
    public boolean needsReplacement() {
        return this.needsReplacement;
    }
}
