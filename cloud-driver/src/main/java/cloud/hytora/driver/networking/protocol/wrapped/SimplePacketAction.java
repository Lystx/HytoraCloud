package cloud.hytora.driver.networking.protocol.wrapped;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.task.ITask;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.networking.protocol.packets.defaults.RedirectPacket;
import cloud.hytora.driver.networking.IHandlerNetworkExecutor;
import cloud.hytora.driver.networking.INetworkExecutor;
import cloud.hytora.driver.networking.cluster.ClusterClientExecutor;
import cloud.hytora.driver.networking.cluster.ClusterExecutor;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.*;
import cloud.hytora.driver.networking.protocol.packets.defaults.ResponsePacket;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


public class SimplePacketAction<R> implements ChanneledPacketAction<R> {

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

    public SimplePacketAction(PacketChannel wrapper, Class<R> returnTypeClass, String identifier) {
        this.wrapper = wrapper;
        this.returnTypeClass = returnTypeClass;
        this.identifier = identifier;

        this.state = NetworkResponseState.OK;
        this.data = DocumentFactory.newJsonDocument();
        this.buffer = PacketBuffer.unsafe();
    }

    @Override
    public ChanneledPacketAction<R> state(NetworkResponseState state) {
        this.state = state;
        return this;
    }

    @Override
    public ChanneledPacketAction<R> error(Throwable state) {
        this.error = error;
        return this;
    }

    @Override
    public ChanneledPacketAction<R> data(Document document) {
        this.data = document;
        return this;
    }

    @Override
    public ChanneledPacketAction<R> buffer(Consumer<PacketBuffer> buf) {
        buf.accept(this.buffer);
        return this;
    }

    @Override
    public ChanneledPacketAction<R> buffer(PacketBuffer buf) {
        this.buffer = buf;
        return this;
    }

    @Override
    public ChanneledPacketAction<R> receivers(String... receivers) {
        this.receivers = receivers;
        return this;
    }

    @Override
    public ChanneledPacketAction<R> receivers(ConnectionType... types) {
        this.receiverTypes = types;
        return this;
    }

    @Override
    public ITask<R> execute(IPacket packet) {
        ITask<R> task = ITask.empty();
        INetworkExecutor executor = this.wrapper.executor();

        if (identifier.equalsIgnoreCase("multiQuery")) {
            if (!(executor instanceof IHandlerNetworkExecutor)) {
                throw new IllegalStateException("Can't execute MultiQuery from normal NetworkExecutor!");
            }
            Set<BufferedResponse> responses = new HashSet<>();

            AtomicInteger neededResponses = new AtomicInteger();

            if (receivers == null || receivers.length == 0) {
                if (executor instanceof ClusterExecutor) {
                    neededResponses.set(((ClusterExecutor) this.wrapper.executor()).getAllCachedConnectedClients().size());
                } else {
                    neededResponses.set(1);
                }
            } else {
                neededResponses.set(receivers.length);
            }


            ((IHandlerNetworkExecutor)executor).registerPacketHandler((PacketHandler<ResponsePacket>) (wrap, packet1) -> {

                if (packet1.transferInfo().getInternalQueryId().equals(packet.transferInfo().getInternalQueryId())) {
                    responses.add(packet1);

                    //decreasing and checking if all collected
                    if (neededResponses.decrementAndGet() <= 0) {
                        task.setResult((R) responses);
                    }
                }
            });
            this.sendPacket(packet);

        } else if (identifier.equalsIgnoreCase("singleQuery")) {

            if (!(executor instanceof IHandlerNetworkExecutor)) {
                throw new IllegalStateException("Can't execute SingleQuery from normal NetworkExecutor!");
            }

            ((IHandlerNetworkExecutor)executor).registerSelfDestructivePacketHandler((PacketHandler<ResponsePacket>) (wrap, packet1) -> {
                if (packet1.transferInfo().getInternalQueryId().equals(packet.transferInfo().getInternalQueryId())) {
                    task.setResult((R) packet1);
                }
            });
            this.sendPacket(packet);

        } else if (identifier.equalsIgnoreCase("response")) {
            ResponsePacket responsePacket = new ResponsePacket(this.wrapper.executor().getName(), error, state, data, buffer);
            responsePacket.transferInfo().setInternalQueryId(packet.transferInfo().getInternalQueryId());

            this.sendPacket(responsePacket);

        } else {
            this.sendPacket(packet);
        }

        return task;
    }

    private void sendPacket(IPacket packet) {

        INetworkExecutor executor = wrapper.executor();

        if (executor instanceof ClusterExecutor) {
            //no environment provided... sending raw using the provided NetworkParticipant
            if (receiverTypes == null || receiverTypes.length == 0) {
                executor.sendPacket(packet);
                return;
            }

            if (receivers == null || receivers.length == 0) {
                //all receivers should receive the packet
                for (ClusterClientExecutor connectedParticipant : ((ClusterExecutor) executor).getAllCachedConnectedClients()) {
                    if (Arrays.asList(receiverTypes).contains(connectedParticipant.getType())) {
                        connectedParticipant.sendPacket(packet);
                    }
                }
            } else {
                //specific receiver(s) should receive the packet
                for (String receiver : receivers) {
                    if (receiver.equalsIgnoreCase(executor.getName())) {
                        ((IHandlerNetworkExecutor) executor).handlePacket(null, packet);
                        continue;
                    }
                    ClusterClientExecutor client = ((ClusterExecutor) executor).getClient(receiver).orElse(null);
                    if (client != null) {
                        client.sendPacket(packet);
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
                executor.sendPacket(new RedirectPacket(receiver, packet));
            }

        }
    }
}
