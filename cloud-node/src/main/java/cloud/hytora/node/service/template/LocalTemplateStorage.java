package cloud.hytora.node.service.template;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.IProcessCloudServer;
import cloud.hytora.driver.services.task.ICloudServiceTaskManager;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.deployment.IDeployment;
import cloud.hytora.driver.services.template.ITemplate;
import cloud.hytora.driver.services.template.ITemplateStorage;
import cloud.hytora.driver.services.utils.ServiceShutdownBehaviour;
import cloud.hytora.node.NodeDriver;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

@Getter
public class LocalTemplateStorage implements ITemplateStorage {

    private final String name;

    private static final File GLOBAL_FOLDER = new File(NodeDriver.TEMPLATES_DIR, "GLOBAL/");
    private static final File GLOBAL_SERVICE_FOLDER = new File(NodeDriver.TEMPLATES_DIR, "GLOBAL_SERVICE/");
    private static final File GLOBAL_PROXY_FOLDER = new File(NodeDriver.TEMPLATES_DIR, "GLOBAL_PROXY/");

    public LocalTemplateStorage() {
        this.name = "local";

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

        this.checkIfTemplateFoldersNeeded();

        try {
            this.checkForUnusedTemplateFolders();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void checkIfTemplateFoldersNeeded() {
        for (IServiceTask con : CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceTaskManager.class).getAllCachedTasks()) {
            for (ITemplate template : con.getTaskGroup().getTemplates()) {
                this.createTemplate(template);
            }
            for (ITemplate template : con.getTemplates()) {
                this.createTemplate(template);
            }
        }
    }

    private void checkForUnusedTemplateFolders() throws IOException {
        File[] files = NodeDriver.TEMPLATES_DIR.listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                if (name.equalsIgnoreCase("GLOBAL") || name.equals("GLOBAL_SERVICE") || name.equals("GLOBAL_PROXY")) {
                    continue;
                }
                IServiceTask con = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceTaskManager.class).getTaskOrNull(name);
                if (con == null) {
                    FileUtils.deleteDirectory(file);
                }
            }
        }
    }

    @Override
    public void copyTemplate(@NotNull ICloudServer server, @NotNull ITemplate template, @NotNull File directory) throws Exception {
        IServiceTask serviceTask = server.getTask();

        //do not perform if wrong node
        if (!server.getRunningNodeName().equalsIgnoreCase(NodeDriver.getInstance().getNetworkExecutor().getNodeName())) {
            return;
        }

        FileUtils.copyDirectory(GLOBAL_FOLDER, directory);
        FileUtils.copyDirectory(serviceTask.getVersion().isProxy() ? GLOBAL_PROXY_FOLDER : GLOBAL_SERVICE_FOLDER, directory);

        File templateDir = new File(NodeDriver.TEMPLATES_DIR, template.buildTemplatePath());
        if (serviceTask.getTaskGroup().getShutdownBehaviour() == ServiceShutdownBehaviour.KEEP && !template.shouldCopyToStatic()) {
            //static but template does not allow copying to static
            return;
        }

        if (templateDir.exists()) { //if dynamic and dir exists just copy it
            FileUtils.copyDirectory(templateDir, directory);
        }
    }

    @Override
    public void deployService(@NotNull ICloudServer server, @NotNull IDeployment deployment) {

        //do not perform if wrong node
        if (!server.getRunningNodeName().equalsIgnoreCase(NodeDriver.getInstance().getNetworkExecutor().getNodeName())) {
            return;
        }

        IProcessCloudServer nodeCloudServer = (IProcessCloudServer) server;
        ITemplate template = deployment.getTemplate();

        if (template != null && template.getStorage() != null && template.getPrefix() != null && template.getName() != null) {
            File workingDirectory = nodeCloudServer.getWorkingDirectory();
            if (workingDirectory == null) {
                return;
            }
            File templateDirectory = new File(NodeDriver.TEMPLATES_DIR, template.buildTemplatePath());

            File[] files = workingDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    if (!deployment.getOnlyIncludedFiles().isEmpty() && !deployment.getOnlyIncludedFiles().contains(fileName)) {
                        continue;
                    }
                    if (deployment.getExclusionFiles().contains(fileName) || fileName.equalsIgnoreCase("remote.jar") || fileName.equalsIgnoreCase("plugin.jar")) {
                        continue;
                    }

                    try {
                        if (file.isDirectory()) {
                            FileUtils.copyDirectory(file, new File(templateDirectory, file.getName()));
                        } else {
                            FileUtils.copyFile(file, new File(templateDirectory, file.getName()));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

    }

    @Override
    public void deleteTemplate(@NotNull ITemplate template) {
        try {
            FileUtils.deleteDirectory(new File(NodeDriver.TEMPLATES_DIR, template.getName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createTemplate(@NotNull ITemplate template) {
        File directory = new File(NodeDriver.TEMPLATES_DIR, template.getName());
        directory.mkdirs();

        File defaultDir = new File(directory, template.getPrefix());
        defaultDir.mkdirs();

        File pluginsDir = new File(defaultDir, "plugins/");
        pluginsDir.mkdirs();

    }

    @Override
    public void close() throws IOException {
    }
}
