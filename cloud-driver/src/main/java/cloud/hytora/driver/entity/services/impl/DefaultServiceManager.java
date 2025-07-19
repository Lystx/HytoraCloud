package cloud.hytora.driver.entity.services.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.ServiceManager;
import cloud.hytora.driver.entity.services.fallback.FallbackEntry;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.entity.services.utils.ServiceState;
import cloud.hytora.driver.entity.services.utils.ServiceVisibility;

import cloud.hytora.driver.entity.services.utils.SpecificDriverEnvironment;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class DefaultServiceManager implements ServiceManager {

    /**
     * All cached {@link CloudService} stored in a {@link List}
     */
    protected List<CloudService> allCachedServices;

    public DefaultServiceManager() {
        this.allCachedServices = new CopyOnWriteArrayList<>();
        CloudDriver.getInstance().getEventManager().registerListener(this);
    }


    public @NotNull List<CloudService> getAllServicesByTask(@NotNull ServiceTask serviceTask) {
        if (serviceTask == null) {
            return new ArrayList<>();
        }
        return this.getAllCachedServices()
                .stream()
                .filter(Objects::nonNull)
                .filter(it -> it.getTask() != null)
                .filter(it -> it.getTask().getName().equalsIgnoreCase(serviceTask.getName()))
                .collect(Collectors.toList());
    }

    public List<CloudService> getAllServicesByState(@NotNull ServiceState serviceState) {
        return this.getAllCachedServices().stream().filter(it -> it.getServiceState() == serviceState).collect(Collectors.toList());
    }

    public List<CloudService> getAllServicesByEnvironment(@NotNull SpecificDriverEnvironment environment) {
        return this.getAllCachedServices().stream().filter(it -> it.getTask() != null && it.getTask().getTaskGroup() != null && it.getTask().getTaskGroup().getEnvironment() == environment).collect(Collectors.toList());
    }


    public void setAllCachedServices(List<CloudService> allCachedServices) {
        this.allCachedServices = new ArrayList<>(allCachedServices);
    }

    @Override
    public void registerService(CloudService service) {
        CloudService uniqueService = this.getCachedCloudService(service.getName());
        if (uniqueService != null) {
            //already added
            return;
        }
        this.allCachedServices.add(service);
    }

    @Override
    public void unregisterService(CloudService service) {
        CloudService uniqueService = this.getCachedCloudService(service.getName());
        if (uniqueService == null) {
            return;
        }
        this.allCachedServices.remove(uniqueService);
    }

    @Override
    public CloudService getCachedCloudService(@NotNull String name) {
        return this.allCachedServices.stream().filter(s -> s.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }



    public void updateServerInternally(CloudService service) {
        CloudService server = this.getCachedCloudService(service.getName());
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
    public @NotNull Task<CloudService> getFallbackAsService() {
        return Task.build(
                getAvailableFallbacksAsServices()
                        .stream()
                        .min(Comparator.comparing(CloudService::getOnlinePlayerCount)).orElse(null));
    }

    @Override
    public @NotNull List<CloudService> getAvailableFallbacksAsServices() {
        return CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream()
                .filter(CloudService::isReady)
                .filter(it -> it.getServiceState() == ServiceState.ONLINE)
                .filter(it -> it.getServiceVisibility() == ServiceVisibility.VISIBLE)
                .filter(it -> !it.getTask().getVersion().isProxy())
                .filter(it -> it.getTask().getFallback().isEnabled())
                .sorted(Comparator.comparingInt(it -> it.getTask().getFallback().getPriority()))
                .collect(Collectors.toList());
    }
}
