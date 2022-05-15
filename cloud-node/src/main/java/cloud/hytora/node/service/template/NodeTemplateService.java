package cloud.hytora.node.service.template;


import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.node.NodeDriver;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class NodeTemplateService {

    private static final File GLOBAL_FOLDER = new File(NodeDriver.TEMPLATES_DIR, "GLOBAL/");
    private static final File GLOBAL_SERVICE_FOLDER = new File(NodeDriver.TEMPLATES_DIR, "GLOBAL_SERVICE/");
    private static final File GLOBAL_PROXY_FOLDER = new File(NodeDriver.TEMPLATES_DIR, "GLOBAL_PROXY/");

    public NodeTemplateService() {

        try {
            FileUtils.forceMkdir(GLOBAL_FOLDER);
            FileUtils.forceMkdir(new File(GLOBAL_FOLDER, "plugins/"));

            FileUtils.forceMkdir(GLOBAL_SERVICE_FOLDER);
            FileUtils.forceMkdir(new File(GLOBAL_SERVICE_FOLDER, "plugins/"));

            FileUtils.forceMkdir(GLOBAL_PROXY_FOLDER);
            FileUtils.forceMkdir(new File(GLOBAL_PROXY_FOLDER, "plugins/"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copyTemplates(CloudServer service) throws IOException {

        File parent = (service.getConfiguration().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File serviceFolder = new File(parent, service.getName() + "/");

        FileUtils.copyDirectory(GLOBAL_FOLDER, serviceFolder);
        FileUtils.copyDirectory(service.getConfiguration().getVersion().isProxy() ? GLOBAL_PROXY_FOLDER : GLOBAL_SERVICE_FOLDER, serviceFolder);

        File templateDir = new File(NodeDriver.TEMPLATES_DIR, service.getConfiguration().getName() + "/" + service.getConfiguration().getTemplate());
        if (templateDir.exists()) {
            FileUtils.copyDirectory(templateDir, serviceFolder);
        }
    }

    public void createTemplateFolder(ServerConfiguration con) {
        if (!con.getNode().equalsIgnoreCase(NodeDriver.getInstance().getExecutor().getNodeName())) {
            return;
        }
        File dir = new File(NodeDriver.TEMPLATES_DIR, con.getName() + "/" + con.getTemplate() + "/");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File pluginsDir = new File(dir, "plugins/");
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs();
        }
    }

}
