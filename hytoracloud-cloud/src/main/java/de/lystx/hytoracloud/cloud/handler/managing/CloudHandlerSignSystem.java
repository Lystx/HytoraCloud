package de.lystx.hytoracloud.cloud.handler.managing;

import de.lystx.hytoracloud.cloud.manager.other.SignService;
import de.lystx.hytoracloud.driver.CloudDriver;
import de.lystx.hytoracloud.driver.connection.protocol.netty.global.packet.IPacket;
import de.lystx.hytoracloud.driver.connection.protocol.netty.global.packet.handling.IPacketHandler;
import de.lystx.hytoracloud.driver.serverselector.sign.CloudSign;
import de.lystx.hytoracloud.driver.packets.in.PacketInCloudSignCreate;
import de.lystx.hytoracloud.driver.packets.in.PacketInCloudSignDelete;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CloudHandlerSignSystem implements IPacketHandler {

    private final CloudDriver cloudDriver;

    @Override
    public void handle(IPacket packet) {

        if (packet instanceof PacketInCloudSignCreate) {

            PacketInCloudSignCreate packetInCloudSignCreate = (PacketInCloudSignCreate)packet;
            CloudSign sign = packetInCloudSignCreate.getCloudSign();

            CloudSign get = CloudDriver.getInstance().getServiceRegistry().getInstance(SignService.class).getCloudSign(sign.getX(), sign.getY(), sign.getZ(), sign.getWorld());
            if (get == null) {
                CloudDriver.getInstance().getServiceRegistry().getInstance(SignService.class).getCloudSigns().add(sign);
                CloudDriver.getInstance().getServiceRegistry().getInstance(SignService.class).save();
                this.cloudDriver.reload();
            }

        } else if (packet instanceof PacketInCloudSignDelete) {

            PacketInCloudSignDelete packetInCloudSignDelete = (PacketInCloudSignDelete)packet;
            CloudSign sign = packetInCloudSignDelete.getCloudSign();

            CloudSign get = CloudDriver.getInstance().getServiceRegistry().getInstance(SignService.class).getCloudSign(sign.getX(), sign.getY(), sign.getZ(), sign.getWorld());
            if (get != null) {
                CloudDriver.getInstance().getServiceRegistry().getInstance(SignService.class).getCloudSigns().remove(get);
                CloudDriver.getInstance().getServiceRegistry().getInstance(SignService.class).saveAndReload();
                this.cloudDriver.reload();
            }
        }

    }
}
