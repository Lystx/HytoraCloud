package cloud.hytora.driver.services.impl;

import cloud.hytora.common.wrapper.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.ServiceManager;
import cloud.hytora.driver.services.fallback.FallbackEntry;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.driver.services.ServiceInfo;

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

    protected List<ServiceInfo> allCachedServices = new ArrayList<>();

    public void setAllCachedServices(List<ServiceInfo> allCachedServices) {
        this.allCachedServices = new ArrayList<>(allCachedServices);
    }

    @Override
    public void registerService(ServiceInfo service) {
        ServiceInfo uniqueService = this.getServiceByNameOrNull(service.getName());
        if (uniqueService != null) {
            //already added
            return;
        }
        this.allCachedServices.add(service);
    }

    @Override
    public void unregisterService(ServiceInfo service) {
        ServiceInfo uniqueService = this.getServiceByNameOrNull(service.getName());
        if (uniqueService == null) {
            return;
        }
        this.allCachedServices.remove(uniqueService);
    }

    @Override
    public List<String> queryServiceOutput(ServiceInfo service) {
        return new ArrayList<>();
    }

    @Override
    public @NotNull Optional<ServiceInfo> getService(@NotNull String name) {
        return this.getAllCachedServices().stream().filter(it -> it.getName().equals(name)).findAny();
    }


    @Override
    public @NotNull Task<ServiceInfo> getFallbackAsService() {
        return Task.build(
                getAvailableFallbacksAsServices()
                        .stream()
                        .min(Comparator.comparing(ServiceInfo::getOnlinePlayers)).orElse(null));
    }

    @Override
    public @NotNull Task<FallbackEntry> getFallbackAsEntry() {
        return Task.build(
                getAvailableFallbacks()
                        .stream()
                        .min(Comparator.comparing(FallbackEntry::getPriority)).orElse(null));
    }

    @NotNull
    @Override
    public List<FallbackEntry> getAvailableFallbacks() {
        return CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream()
                .filter(ServiceInfo::isReady)
                .filter(it -> it.getServiceState() == ServiceState.ONLINE)
                .filter(it -> it.getServiceVisibility() == ServiceVisibility.VISIBLE)
                .filter(it -> !it.getTask().getVersion().isProxy())
                .filter(it -> it.getTask().getFallback().isEnabled())
                .map(it -> it.getTask().getFallback())
                .sorted(Comparator.comparingInt(FallbackEntry::getPriority))
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull List<ServiceInfo> getAvailableFallbacksAsServices() {
        return CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream()
                .filter(ServiceInfo::isReady)
                .filter(it -> it.getServiceState() == ServiceState.ONLINE)
                .filter(it -> it.getServiceVisibility() == ServiceVisibility.VISIBLE)
                .filter(it -> !it.getTask().getVersion().isProxy())
                .filter(it -> it.getTask().getFallback().isEnabled())
                .sorted(Comparator.comparingInt(it -> it.getTask().getFallback().getPriority()))
                .collect(Collectors.toList());
    }
}
