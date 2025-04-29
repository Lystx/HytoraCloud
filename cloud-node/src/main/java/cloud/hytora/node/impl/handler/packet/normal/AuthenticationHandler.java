package cloud.hytora.node.impl.handler.packet.normal;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.PublishingType;
import cloud.hytora.driver.networking.packets.AuthenticationPacket;
import cloud.hytora.driver.networking.protocol.packets.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.impl.DefaultServiceCycleData;
import cloud.hytora.driver.services.impl.UniversalCloudServer;
import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.node.NodeDriver;


public class AuthenticationHandler implements PacketHandler<AuthenticationPacket> {

    @Override
    public void handle(PacketChannel channel, AuthenticationPacket packet) {

        AuthenticationPacket.AuthenticationPayload payload = packet.getPayload();
        RemoteIdentity identity = packet.getIdentity();

        String authKey = NodeDriver.getInstance().getNode().getConfig().getAuthKey();
        switch (payload) {
            case NODE: {
                // TODO: 28.04.2025 implement

                break;
            }

            case SERVICE: {
                if (!authKey.equalsIgnoreCase(identity.getAuthKey())) {
                    error("§8'§e{}§8' §7provided §cwrong authentication§8!", identity.getName(), payload, channel);

                    packet.respond(NetworkResponseState.FAILED);

                    channel.close();
                    return;
                }

                String serviceName = identity.getName();
                ICloudService service = CloudDriver.getInstance().getServiceManager().getCachedCloudService(serviceName);
                if (service == null) {
                    warn("§8'§e{}§8' §cis unknown to the Cloud", identity.getName());

                    packet.respond(NetworkResponseState.FAILED);

                    channel.close();
                    return;
                }
                debug("§8'§e{}§8' §7provided auth for§8: §8'§e{}§8' §8| §8'§e{}§8'", identity.getName(), payload, channel);

                ((UniversalCloudServer)service).setChannel(channel);
                service.setLastCycleData(new DefaultServiceCycleData(Document.emptyDocument(), System.currentTimeMillis(), -1));

                service.update(PublishingType.INTERNAL);

                packet.respond(NetworkResponseState.OK, packetBuffer -> packetBuffer.writeString("This is a test"));

                break;
            }

        }

    }
}
