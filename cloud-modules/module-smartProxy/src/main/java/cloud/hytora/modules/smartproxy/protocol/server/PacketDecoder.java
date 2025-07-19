package cloud.hytora.modules.smartproxy.protocol.server;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.modules.smartproxy.SmartProxyModule;
import cloud.hytora.modules.smartproxy.protocol.packet.MinecraftPacket;
import cloud.hytora.modules.smartproxy.protocol.packet.PacketBuffer;
import cloud.hytora.modules.smartproxy.protocol.packet.PingPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


@ChannelHandler.Sharable
public class PacketDecoder extends SimpleChannelInboundHandler<ByteBuf> {

    public PacketDecoder() {
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {

            InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();

            PacketBuffer buffer = new PacketBuffer(buf);
            int packetLength = buffer.readSignedVarInt();
            int packetID = buffer.readSignedVarInt();

            Class<? extends MinecraftPacket> aClass = SmartProxyModule.MINECRAFT_PACKETS.get(packetID);

            if (aClass == null) {
                CloudDriver.getInstance().getLogger().error("§cReceived Packet with id §e" + packetID + " §cthat is not registered!");
                return;
            }
            MinecraftPacket minecraftPacket = aClass.newInstance();

            minecraftPacket.read(buffer);
            minecraftPacket.handle();
            buf.retain();

            if (minecraftPacket instanceof PingPacket) {
                PingPacket pingPacket = (PingPacket) minecraftPacket;

                String hostName = pingPacket.getHostName();

                hostName = hostName.replace("localhost", "127.0.0.1");
                hostName = hostName.replace("192.168.178.82", "127.0.0.1");
                pingPacket.setHostName(hostName);

                List<CloudService> proxies = CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream().filter(s -> s.getTask().getVersion().isProxy()).collect(Collectors.toList());
                CloudService service = proxies.get(new Random().nextInt(proxies.size()));

                if (service != null) {
                    CloudService freeProxy = SmartProxyModule.getInstance().getFreeProxy(service.getTask(), pingPacket.getState());
                    if (freeProxy == null) {
                        CloudDriver.getInstance().getLogger().info("§cNo free Proxy for group §e" + service.getTask().getName() + " could be found. Shutting down...");
                        ctx.channel().close();
                        return;
                    }
                    SmartProxyModule.getInstance().forwardRequestToNextProxy(ctx.channel(), freeProxy, buf, pingPacket.getState());
                }
            }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //Ignoring exceptions at first
    }

}
