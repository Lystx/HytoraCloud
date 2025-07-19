package cloud.hytora.driver.entity.services.template.def;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.deployment.ServiceDeployment;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import cloud.hytora.driver.entity.services.template.TemplateManager;
import cloud.hytora.driver.entity.services.template.TemplateStorage;
import cloud.hytora.driver.networking.packets.other.PacketTemplate;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Getter
public class DefaultTemplateManager implements TemplateManager {

    /**
     * All cached storages
     */
    private final Collection<TemplateStorage> storages = new ArrayList <>();

    public DefaultTemplateManager() {
    }

    @Override
    public void registerStorage(TemplateStorage storage) {
        if (this.getStorage(storage.getName()) == null) {
            this.storages.add(storage);
        }
    }

    @Override
    public void deployService(@NotNull CloudService server, @NotNull ServiceDeployment... deployments) {
        for (ServiceDeployment deployment : deployments) {
            ServiceTemplate template = deployment.getTemplate();
            TemplateStorage storage = template.getStorage();
            if (storage != null) {
                storage.deployService(server, deployment);
            }
        }
    }

    @Override
    public Task<Collection<File>> getDirectoryContentsAsync(ServiceTemplate template, String storageName) {
        Task<Collection<File>> task = Task.empty();


        if (CloudDriver.getInstance().getEnvironment() == CloudDriver.Environment.NODE) {
            File file = template.buildTemplateDirectory();
            File target = new File(file, storageName);
            File[] files = target.listFiles();
            if (files != null) {
                task.setResult(Arrays.asList(files));
            } else {
                task.setResult(new ArrayList<>());
            }
        } else if (CloudDriver.getInstance().getEnvironment() == CloudDriver.Environment.SERVICE) {
            PacketTemplate packetTemplate = PacketTemplate.forFiles(template, storageName);
            packetTemplate.sendQuery()
                    .execute().onTaskSucess(bufferedResponse -> {
                        NetworkResponseState state = bufferedResponse.state();
                        PacketBuffer buffer = bufferedResponse.buffer();
                        if (state == NetworkResponseState.OK) {
                            try {
                                Collection<File> files = buffer.readFileCollection(new File("tempDir"));
                                task.setResult(files);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }

        return task;
    }

    @Override
    public TemplateStorage getStorage(String name) {
        return this.storages.stream().filter(ts -> ts.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public void handle(PacketChannel channel, PacketTemplate packet) {
        PacketBuffer buffer = packet.buffer();
        switch (buffer.readEnum(PacketTemplate.PayLoad.class)) {
            case GET_FILES:
                CloudTemplate cloudTemplate = buffer.readObject(CloudTemplate.class);
                String dirName = buffer.readString();
                getDirectoryContentsAsync(cloudTemplate, dirName).onTaskSucess(collection -> {

                    packet.sendResponse().setState(NetworkResponseState.OK).setBuffer(buf -> {
                        try {
                            buf.writeFileCollection(collection);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).execute();
                });
                break;
        }
    }
}
