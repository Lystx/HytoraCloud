package cloud.hytora.driver.services.impl;

import cloud.hytora.common.task.IPromise;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.driver.services.fallback.ICloudFallback;
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
public abstract class DefaultServiceManager implements ICloudServiceManager {

    /**
     * All cached {@link ICloudServer} stored in a {@link List}
     */
    protected List<ICloudServer> allCachedServices;

    public DefaultServiceManager() {
        this.allCachedServices = new CopyOnWriteArrayList<>();
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).registerListener(this);
    }

    public void setAllCachedServices(List<ICloudServer> allCachedServices) {
        this.allCachedServices = new ArrayList<>(allCachedServices);
    }

    @Override
    public void registerService(ICloudServer service) {
        ICloudServer uniqueService = this.getService(service.getName());
        if (uniqueService != null) {
            //already added
            return;
        }
        this.allCachedServices.add(service);
    }

    @Override
    public void unregisterService(ICloudServer service) {
        ICloudServer uniqueService = this.getService(service.getName());
        if (uniqueService == null) {
            return;
        }
        this.allCachedServices.remove(uniqueService);
    }

    @Override
    public ICloudServer getService(@NotNull String name) {
        return this.allCachedServices.stream().filter(s -> s.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public @NotNull IPromise<ICloudServer> getServiceAsync(@NotNull String name) {
        return IPromise.callAsync(() -> {
           return this.allCachedServices.stream().filter(s -> s.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        });
    }


    public void updateServerInternally(ICloudServer service) {
        ICloudServer server = this.getService(service.getName());
        if (server != null) {

            UniversalCloudServer serviceInfo = (UniversalCloudServer) server;

            Process process = serviceInfo.getProcess();
            File workingDirectory = serviceInfo.getWorkingDirectory();

            ((UniversalCloudServer)service).setProcess(process);
            ((UniversalCloudServer)service).setWorkingDirectory(workingDirectory);

            int i = allCachedServices.indexOf(serviceInfo);
            allCachedServices.set(i, service);
        }

    }

    @Override
    public @NotNull IPromise<ICloudServer> getFallbackAsService() {
        return IPromise.newInstance(
                getAvailableFallbacksAsServices()
                        .stream()
                        .min(Comparator.comparing(s -> s.getOnlinePlayers().size())).orElse(null));
    }

    @Override
    public @NotNull IPromise<ICloudFallback> getFallbackAsEntry() {
        return IPromise.newInstance(
                getAvailableFallbacks()
                        .stream()
                        .min(Comparator.comparing(ICloudFallback::getPriority)).orElse(null));
    }

    @NotNull
    @Override
    public List<ICloudFallback> getAvailableFallbacks() {
        return CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getAllCachedServices().stream()
                .filter(ICloudServer::isReady)
                .filter(it -> it.getServiceState() == ServiceState.ONLINE)
                .filter(it -> it.getServiceVisibility() == ServiceVisibility.VISIBLE)
                .filter(it -> !it.getTask().getVersion().isProxy())
                .filter(it -> it.getTask().getFallback().isEnabled())
                .map(it -> it.getTask().getFallback())
                .sorted(Comparator.comparingInt(ICloudFallback::getPriority))
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull List<ICloudServer> getAvailableFallbacksAsServices() {
        return CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getAllCachedServices().stream()
                .filter(ICloudServer::isReady)
                .filter(it -> it.getServiceState() == ServiceState.ONLINE)
                .filter(it -> it.getServiceVisibility() == ServiceVisibility.VISIBLE)
                .filter(it -> !it.getTask().getVersion().isProxy())
                .filter(it -> it.getTask().getFallback().isEnabled())
                .sorted(Comparator.comparingInt(it -> it.getTask().getFallback().getPriority()))
                .collect(Collectors.toList());
    }
}
