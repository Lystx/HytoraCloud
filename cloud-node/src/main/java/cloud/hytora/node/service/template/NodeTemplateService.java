package cloud.hytora.node.service.template;


import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.node.NodeDriver;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class NodeTemplateService {

    private static final String EVERY_FOLDER = "templates/EVERY/";
    private static final String EVERY_SERVICE_FOLDER = "templates/EVERY_SERVICE/";
    private static final String EVERY_PROXY_FOLDER = "templates/EVERY_PROXY/";

    public NodeTemplateService() {
        this.initFolder(EVERY_FOLDER);
        this.initFolder(EVERY_SERVICE_FOLDER);
        this.initFolder(EVERY_PROXY_FOLDER);
    }

    public void copyTemplates(CloudServer service) throws IOException {

        File parent = (service.getConfiguration().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File serviceFolder = new File(parent, service.getName() + "/");

        FileUtils.copyDirectory(new File(EVERY_FOLDER), serviceFolder);
        FileUtils.copyDirectory(new File(service.getConfiguration().getVersion().isProxy() ? EVERY_PROXY_FOLDER : EVERY_SERVICE_FOLDER), serviceFolder);

        File templateDirection = new File("templates/" + service.getConfiguration().getTemplate());
        if (templateDirection.exists()) {
            FileUtils.copyDirectory(templateDirection, serviceFolder);
        }
    }

    public void createTemplateFolder(ServerConfiguration group) {
        if (!group.getNode().equalsIgnoreCase(NodeDriver.getInstance().getExecutor().getNodeName())) return;
        File file = new File("templates/" + group.getTemplate());
        if (!file.exists()) file.mkdirs();
    }

    @SneakyThrows
    public void initFolder(String file) {
        FileUtils.forceMkdir(new File(file));
    }

}
