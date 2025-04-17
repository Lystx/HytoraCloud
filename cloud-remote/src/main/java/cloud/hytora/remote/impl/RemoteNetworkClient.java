package cloud.hytora.remote.impl;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.defaults.remote.RemoteConnectEvent;
import cloud.hytora.driver.networking.cluster.client.AdvancedClusterParticipant;
import cloud.hytora.driver.networking.cluster.client.ClusterParticipant;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import io.netty.channel.ChannelHandlerContext;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;

public class RemoteNetworkClient extends AdvancedClusterParticipant {

    public RemoteNetworkClient(String authKey, String clientName, String hostname, int port, Document customData, Runnable... connectionFailed) {
        super(authKey, clientName, ConnectionType.SERVICE, customData);

        this.bootAsync().openConnection(hostname, port).registerListener(channelTask -> {
            if (channelTask.isPresent()) {
                System.out.println("\n" +
                        "   _____ _                 _ ____       _     _            \n" +
                        "  / ____| |               | |  _ \\     (_)   | |           \n" +
                        " | |    | | ___  _   _  __| | |_) |_ __ _  __| | __ _  ___ \n" +
                        " | |    | |/ _ \\| | | |/ _` |  _ <| '__| |/ _` |/ _` |/ _ \\\n" +
                        " | |____| | (_) | |_| | (_| | |_) | |  | | (_| | (_| |  __/\n" +
                        "  \\_____|_|\\___/ \\__,_|\\__,_|____/|_|  |_|\\__,_|\\__, |\\___|\n" +
                        "                                                 __/ |     \n" +
                        "                                                |___/      ");
                System.out.println("-------------------------");
                System.out.println("[CloudRemote] Remote has connected to cloud at [" + hostname + ":" + port + "]");
                System.out.println("[CloudRemote] This Remote is now registered and has Hands shaken with the CloudSystem");
                CloudDriver.getInstance().getEventManager().callEventOnlyLocally(new RemoteConnectEvent());
            } else {
                CloudDriver.getInstance().getLogger().info("This service couldn't connect to the Cluster!");
                channelTask.error().printStackTrace();
                for (Runnable runnable : connectionFailed) runnable.run();
            }
        });
    }

    @Override
    public void sendPacket(IPacket packet) {
        super.sendPacket(packet);
    }

    @Override
    public void onAuthenticationChanged(PacketChannel wrapper) {
        CloudDriver.getInstance().getLogger().info("This service was authenticated by the cluster");
    }

    @Override
    public void onActivated(ChannelHandlerContext channelHandlerContext) {
        CloudDriver.getInstance().getLogger().info("This service successfully connected to the cluster.");
    }

    @Override
    public void onClose(ChannelHandlerContext channelHandlerContext) {
        CloudDriver.getInstance().getLogger().info("This service disconnected from the cluster");
    }

}
