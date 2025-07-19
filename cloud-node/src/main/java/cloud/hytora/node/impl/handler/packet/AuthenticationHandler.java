package cloud.hytora.node.impl.handler.packet;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.networking.packets.auth.PacketAuthentication;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.impl.DefaultServiceCycleData;
import cloud.hytora.driver.entity.services.impl.UniversalCloudServer;
import cloud.hytora.driver.entity.services.utils.RemoteIdentity;
import cloud.hytora.node.NodeDriver;


public class AuthenticationHandler implements PacketHandler<PacketAuthentication> {

    @Override
    public void handle(PacketChannel channel, PacketAuthentication packet) {

        PacketAuthentication.AuthenticationPayload payload = packet.getPayload();
        RemoteIdentity identity = packet.getIdentity();

        String authKey = NodeDriver.getInstance().getNode().getConfig().getAuthKey();
        switch (payload) {
            case NODE: {

                break;
            }

            case SERVICE: {
                if (!authKey.equalsIgnoreCase(identity.getAuthKey())) {
                    error("§8'§e{}§8' §7provided §cwrong authentication§8!", identity.getName(), payload, channel);

                    packet.sendResponse().setState(NetworkResponseState.FAILED).setBuffer(buf -> buf.writeString("This Service provided a wrong AuthKey!")).execute();

                    channel.close();
                    return;
                }

                String serviceName = identity.getName();
                CloudService service = CloudDriver.getInstance().getServiceManager().getCachedCloudService(serviceName);
                if (service == null) {
                    warn("§8'§e{}§8' §cis unknown to the Cloud", identity.getName());

                    packet.sendResponse().setState(NetworkResponseState.FAILED).setBuffer(buffer -> buffer.writeString("This Service is unknown to the running Node!")).execute();

                    channel.close();
                    return;
                }
                debug("§8'§e{}§8' §7provided auth for§8: §8'§e{}§8' §8| §8'§e{}§8'", identity.getName(), payload, channel);

                ((UniversalCloudServer) service).setChannel(channel);
                service.setLastCycleData(new DefaultServiceCycleData(Document.empty(), System.currentTimeMillis(), -1));

                service.update(PublishingType.INTERNAL);

                packet.sendResponse().setState(NetworkResponseState.OK).execute();

                break;
            }

        }

    }
}
