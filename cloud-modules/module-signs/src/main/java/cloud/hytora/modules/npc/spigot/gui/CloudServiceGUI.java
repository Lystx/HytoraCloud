package cloud.hytora.modules.npc.spigot.gui;

import cloud.hytora.driver.services.ICloudService;
import de.lystx.bettergui.utils.Inventory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
@Getter
public class CloudServiceGUI {

    private final ICloudService cloudService;


    public void open(Player player) {

        Inventory inv = new Inventory(player, "§8» §3Hytora§bCloud &8┃ §a" + cloudService.getName(), 3);
        inv.setRand();
        inv.setItem(13, new ItemStack(Material.DIAMOND_SWORD));
        inv.setRunnable(13, () -> player.sendMessage("§6This is a short runnable"));
        inv.setRunnable(13, new Runnable() {
            @Override
            public void run() {
                player.sendMessage("§This is a normal runnable! Multiple lines can be executed");
            }
        });
        inv.setItem(15, new ItemStack(Material.DIAMOND), 2L); //Adds an item delayed (2 Ticks)

        inv.setClickable(false); // Denies to click the gui (Cancels the event)
        inv.playSound(Sound.LEVEL_UP);

        inv.open();
    }

}
