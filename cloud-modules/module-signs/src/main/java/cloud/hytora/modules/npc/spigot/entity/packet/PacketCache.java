package cloud.hytora.modules.npc.spigot.entity.packet;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class PacketCache {
    protected static final ImmutableMap<Method, PacketValue> VALUE_LOOKUP_BY_NAME;

    static {
        ImmutableMap.Builder<Method, PacketValue> methodPacketValueBuilder = ImmutableMap.builder();
        for (Method method : NPCPacket.class.getMethods()) {
            if (method.isAnnotationPresent(PacketValue.class))
                methodPacketValueBuilder.put(method, method.getAnnotation(PacketValue.class));
        }
        VALUE_LOOKUP_BY_NAME = methodPacketValueBuilder.build();
    }

    private final Map<String, Object> packetResultCache;
    private final NPCPacket proxyInstance;

    public PacketCache(NPCPacket packet) {
        this.proxyInstance = (NPCPacket) Proxy.newProxyInstance(packet
                .getClass().getClassLoader(), new Class[]{NPCPacket.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (PacketCache.VALUE_LOOKUP_BY_NAME.containsKey(method)) {

                    if (!VALUE_LOOKUP_BY_NAME.containsKey(method)) {
                        throw new IllegalStateException("value not found for method: " + method.getName());
                    }
                    PacketValue packetValue = VALUE_LOOKUP_BY_NAME.get(method);
                    String keyString = packetValue.valueType().resolve(packetValue.keyName(), args);
                    return packetResultCache.computeIfAbsent(keyString, o -> {
                        try {
                            return method.invoke(packet, args);
                        } catch (InvocationTargetException | IllegalAccessException operationException) {
                            throw new AssertionError("can't invoke method: " + method.getName(), operationException);
                        }
                    });
                }
                return method.invoke(packet, args);
            }
        });
        this.packetResultCache = new ConcurrentHashMap<>();
    }

    public PacketCache() {
        this(PacketFactory.PACKET_FOR_CURRENT_VERSION);
    }


    public void flushCache(String... strings) {
        Set<Map.Entry<String, Object>> set = this.packetResultCache.entrySet();
        for (String string : strings) {
            set.removeIf(entry -> entry.getKey().startsWith(string));
        }
    }

}
