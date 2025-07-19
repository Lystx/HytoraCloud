import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Test {

    public static void main(String[] args) throws IOException {

        List<ProtocolAddress> addresses =
                Arrays.asList(
                        new ProtocolAddress("127.0.0.1", 2438),
                        new ProtocolAddress("127.0.0.1", 23432),
                        new ProtocolAddress("127.0.0.1", 1621),
                        new ProtocolAddress("127.0.0.1", 1694),
                        new ProtocolAddress("127.0.0.1", 8842),
                        new ProtocolAddress("127.0.0.1", 7845)
                );

        ProtocolAddress[] protocolAddresses = toArray(addresses);

        System.out.println(protocolAddresses.length);
        System.out.println(protocolAddresses[3]);
    }


    public static <T> T[] toArray(List<T> list) {
        T[] toR = (T[]) java.lang.reflect.Array.newInstance(list.get(0)
                .getClass(), list.size());
        for (int i = 0; i < list.size(); i++) {
            toR[i] = list.get(i);
        }
        return toR;
    }
}
