
package cloud.hytora.driver.provider.defaults;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.provider.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultProviderRegistry implements ProviderRegistry {

    private final Map<Class<?>, ProviderEntry<?>> entries;
    private final boolean registerInstancesAsEventListener;
    private final IEventManager eventManager;

    public DefaultProviderRegistry(boolean registerInstancesAsEventListener, IEventManager eventManager) {
        this.registerInstancesAsEventListener = registerInstancesAsEventListener;
        this.eventManager = eventManager;
        this.entries = new ConcurrentHashMap<>();
    }

    @Override
    public <T> Task<T> setProvider(Class<T> service, T provider, boolean immutable, boolean needsReplacement) throws ProviderImmutableException {
        Task<T> task = Task.empty();
        ProviderEntry<?> current = this.entries.get(service);
        if (current != null && current.isImmutable()) {
            throw new ProviderImmutableException(service);
        }

        if (registerInstancesAsEventListener) {
            eventManager.registerListener(provider);
        }
        this.entries.put(service, new DefaultProviderEntry<>(service, provider, immutable, needsReplacement));
        task.setResult(provider);
        return task;
    }

    
    @Override
    @SuppressWarnings("unchecked")
    public <T> Task<T> get(Class<T> service) {
        ProviderEntry<T> entry = (ProviderEntry<T>) this.entries.get(service);
        return entry == null ? Task.empty() : Task.newInstance(entry.getProvider());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getUnchecked(Class<T> service) throws ProviderNotRegisteredException {
        ProviderEntry<T> entry = (ProviderEntry<T>) this.entries.get(service);
        if (entry == null) {
            throw new ProviderNotRegisteredException(service);
        }

        return entry.getProvider();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Task<ProviderEntry<T>> getEntry(Class<T> service) {
        ProviderEntry<T> entry = (ProviderEntry<T>) this.entries.get(service);
        return Task.newInstance(entry);
    }

    @Override
    public <T> ProviderEntry<T> getEntryUnchecked(Class<T> service) throws ProviderNotRegisteredException {
        Task<ProviderEntry<T>> registeredEntry = this.getEntry(service);
        if (!registeredEntry.isPresent()) {
            throw new ProviderNotRegisteredException(service);
        }

        return registeredEntry.get();
    }

    @Override
    public Collection<ProviderEntry<?>> getEntries() {
        return Collections.unmodifiableCollection(this.entries.values());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void unregister(Class<T> service, T replacement) throws ProviderNotRegisteredException, ProviderImmutableException, ProviderNeedsReplacementException {
        ProviderEntry<T> entry = (ProviderEntry<T>) this.entries.get(service);
        if (entry == null) {
            throw new ProviderNotRegisteredException(service);
        }

        if (entry.isImmutable()) {
            throw new ProviderImmutableException(service);
        }

        if (entry.needsReplacement() && replacement == null) {
            throw new ProviderNeedsReplacementException(service);
        }

        eventManager.unregisterListener(service);
        this.entries.remove(service);
    }

    @Override
    public boolean isRegistered(Class<?> service) {
        return this.get(service).isPresent();
    }

}
