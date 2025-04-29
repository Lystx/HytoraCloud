package cloud.hytora.modules.npc.spigot.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.http.HttpClient;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.modules.npc.api.NPCFactory;
import cloud.hytora.modules.npc.api.NPCManager;
import cloud.hytora.modules.npc.spigot.task.NPCManagerTask;
import cloud.hytora.modules.npc.spigot.task.NpcRefreshSkinTask;
import cloud.hytora.modules.npc.spigot.entity.skin.AshconSkinFetcherServer;
import cloud.hytora.modules.npc.spigot.entity.skin.MineSkinFetcher;
import cloud.hytora.modules.npc.spigot.entity.skin.SkinFetcher;
import cloud.hytora.modules.npc.spigot.entity.skin.DefaultSkinFetcher;
import cloud.hytora.modules.npc.spigot.entity.user.EntityPlayerConnection;
import cloud.hytora.modules.npc.spigot.listeners.PlayerListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

@Getter
public class SpigotNPCManager implements NPCManager {

    private final Map<String, NPCFactory> factories;
    private SkinFetcher skinFetcher;


    public static final int VIEW_DISTANCE = 32;
    public static final int MAX_PATH_LOCATION = 500;


    public SpigotNPCManager() {
        this.factories = new HashMap<>();
    }

    @Override
    public NPCFactory getNPCFactory(ICloudService cloudService) {
        if (this.factories.keySet().stream().anyMatch(s -> cloudService.getName().equalsIgnoreCase(s))) {
            return this.factories.get(cloudService.getName());
        }
        NPCFactory factory = new SpigotNPCFactory(cloudService);
        this.factories.put(cloudService.getName(), factory);
        return factory;
    }

    @Override
    public void update(NPCFactory factory) {
        this.factories.put(factory.getService().getName(), factory);
    }

    @Override
    public void load(Object instance) {
        if (instance instanceof Plugin) {
            Plugin plugin = (Plugin) instance;

            this.skinFetcher = new DefaultSkinFetcher(
                    new HttpClient(),

                    new AshconSkinFetcherServer(),
                    new MineSkinFetcher()
            );

            Bukkit.getOnlinePlayers().forEach(EntityPlayerConnection::find);

            CloudDriver.getInstance().getScheduler().scheduleRepeatingTaskAsync(new NpcRefreshSkinTask(skinFetcher), 60L, 1L);

            plugin.getServer().getPluginManager().registerEvents(new PlayerListener(), plugin);

        }
    }

    @Override
    public void unload() {
    }
}
