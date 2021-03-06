package de.lystx.hytoracloud.driver.packets.in;

import de.lystx.hytoracloud.driver.connection.protocol.netty.global.packet.impl.json.JsonPacket;
import de.lystx.hytoracloud.driver.connection.protocol.netty.global.packet.impl.json.PacketSerializable;
import de.lystx.hytoracloud.driver.utils.json.JsonObject;
import de.lystx.hytoracloud.driver.utils.json.PropertyObject;
import de.lystx.hytoracloud.driver.service.IService;


import de.lystx.hytoracloud.driver.wrapped.ServiceObject;
import lombok.AllArgsConstructor;
import lombok.Getter;



@Getter @AllArgsConstructor
public class PacketInStartService extends JsonPacket {

    @PacketSerializable(ServiceObject.class)
    private IService service;


    @PacketSerializable(PropertyObject.class)
    private JsonObject<?> properties;

}
