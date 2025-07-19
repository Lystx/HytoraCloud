package cloud.hytora.node.impl.handler.packet;

import cloud.hytora.common.function.ExceptionallyConsumer;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.packets.module.PacketModuleExecute;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.node.impl.module.ModuleProtocolData;

import java.util.List;
import java.util.stream.Collectors;

public class NodeModulePacketHandler implements PacketHandler<PacketModuleExecute> {

    @Override
    public void handle(PacketChannel channel, PacketModuleExecute packet) {
        PacketBuffer buffer = packet.buffer();
        PacketModuleExecute.PayLoad payLoad = buffer.readEnum(PacketModuleExecute.PayLoad.class);

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
                channel.sendResponse().setBuffer(buf -> buf.writeObjectCollection(CloudDriver.getInstance().getModuleManager().getModules())).execute(packet);
                break;
            case TRANSFER_MODULES:
                List<ModuleProtocolData> collect = CloudDriver.getInstance().getModuleManager().getModules().stream()
                        .map(controller -> new ModuleProtocolData(
                                controller.getJarFile().toFile(),
                                controller.getDataFolder().toFile()
                        )).collect(Collectors.toList());


                packet.sendResponse()
                        .setState(NetworkResponseState.OK)
                        .setBuffer(buf -> {
                            buf.writeCollection(collect, (ExceptionallyConsumer<ModuleProtocolData>) module -> {

                                buf.writeFile(module.getJarFile());
                                buf.writeFile(module.getFolder());
                            });
                        })
                        .execute();
                break;
        }
    }
}
