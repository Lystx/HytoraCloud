package cloud.hytora.modules.sign.cloud.manager;

import cloud.hytora.common.task.Task;
import cloud.hytora.document.Bundle;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.document.IEntry;
import cloud.hytora.modules.sign.api.CloudSign;
import cloud.hytora.modules.sign.cloud.CloudSignsModule;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Getter
public class SignManager {

    private final Collection<CloudSign> cachedCloudSigns;
    private final File signFile;

    public SignManager() {
        this.cachedCloudSigns = new ArrayList<>();

        this.signFile = CloudSignsModule.getInstance().getController().getDataFolder().resolve("signs.json").toFile();
    }

    public Task<Collection<CloudSign>> loadSigns() {
        return Task.callAsync(() -> {
            Bundle bundle = DocumentFactory.newJsonBundle(this.signFile);
            for (IEntry entry : bundle) {
                cachedCloudSigns.add(entry.toInstance(CloudSign.class));
            }
            return cachedCloudSigns;
        });
    }

    public void addCloudSign(CloudSign cloudSign) {
        this.cachedCloudSigns.add(cloudSign);
        this.save();
    }

    public void removeCloudSign(CloudSign cloudSign) {
        this.cachedCloudSigns.remove(cloudSign);
        this.save();
    }

    public CloudSign getSign(UUID uuid) {
        return this.cachedCloudSigns.stream().filter(s -> s.getUuid().equals(uuid)).findFirst().orElse(null);
    }


    public void save() {

        Bundle bundle = DocumentFactory.newJsonBundle();
        for (CloudSign cachedCloudSign : cachedCloudSigns) {
            bundle.add(cachedCloudSign);
        }
        try {
            bundle.saveToFile(this.signFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
