package cloud.hytora.node.impl.handler.packet;

import cloud.hytora.common.function.ExceptionallyConsumer;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.entity.node.data.DefaultNodeData;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.entity.node.NodeManager;
import cloud.hytora.driver.entity.node.data.INodeCycleData;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityNode;
import lombok.SneakyThrows;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class NodeDataCycleHandler implements PacketHandler<PacketCloudEntityNode> {

    @SneakyThrows
    @Override
    public void handle(PacketChannel channel, PacketCloudEntityNode packet) {

        PacketBuffer buffer = packet.buffer();

        switch (packet.getPayLoad()) {
            case REQUEST_FILES:
                String specificFileName = buffer.readString();
                if (specificFileName.equalsIgnoreCase("none")) {

                    packet.sendResponse()
                            .setState(NetworkResponseState.OK)
                            .setBuffer(buf -> {

                                File[] files = CloudDriver.Constants.STORAGE_VERSIONS_FOLDER.listFiles();
                                File[] array;
                                if (files == null) {
                                    array = new File[0];
                                } else {
                                    array = files;
                                }
                                List<File> listFiles = Arrays.asList(array);
                                Map<Path, String> map = new HashMap<>();
                                for (File file : listFiles) {
                                    map.put(file.toPath(), file.getName());
                                }
                                buf.writeMap(map, (buffer1, path) -> buffer1.writeString(path.toString()), PacketBuffer::writeString);

                            }).execute();
                } else {
                    Path path = Paths.get(specificFileName);
                    packet.sendResponse()
                            .setState(NetworkResponseState.OK)
                            .setBuffer((ExceptionallyConsumer<PacketBuffer>) buf -> buf.writeFile(path.toFile()))
                            .execute();
                }

                break;
            case DATA_REQUEST:
                break;
            case DATA_RESPONSE:
                break;
            case CYCLE_DATA:

                String name = buffer.readString();
                INodeCycleData data = buffer.readObject(DefaultNodeData.class);
                Logger logger = CloudDriver.getInstance().getLogger();
                NodeManager nodeManager = CloudDriver.getInstance().getNodeManager();

                INode node = nodeManager.getCachedNode(name);

                if (node == null) {
                    logger.warn("Tried updating non-existent node {}! Data: {}", name, data);
                    return;
                }
                logger.trace("Updated Node {} => {}", node.getName(), data);
                node.setLastCycleData(data);
                node.update(PublishingType.GLOBAL);

                break;
            case SERVER_START:
                break;
            case SERVER_STOP:
                break;
            case NODE_SHUTDOWN:
                break;
        }

    }
}
