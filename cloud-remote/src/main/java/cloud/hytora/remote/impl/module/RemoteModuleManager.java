package cloud.hytora.remote.impl.module;

import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.ModuleManager;
import cloud.hytora.driver.networking.packets.module.PacketModuleExecute;
import cloud.hytora.remote.Remote;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RemoteModuleManager implements ModuleManager {

    @Override
    public ModuleController resolveModule(Path path) {
        return null;
    }

    @Override
    public void resolveModules() {
        Remote.getInstance().getClient().sendPacket(new PacketModuleExecute(PacketModuleExecute.PayLoad.RESOLVE_MODULES));
    }

    @Override
    public void loadModules() {
        Remote.getInstance().getClient().sendPacket(new PacketModuleExecute(PacketModuleExecute.PayLoad.LOAD_MODULES));

    }

    @Override
    public void enableModules() {
        Remote.getInstance().getClient().sendPacket(new PacketModuleExecute(PacketModuleExecute.PayLoad.ENABLE_MODULES));
    }

    @Override
    public void disableModules() {
        Remote.getInstance().getClient().sendPacket(new PacketModuleExecute(PacketModuleExecute.PayLoad.DISABLE_MODULES));
    }

    @Override
    public void unregisterModules() {
        Remote.getInstance().getClient().sendPacket(new PacketModuleExecute(PacketModuleExecute.PayLoad.UNREGISTER_MODULES));
    }

    @NotNull
    @Override
    public Path getModulesDirectory() {
        return Paths.get("modules/");
    }

    @Override
    public void setModulesDirectory(@NotNull Path directory) {
    }

    @NotNull
    @Override
    public List<ModuleController> getModules() {
        return new ArrayList<>(Remote.getInstance().getClient().getPacketChannel().sendQuery()
                .execute(new PacketModuleExecute(PacketModuleExecute.PayLoad.RETRIEVE_MODULES))
                .syncUninterruptedly().get().buffer().readObjectCollection(RemoteModuleController.class));
    }
}
