package cloud.hytora.node.impl.handler.packet.normal;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.IModuleManager;
import cloud.hytora.driver.module.controller.base.ModuleConfig;
import cloud.hytora.driver.module.packet.RemoteModuleControllerPacket;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.node.NodeDriver;

public class NodeModuleControllerPacketHandler implements PacketHandler<RemoteModuleControllerPacket> {

    @Override
    public void handle(PacketChannel wrapper, RemoteModuleControllerPacket packet) {

        PacketBuffer buffer = packet.buffer();
        RemoteModuleControllerPacket.PayLoad payLoad = buffer.readEnum(RemoteModuleControllerPacket.PayLoad.class);
        ModuleConfig moduleConfig = buffer.readObject(ModuleConfig.class);
        IModuleManager moduleManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IModuleManager.class);
        ModuleController controller = moduleManager.getModules().stream().filter(mc -> mc.getModuleConfig().getName().equalsIgnoreCase(moduleConfig.getName())).findFirst().orElse(null);
        if (controller == null) {
            return;
        }

        switch (payLoad) {
            case LOAD_MODULE:
                controller.loadModule();
                break;
            case ENABLE_MODULE:
                controller.enableModule();
                break;
            case DISABLE_MODULE:
                controller.disableModule();
                break;
            case UNREGISTER_MODULE:
                controller.unregisterModule();
                break;
            case RELOAD_CONFIG:
                wrapper.prepareResponse().buffer(buf -> buf.writeDocument(controller.reloadConfig())).execute(packet);
                break;
            case GET_JAR_FILE:
                wrapper.prepareResponse().buffer(buf -> buf.writeString(controller.getJarFile().toString())).execute(packet);
                break;
            case GET_DATA_FOLDER:
                wrapper.prepareResponse().buffer(buf -> buf.writeString(controller.getDataFolder().toString())).execute(packet);
                break;
        }
    }

}
