package cloud.hytora.driver.services.impl;

import cloud.hytora.common.wrapper.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.ServiceManager;
import cloud.hytora.driver.services.fallback.FallbackEntry;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.driver.services.CloudServer;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class DefaultServiceManager implements ServiceManager {

    protected List<CloudServer> allCachedServices = new ArrayList<>();

    public void setAllCachedServices(List<CloudServer> allCachedServices) {
        this.allCachedServices = new ArrayList<>(allCachedServices);
    }

    @Override
    public void registerService(CloudServer service) {
        CloudServer uniqueService = this.getServiceByNameOrNull(service.getName());
        if (uniqueService != null) {
            //already added
            return;
        }
        this.allCachedServices.add(service);
    }

    @Override
    public void unregisterService(CloudServer service) {
        CloudServer uniqueService = this.getServiceByNameOrNull(service.getName());
        if (uniqueService == null) {
            return;
        }
        this.allCachedServices.remove(uniqueService);
    }

    @Override
    public List<String> queryServiceOutput(CloudServer service) {
        return new ArrayList<>();
    }

    @Override
    public @NotNull Optional<CloudServer> getService(@NotNull String name) {
        return this.getAllCachedServices().stream().filter(it -> it.getName().equals(name)).findAny();
    }


    @Override
    public @NotNull Task<CloudServer> getFallbackOrNullAsService() {
        return Task.build(
                getAvailableFallbacksAsServices()
                        .stream()
                        .min(Comparator.comparing(CloudServer::getOnlinePlayers)).orElse(null));
    }

    @Override
    public @NotNull Task<FallbackEntry> getFallbackOrNull() {
        return Task.build(
                getAvailableFallbacks()
                        .stream()
                        .min(Comparator.comparing(FallbackEntry::getPriority)).orElse(null));
    }

    @NotNull
    @Override
    public List<FallbackEntry> getAvailableFallbacks() {
        return CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream()
                .filter(it -> it.getServiceState() == ServiceState.ONLINE)
                .filter(it -> it.getServiceVisibility() == ServiceVisibility.VISIBLE)
                .filter(it -> !it.getConfiguration().getVersion().isProxy())
                .filter(it -> it.getConfiguration().getFallback().isEnabled())
                .map(it -> it.getConfiguration().getFallback())
                .sorted(Comparator.comparingInt(FallbackEntry::getPriority))
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull List<CloudServer> getAvailableFallbacksAsServices() {
        return CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream()
                .filter(it -> it.getServiceState() == ServiceState.ONLINE)
                .filter(it -> it.getServiceVisibility() == ServiceVisibility.VISIBLE)
                .filter(it -> !it.getConfiguration().getVersion().isProxy())
                .filter(it -> it.getConfiguration().getFallback().isEnabled())
                .sorted(Comparator.comparingInt(it -> it.getConfiguration().getFallback().getPriority()))
                .collect(Collectors.toList());
    }
}
