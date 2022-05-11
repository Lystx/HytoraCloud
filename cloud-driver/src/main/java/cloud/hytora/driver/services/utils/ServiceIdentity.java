package cloud.hytora.driver.services.utils;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ServiceIdentity {

    /**
     * The name of the node this service runs on
     */
    private String node;

    /**
     * The host name of the node this service runs on
     */
    private String hostname;

    /**
     * The name of this service to identify itself
     */
    private String service;

    /**
     * The port this service runs on
     */
    private int port;


    public void save(File file) {
        try {
            DocumentFactory.newJsonDocument(this).saveToFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ServiceIdentity read(File file) {
        try {
            Document document = DocumentFactory.newJsonDocument(file);
            ServiceIdentity identity = document.toInstance(ServiceIdentity.class);

            this.node = identity.getNode();
            this.hostname = identity.getHostname();
            this.service = identity.getService();
            this.port = identity.getPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }
}
