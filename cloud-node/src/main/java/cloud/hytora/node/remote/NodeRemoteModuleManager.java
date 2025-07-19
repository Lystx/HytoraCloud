package cloud.hytora.node.remote;

import cloud.hytora.common.function.ExceptionallySupplier;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.networking.packets.module.PacketModuleExecute;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.module.ModuleProtocolData;
import cloud.hytora.node.impl.module.NodeModuleManager;

import java.nio.file.Path;
import java.util.Collection;

public class NodeRemoteModuleManager extends NodeModuleManager {


    @Override
    public void resolveModules() {

        //is remote
        PacketBuffer buffer = NodeDriver.getInstance()
                .getExecutor()
                .getPacketChannel()
                .sendQuery()
                .execute(new PacketModuleExecute(PacketModuleExecute.PayLoad.TRANSFER_MODULES))
                .syncUninterruptedly()
                .get()
                .buffer();


        CloudDriver.getInstance().getLogger().info("Downloading Modules from HeadNode...");
        Collection<ModuleProtocolData> moduleProtocolData = buffer.readCollection(new ExceptionallySupplier<ModuleProtocolData>() {
            @Override
            public ModuleProtocolData getExceptionally() throws Exception {
                return new ModuleProtocolData(
                        buffer.readFile(),
                        buffer.readFile()
                );
            }
        });


        int downloaded = 0;
        for (ModuleProtocolData protocolData : moduleProtocolData) {
            //File destination = new File(CloudDriver.Constants.MODULE_FOLDER, jarName);
            //File folder = new File(CloudDriver.Constants.MODULE_FOLDER, folderName + "/");
            CloudDriver.getInstance().getLogger().info("§7Received Module §8'§e{}§8' §7from HeadNode!", protocolData.getJarFile().getName());
            downloaded++;
        }
        CloudDriver.getInstance().getLogger().info("§7Received a total of §e{} §7modules from HeadNode§8!", downloaded);
        super.resolveModules();
        loadModules();
        enableModules();
    }


    @Override
    public void unregisterModules() {
        super.unregisterModules();

        FileUtils.delete(CloudDriver.Constants.MODULE_FOLDER.toPath());
    }

}
