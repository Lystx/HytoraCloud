package cloud.hytora;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Expiration {


    /**
     * The static instance for access
     */
    private static Expiration instance;


    public static Expiration getInstance() {
        return instance == null ? (instance = new Expiration()) : instance;
    }

    private final Map<String, Long> waiting;

    public Expiration() {
        this.waiting = new HashMap<>();
    }

    public boolean hasExpired(String name) {
        if (waiting.get(name) == null) {
            return true;
        }
        Long expirationDate = waiting.get(name);
        long currentTime = System.currentTimeMillis();
        return expirationDate != -1 && currentTime > expirationDate;
    }

    public void wait(String name) {
        this.wait(name, TimeUnit.SECONDS, 3);
    }

    public void wait(String name, TimeUnit unit, long value) {
        long calculatedTimeOut = unit.toMillis(value);
        calculatedTimeOut+= System.currentTimeMillis(); //adding current time to timeOut
        this.waiting.put(name, calculatedTimeOut);
    }


}
