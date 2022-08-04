package cloud.hytora.driver.services.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.ServiceManager;
import cloud.hytora.driver.services.fallback.FallbackEntry;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.driver.services.ICloudServer;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class DefaultServiceManager implements ServiceManager {

    /**
     * All cached {@link ICloudServer} stored in a {@link List}
     */
    protected List<ICloudServer> allCachedServices;

    public DefaultServiceManager() {
        this.allCachedServices = new CopyOnWriteArrayList<>();
        CloudDriver.getInstance().getEventManager().registerListener(this);
    }

    public void setAllCachedServices(List<ICloudServer> allCachedServices) {
        this.allCachedServices = new ArrayList<>(allCachedServices);
    }

    @Override
    public void registerService(ICloudServer service) {
        ICloudServer uniqueService = this.getServiceByNameOrNull(service.getName());
        if (uniqueService != null) {
            //already added
            return;
        }
        this.allCachedServices.add(service);
    }

    @Override
    public void unregisterService(ICloudServer service) {
        ICloudServer uniqueService = this.getServiceByNameOrNull(service.getName());
        if (uniqueService == null) {
            return;
        }
        this.allCachedServices.remove(uniqueService);
    }

    @Override
    public ICloudServer getServiceByNameOrNull(@NotNull String name) {
        return this.allCachedServices.stream().filter(s -> s.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public @NotNull Task<ICloudServer> getServiceByNameOrNullAsync(@NotNull String name) {
        return Task.callAsync(() -> {
           return this.allCachedServices.stream().filter(s -> s.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        });
    }


    public void updateServerInternally(ICloudServer service) {
        Task<ICloudServer> server = this.getServiceByNameOrNullAsync(service.getName());
        if (server.isPresent()) {

            UniversalCloudServer serviceInfo = (UniversalCloudServer) server.get();

            Process process = serviceInfo.getProcess();
            File workingDirectory = serviceInfo.getWorkingDirectory();

            ((UniversalCloudServer)service).setProcess(process);
            ((UniversalCloudServer)service).setWorkingDirectory(workingDirectory);

            int i = allCachedServices.indexOf(serviceInfo);
            allCachedServices.set(i, service);
        }

    }

    @Override
    public ICloudServer thisServiceOrNull() {
        return thisService().orElse(null);
    }

    @Override
    public @NotNull Task<ICloudServer> getFallbackAsService() {
        return Task.build(
                getAvailableFallbacksAsServices()
                        .stream()
                        .min(Comparator.comparing(ICloudServer::getOnlinePlayerCount)).orElse(null));
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
                .filter(ICloudServer::isReady)
                .filter(it -> it.getServiceState() == ServiceState.ONLINE)
                .filter(it -> it.getServiceVisibility() == ServiceVisibility.VISIBLE)
                .filter(it -> !it.getTask().getVersion().isProxy())
                .filter(it -> it.getTask().getFallback().isEnabled())
                .map(it -> it.getTask().getFallback())
                .sorted(Comparator.comparingInt(FallbackEntry::getPriority))
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull List<ICloudServer> getAvailableFallbacksAsServices() {
        return CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream()
                .filter(ICloudServer::isReady)
                .filter(it -> it.getServiceState() == ServiceState.ONLINE)
                .filter(it -> it.getServiceVisibility() == ServiceVisibility.VISIBLE)
                .filter(it -> !it.getTask().getVersion().isProxy())
                .filter(it -> it.getTask().getFallback().isEnabled())
                .sorted(Comparator.comparingInt(it -> it.getTask().getFallback().getPriority()))
                .collect(Collectors.toList());
    }
}
