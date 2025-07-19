package cloud.hytora.driver.networking.protocol.wrapped;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.packets.other.PacketRedirecting;
import cloud.hytora.driver.networking.HandlingNetworkExecutor;
import cloud.hytora.driver.networking.NetworkExecutor;
import cloud.hytora.driver.networking.cluster.ClusterExecutor;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.*;
import cloud.hytora.driver.networking.packets.response.PacketResponse;
import cloud.hytora.driver.networking.protocol.types.ConnectionType;
import lombok.Setter;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;


public class SimplePacketAction<R> implements PacketAction<R> {

    //global values
    private final PacketChannel wrapper;
    private final Class<R> returnTypeClass;

    private Throwable error;
    private final String identifier;

    private String[] receivers;
    private ConnectionType[] receiverTypes;

    //response values
    private NetworkResponseState state;
    private Document data;
    private PacketBuffer buffer;

    @Setter
    private IPacket packet;

    public SimplePacketAction(PacketChannel wrapper, Class<R> returnTypeClass, String identifier) {
        this.wrapper = wrapper;
        this.returnTypeClass = returnTypeClass;
        this.identifier = identifier;

        this.state = NetworkResponseState.OK;
        this.data = Document.gson();
        this.buffer = PacketBuffer.unPooled();
    }

    @Override
    public PacketAction<R> setState(NetworkResponseState state) {
        this.state = state;
        return this;
    }

    @Override
    public PacketAction<R> setError(Throwable error) {
        this.error = error;
        return this;
    }

    @Override
    public PacketAction<R> setDocument(Document document) {
        this.data = document;
        return this;
    }

    @Override
    public PacketAction<R> setDocument(Consumer<Document> document) {
        Document d = Document.gson();
        document.accept(d);
        return setDocument(d);
    }

    @Override
    public PacketAction<R> setBuffer(Consumer<PacketBuffer> buf) {
        buf.accept(this.buffer);
        return this;
    }

    @Override
    public PacketAction<R> setBuffer(PacketBuffer buf) {
        this.buffer = buf;
        return this;
    }

    @Override
    public PacketAction<R> setReceivers(String... receivers) {
        this.receivers = receivers;
        return this;
    }

    @Override
    public PacketAction<R> setReceivers(ConnectionType... types) {
        this.receiverTypes = types;
        return this;
    }

    @Override
    public Task<R> execute() {
        return this.execute(this.packet);
    }

    @Override
    public Task<R> execute(IPacket packet) {
        Task<R> task = Task.empty();
        task.denyNull();
        NetworkExecutor executor = this.wrapper.executor();

        if (identifier.equalsIgnoreCase("singleQuery")) {

            if (!(executor instanceof HandlingNetworkExecutor)) {
                throw new IllegalStateException("Can't execute SingleQuery from normal NetworkExecutor!");
            }

            UUID queryId = packet.transferInfo().getInternalQueryId();
            ((HandlingNetworkExecutor) executor).registerSelfDestructivePacketHandler((PacketHandler<PacketResponse>) (wrap, packet1) -> {
                if (packet1.transferInfo().getInternalQueryId().equals(queryId)) {
                    task.setResult((R) packet1);
                }
            });
            this.sendPacket(packet);

        } else if (identifier.equalsIgnoreCase("response")) {
            PacketResponse responsePacket = new PacketResponse(this.wrapper.executor().getName(), error, state, data, buffer);
            responsePacket.transferInfo().setInternalQueryId(packet.transferInfo().getInternalQueryId());

            this.sendPacket(responsePacket);

        } else {
            this.sendPacket(packet);
        }


        return task;
    }

    private void sendPacket(IPacket packet) {

        NetworkExecutor executor = wrapper.executor();

        if (executor instanceof ClusterExecutor) {
            //no environment provided... sending raw using the provided NetworkParticipant
            if (receiverTypes == null || receiverTypes.length == 0) {
                executor.sendPacket(packet);
                return;
            }

            if (receivers == null || receivers.length == 0) {
                //all receivers should receive the packet
                for (PacketChannel packetChannel : ((ClusterExecutor) executor).getAllConnectedChannels()) {
                    if (Arrays.asList(receiverTypes).contains(packetChannel.getType())) {
                        packetChannel.sendPacket(packet);
                    }
                }
            } else {
                //specific receiver(s) should receive the packet
                for (String receiver : receivers) {
                    if (receiver.equalsIgnoreCase(executor.getName())) {
                        ((HandlingNetworkExecutor) executor).handlePacket(null, packet);
                        continue;
                    }
                    PacketChannel channel = ((ClusterExecutor) executor).getConnectedChannel(receiver);
                    if (channel != null) {
                        channel.sendPacket(packet);
                    } else {
                        System.out.println(StringUtils.format("Tried sending packet {0} to following receivers {1} of types(s) {2}", packet.getClass().getSimpleName(), Arrays.asList(receivers), Arrays.asList(receiverTypes)));
                    }
                }
            }

        } else {
            //no environment provided... sending raw using the provided NetworkParticipant
            if (receiverTypes == null || receiverTypes.length == 0) {
                executor.sendPacket(packet);
                return;
            }

            if (receivers == null || receivers.length == 0) {
                executor.sendPacket(packet);
                return;
            }

            for (String receiver : receivers) {
                //forwarding it to the right receiver(s)
                executor.sendPacket(new PacketRedirecting(receiver, packet));
            }

        }
    }
}
