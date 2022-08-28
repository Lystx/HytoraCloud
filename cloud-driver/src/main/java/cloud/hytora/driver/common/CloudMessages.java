package cloud.hytora.driver.common;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.storage.INetworkDocumentStorage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CloudMessages {

    private final String prefix;
    private final String taskHasPermissionMessage;
    private final String noAvailableFallbackMessage;
    private final String networkCurrentlyInMaintenance;

    private final String alreadyOnFallbackMessage;

    private final String maintenanceKickByPassedMessage;

    public CloudMessages() {
        this.prefix = "§8× §bHytora§fCloud §8»";
        this.taskHasPermissionMessage = "§cThe task requires the permission {} to join it!";
        this.noAvailableFallbackMessage = "§cCould not find any available fallback.";
        this.alreadyOnFallbackMessage = "§cYou are already on a fallback server!";
        this.networkCurrentlyInMaintenance = "§cThe network is currently in maintenance!";
        this.maintenanceKickByPassedMessage = "§cThe maintenance for the network was enabled but you didn't get kicked because you are permitted to stay!";
    }

    public static CloudMessages retrieveFromStorage() {
        return CloudDriver.getInstance().getProviderRegistry().getUnchecked(INetworkDocumentStorage.class).get("cloud::messages").toInstance(CloudMessages.class);
    }
}
