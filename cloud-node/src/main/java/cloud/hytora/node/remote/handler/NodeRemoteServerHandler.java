package cloud.hytora.node.remote.handler;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityNode;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.node.NodeDriver;

public class NodeRemoteServerHandler implements PacketHandler<PacketCloudEntityNode> {

    @Override
    public void handle(PacketChannel channel, PacketCloudEntityNode packet) {

        PacketBuffer buffer = packet.buffer();

        switch (packet.getPayLoad()) {
            case SERVER_START:
                CloudService cloudService = buffer.readService();
                boolean demandsResponse = buffer.readBoolean();
                INode runningNode = cloudService.getRunningNode();

                if (runningNode == null) {
                    warn("§cTried to start CloudService §e{} §cbut could not determine its running §eNode ({})§c!", cloudService.getName(), cloudService.getRunningNodeName());
                    return;
                }
                if (!runningNode.getName().equalsIgnoreCase(NodeDriver.getInstance().getNodeManager().thisNode().getName())) {
                    warn("§cReceived command to start CloudService §e{} §cfrom §e{} §cbut it was wrongly sent to this Node when it really has to be started on §e{}§c!", cloudService.getName(), channel.getPossibleNameHolder().getName(), cloudService.getRunningNodeName());
                    return;
                }

                System.out.println("Received demand for start => " + cloudService.getName() + " FROM => " + channel.getPossibleNameHolder().getName());


                CloudDriver.getInstance().getServiceManager().startService(cloudService)
                        .onTaskSucess(cloudServer -> {
                            if (demandsResponse) {
                                channel.sendResponse().setState(NetworkResponseState.OK).execute(packet);
                            }
                        })
                        .onTaskFailed(throwable -> {
                            if (demandsResponse) {
                                channel.sendResponse().setError(throwable).setState(NetworkResponseState.FAILED).execute(packet);
                            }
                        });
                break;
            case NODE_SHUTDOWN:
                String node = buffer.readString();
                if (node.equalsIgnoreCase(NodeDriver.getInstance().getNode().getName())) {
                    NodeDriver.getInstance().getNode().shutdown();
                }
                break;
            case SERVER_STOP:
                String server = buffer.readString();
                boolean needsResponse = buffer.readBoolean();

                CloudService service = CloudDriver.getInstance().getServiceManager().getCachedCloudService(server);
                if (service != null) {
                    NodeDriver.getInstance().getNode().stopServer(service);
                }
                if (needsResponse) {
                    channel.sendResponse().setState(service != null ? NetworkResponseState.OK : NetworkResponseState.FAILED).execute(packet);
                }
                break;
        }
    }
}
