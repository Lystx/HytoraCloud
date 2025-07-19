package cloud.hytora.node.impl.handler.packet;

import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.ModuleManager;
import cloud.hytora.driver.module.controller.base.ModuleConfig;
import cloud.hytora.driver.networking.packets.module.PacketModuleController;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.node.NodeDriver;

public class NodeModuleControllerPacketHandler implements PacketHandler<PacketModuleController> {

    @Override
    public void handle(PacketChannel channel, PacketModuleController packet) {

        PacketBuffer buffer = packet.buffer();
        PacketModuleController.PayLoad payLoad = buffer.readEnum(PacketModuleController.PayLoad.class);
        ModuleConfig moduleConfig = buffer.readObject(ModuleConfig.class);
        ModuleManager moduleManager = NodeDriver.getInstance().getModuleManager();
        ModuleController controller = moduleManager.getModules().stream().filter(mc -> mc.getModuleConfig().getName().equalsIgnoreCase(moduleConfig.getName())).findFirst().orElse(null);
        if (controller == null) {
            return;
        }

        switch (payLoad) {
            case LOAD_MODULE:
                controller.loadModule();
                break;
            case RELOAD_MODULE:
                controller.reloadModule();
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
            case API_UPDATE:
                controller.update();
                break;
            case RELOAD_CONFIG:
                channel.sendResponse().setBuffer(buf -> buf.writeDocument(controller.reloadConfig())).execute(packet);
                break;
            case GET_JAR_FILE:
                channel.sendResponse().setBuffer(buf -> buf.writeString(controller.getJarFile().toString())).execute(packet);
                break;
            case GET_DATA_FOLDER:
                channel.sendResponse().setBuffer(buf -> buf.writeString(controller.getDataFolder().toString())).execute(packet);
                break;
        }
    }

}
