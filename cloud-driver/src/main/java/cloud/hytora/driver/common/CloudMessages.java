package cloud.hytora.driver.common;

import jdk.management.resource.internal.ApproverGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class CloudMessages {

    private final String taskHasPermissionMessage;
    private final String noAvailableFallbackMessage;
    private final String networkCurrentlyInMaintenance;

    private final String maintenanceKickByPassedMessage;

    public CloudMessages() {
        this.taskHasPermissionMessage = "The task requires the permission {} to join it!";
        this.noAvailableFallbackMessage = "Could not find any available fallback.";
        this.networkCurrentlyInMaintenance = "§cThe network is currently in maintenance!";
        this.maintenanceKickByPassedMessage = "§cThe maintenance for the network was enabled but you didn't get kicked because you are permitted to stay!";
    }
}
