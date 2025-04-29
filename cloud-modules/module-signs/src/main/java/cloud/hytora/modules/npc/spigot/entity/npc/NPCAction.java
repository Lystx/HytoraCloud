package cloud.hytora.modules.npc.spigot.entity.npc;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.modules.npc.spigot.entity.npc.types.ClickType;
import cloud.hytora.modules.npc.spigot.entity.user.EntityPlayerConnection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Consumer;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class NPCAction {

    private final ClickType clickType;
    private final Consumer<ICloudPlayer> action;
    private int delay;


    public static NPCAction click(Consumer<ICloudPlayer> handler) {
        return new NPCAction(ClickType.DEFAULT, handler, 0);
    }

    public static NPCAction clickLeft(Consumer<ICloudPlayer> handler) {
        return new NPCAction(ClickType.LEFT, handler, 0);
    }

    public static NPCAction clickRight(Consumer<ICloudPlayer> handler) {
        return new NPCAction(ClickType.RIGHT, handler, 0);
    }

    public long getFixedDelay() {
        return 1000000000L * this.delay;
    }

    public void execute(EntityPlayerConnection user, Consumer<ICloudPlayer> action) {
        ICloudPlayer cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(user.getUniqueId());

        action.accept(cloudPlayer);
    }


}
