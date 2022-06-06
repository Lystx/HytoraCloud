package cloud.hytora.node.impl.handler.packet.normal;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.networking.packets.module.RemoteModuleExecutionPacket;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.networking.protocol.wrapped.ChanneledPacketAction;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class NodeModulePacketHandler implements PacketHandler<RemoteModuleExecutionPacket> {

    @Override
    public void handle(PacketChannel wrapper, RemoteModuleExecutionPacket packet) {
        PacketBuffer buffer = packet.buffer();
        RemoteModuleExecutionPacket.PayLoad payLoad = buffer.readEnum(RemoteModuleExecutionPacket.PayLoad.class);

        switch (payLoad) {
            case LOAD_MODULES:
                CloudDriver.getInstance().getModuleManager().loadModules();
                break;
            case ENABLE_MODULES:
                CloudDriver.getInstance().getModuleManager().enableModules();
                break;
            case DISABLE_MODULES:
                CloudDriver.getInstance().getModuleManager().disableModules();
                break;
            case UNREGISTER_MODULES:
                CloudDriver.getInstance().getModuleManager().unregisterModules();
                break;
            case RESOLVE_MODULES:
                CloudDriver.getInstance().getModuleManager().resolveModules();
                break;
            case RETRIEVE_MODULES:
                wrapper.prepareResponse().buffer(buf -> buf.writeObjectCollection(CloudDriver.getInstance().getModuleManager().getModules())).execute(packet);
                break;
            case TRANSFER_MODULES:
                ChanneledPacketAction<Void> response = wrapper.prepareResponse();
                List<ModuleController> copyModules = CloudDriver.getInstance().getModuleManager().getModules().stream().filter(m -> m.getModuleConfig().getCopyType() != ModuleCopyType.NONE).collect(Collectors.toList());
                response.buffer(buf -> buf.writeInt(copyModules.size()));
                for (ModuleController module : copyModules) {
                    response.buffer(buf -> {
                        try {
                            buf.writeString(module.getJarFile().toFile().getName());
                            buf.writeString(module.getDataFolder().toFile().getName());

                            buf.writeFile(module.getJarFile().toFile());
                            buf.writeFile(module.getDataFolder().toFile());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
                response.execute(packet);
                break;
        }
    }
}
