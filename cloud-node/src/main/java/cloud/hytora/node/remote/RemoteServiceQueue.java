package cloud.hytora.node.remote;

import cloud.hytora.driver.networking.packets.other.PacketServiceQueue;
import cloud.hytora.node.ServiceQueue;

import java.util.Collection;

public class RemoteServiceQueue implements ServiceQueue {


    @Override
    public void dequeue() {
        PacketServiceQueue.forType(PacketServiceQueue.PayLoad.DEQUEUE).publish();
    }

    @Override
    public void queue() {
        PacketServiceQueue.forType(PacketServiceQueue.PayLoad.QUEUE).publish();
    }

    @Override
    public Collection<String> getPausedGroups() {
        return PacketServiceQueue.forType(PacketServiceQueue.PayLoad.SCP_GET_PAUSED_GROUPS).sendQuery().execute().syncUninterruptedly().get().buffer().readStringCollection();
    }

    @Override
    public void addPausedGroup(String name) {
        PacketServiceQueue.forType(PacketServiceQueue.PayLoad.SCP_ADD_GROUP, buf -> buf.writeString(name)).publish();
    }

    @Override
    public void removePausedGroup(String name) {
        PacketServiceQueue.forType(PacketServiceQueue.PayLoad.SCP_REMOVE_GROUP, buf -> buf.writeString(name)).publish();
    }
}
