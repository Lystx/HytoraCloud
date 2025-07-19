package cloud.hytora.modules.dashboard.intern.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.SecureRandom;

@Getter @AllArgsConstructor
public class WebConfig  {

    /**
     * The port of the dashboard
     */
    private final int port;


    /**
     * The security salt-key
     */
    private byte[] salt;

    /**
     * The default-config
     */
    public WebConfig() {
        this.port = 6988;
        this.generateNewSalt();
    }

    /**
     * Generates an ew Salt-key
     */
    public void generateNewSalt() {
        this.salt = new byte[16];
        new SecureRandom().nextBytes(this.salt);
    }

}
