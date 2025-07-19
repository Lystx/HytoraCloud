package cloud.hytora.driver.config.def;

import cloud.hytora.common.logging.ConsoleColor;
import cloud.hytora.driver.common.component.style.ComponentColor;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
@AllArgsConstructor
@Setter
public class UniversalCloudMessages implements IBufferObject {

    private ComponentColor mainColor;
    private ComponentColor secondColor;
    private String prefix;
    private String taskHasPermissionMessage;
    private String noAvailableFallbackMessage;
    private String networkCurrentlyInMaintenance;
    private String alreadyOnFallbackMessage;
    private String maintenanceKickByPassedMessage;
    private String alreadyOnNetworkMessage;
    private String higherPriorityJoined;
    private String noLowerPriorityThanSelf;
    private String higherMinecraftVersionNeeded;
    private String allProxiesFull;
    private String noCloudPlayerFoundLogin;
    private String networkShutdown;

    public UniversalCloudMessages() {
        this.mainColor = ComponentColor.CYAN;
        this.secondColor = ComponentColor.LIGHT_BLUE;
        this.prefix = "§8•§7▮ %1Hytora%2Cloud §8▎ §7";
        this.taskHasPermissionMessage = "§cThe task requires the permission {} to join it!";
        this.noAvailableFallbackMessage = "§cCould not find any available fallback.";
        this.alreadyOnFallbackMessage = "§cYou are already on a fallback server!";
        this.networkCurrentlyInMaintenance = "§cThe network is currently in maintenance!";
        this.networkShutdown = "§cThe network has been shut down";
        this.maintenanceKickByPassedMessage = "§cThe maintenance for the network was enabled but you didn't get kicked because you are permitted to stay!";
        this.alreadyOnNetworkMessage = "§cYou are already connected to the network!";
        this.higherMinecraftVersionNeeded = "§cFor this Server the Version §e%version% §cor higher is required";
        this.allProxiesFull = "§cAll proxies are currently full and you are not permitted to kick a player for yourself to join the network!";
        this.noCloudPlayerFoundLogin = "§cThe login-query returned no CloudPlayer! Please contact a system administrator to fix your error";
        this.higherPriorityJoined = "§cA Player with a higher rank than you has joined. So you got kicked to make up space!";
        this.noLowerPriorityThanSelf = "§cOn this Server is no one with a lower rank than you. You cannot join this server!";
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:
                this.mainColor = buf.readEnum(ComponentColor.class);
                this.secondColor = buf.readEnum(ComponentColor.class);
                this.prefix = buf.readString();
                this.taskHasPermissionMessage = buf.readString();
                this.noAvailableFallbackMessage = buf.readString();
                this.alreadyOnFallbackMessage = buf.readString();
                this.networkCurrentlyInMaintenance = buf.readString();
                this.maintenanceKickByPassedMessage = buf.readString();
                this.higherMinecraftVersionNeeded = buf.readString();
                this.alreadyOnNetworkMessage = buf.readString();
                this.allProxiesFull = buf.readString();
                this.noCloudPlayerFoundLogin = buf.readString();
                this.higherPriorityJoined = buf.readString();
                this.noLowerPriorityThanSelf = buf.readString();
                break;

            case WRITE:
                buf.writeEnum(mainColor);
                buf.writeEnum(secondColor);
                buf.writeString(prefix);
                buf.writeString(taskHasPermissionMessage);
                buf.writeString(noAvailableFallbackMessage);
                buf.writeString(alreadyOnFallbackMessage);
                buf.writeString(networkCurrentlyInMaintenance);
                buf.writeString(maintenanceKickByPassedMessage);
                buf.writeString(higherMinecraftVersionNeeded);
                buf.writeString(alreadyOnNetworkMessage);
                buf.writeString(allProxiesFull);
                buf.writeString(noCloudPlayerFoundLogin);
                buf.writeString(higherPriorityJoined);
                buf.writeString(noLowerPriorityThanSelf);
                break;
        }
    }
}
