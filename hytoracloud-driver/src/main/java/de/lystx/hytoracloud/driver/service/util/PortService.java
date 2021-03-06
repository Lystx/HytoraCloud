package de.lystx.hytoracloud.driver.service.util;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * This Service searches for a free Port
 * for a certain group
 * It will look for ports within a specific range
 * you can define in your {@link de.lystx.hytoracloud.driver.config.impl.NetworkConfig} under
 * proxyStartPort or serverStartPort
 */
@Getter
public class PortService implements Serializable {

    private static final long serialVersionUID = -6055013827003323170L;
    /**
     * The used ports
     */
    private final List<String> portList;

    /**
     * The used proxy ports
     */
    private final List<String> proxyPortList;

    /**
     * The start proxyPort and start serverPort
     */
    @Setter
    private int proxyPort, serverPort;

    public PortService(int proxyPort, int serverPort) {
        this.proxyPort = proxyPort;
        this.serverPort = serverPort;
        this.portList = new LinkedList<>();
        this.proxyPortList = new LinkedList<>();
    }

    /**
     * Marks port as unused
     *
     * @param port the port
     */
    public void removePort(Integer port) {
        this.portList.remove(String.valueOf(port));
    }

    /**
     * Returns free port for server
     *
     * @return free port
     */
    public int getFreePort() {
        for (int i = this.serverPort; i < (this.serverPort + 300000);) {
            if (this.portList.contains(String.valueOf(i))) {
                i++;
                continue;
            }
            this.portList.add(String.valueOf(i));
            return i;
        }
        return 404;
    }

    /**
     * Returns free port for proxy
     *
     * @return free proxy port
     */
    public int getFreeProxyPort() {
        for (int i = this.proxyPort; i < (this.proxyPort + 300000);) {
            if (this.proxyPortList.contains(String.valueOf(i))) {
                i++;
                continue;
            }
            this.proxyPortList.add(String.valueOf(i));
            return i;
        }
        return 404;
    }

    /**
     * Marks port as unused
     *
     * @param port the port
     */
    public void removeProxyPort(Integer port) {
        this.proxyPortList.remove(String.valueOf(port));
    }

}
