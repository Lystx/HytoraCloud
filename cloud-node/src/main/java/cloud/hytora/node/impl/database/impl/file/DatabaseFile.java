package cloud.hytora.node.impl.database.impl.file;

import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;

import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.driver.services.configuration.SimpleServerConfiguration;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.database.IDatabase;


import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseFile implements IDatabase {


    public DatabaseFile() {
        NodeDriver.CONFIGURATIONS_FOLDER.mkdirs();
    }

    @Override
    public void connect() {
        CloudDriver.getInstance().getLogger().info("The connection is now established to the database.");
    }

    @Override
    public @NotNull Wrapper<Boolean> disconnect() {
        Wrapper<Boolean> wrapper = Wrapper.empty();
        wrapper.setResult(true);
        return wrapper;
    }

    @Override
    public List<ServerConfiguration> getAllServiceGroups() {
        List<ServerConfiguration> serviceGroups = new ArrayList<>();
        for (File file : NodeDriver.CONFIGURATIONS_FOLDER.listFiles()) {
            try {
                cloud.hytora.document.Document document = DocumentFactory.newJsonDocument(file);
                serviceGroups.add(document.toInstance(SimpleServerConfiguration.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return serviceGroups;
    }

    @Override
    public void addGroup(@NotNull ServerConfiguration serviceGroup) {
        File file = new File(NodeDriver.CONFIGURATIONS_FOLDER, serviceGroup.getName() + ".json");
        Document document = DocumentFactory.newJsonDocument(serviceGroup);
        try {
            document.saveToFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeGroup(@NotNull ServerConfiguration serviceGroup) {
        File file = new File(NodeDriver.CONFIGURATIONS_FOLDER, serviceGroup.getName() + ".json");
        file.delete();
    }
}
