package cloud.hytora.remote.impl.module;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.document.wrapped.StorableDocument;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.exception.IncompatibleDriverEnvironment;
import cloud.hytora.driver.module.IModule;
import cloud.hytora.driver.module.controller.ModuleClassLoader;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.IModuleManager;
import cloud.hytora.driver.module.controller.base.ModuleConfig;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.packet.RemoteModuleControllerPacket;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
public class RemoteModuleController implements ModuleController {

    private boolean enabled;
    private boolean initialized;

    private ModuleState state;
    private ModuleConfig moduleConfig;

    private Document config;

    public @NotNull StorableDocument getConfig() {
        return DocumentFactory.newStorableDocument(config, getJarFile());
    }

    @Override
    public boolean isEnabled(boolean defaultValue) {
        return enabled;
    }

    @Override
    public void loadModule() {
        CloudDriver.getInstance().getNetworkExecutor().sendPacket(new RemoteModuleControllerPacket(this.moduleConfig, RemoteModuleControllerPacket.PayLoad.LOAD_MODULE));
    }

    @Override
    public void enableModule() {
        CloudDriver.getInstance().getNetworkExecutor().sendPacket(new RemoteModuleControllerPacket(this.moduleConfig, RemoteModuleControllerPacket.PayLoad.ENABLE_MODULE));

    }

    @Override
    public void disableModule() {
        CloudDriver.getInstance().getNetworkExecutor().sendPacket(new RemoteModuleControllerPacket(this.moduleConfig, RemoteModuleControllerPacket.PayLoad.DISABLE_MODULE));
    }

    @Override
    public void unregisterModule() {
        CloudDriver.getInstance().getNetworkExecutor().sendPacket(new RemoteModuleControllerPacket(this.moduleConfig, RemoteModuleControllerPacket.PayLoad.UNREGISTER_MODULE));
    }

    public @NotNull IModule getModule() {
        return () -> RemoteModuleController.this;
    }

    @NotNull
    @Override
    public IModuleManager getManager() {
        return CloudDriver.getInstance().getProviderRegistry().getUnchecked(IModuleManager.class);
    }

    Path jarFile;

    @NotNull
    @Override
    public Path getJarFile() {
        if (jarFile == null) {
            jarFile = Paths.get(CloudDriver.getInstance().getNetworkExecutor().getPacketChannel().prepareSingleQuery().execute(new RemoteModuleControllerPacket(this.moduleConfig, RemoteModuleControllerPacket.PayLoad.GET_JAR_FILE)).syncUninterruptedly().get().buffer().readString());
        }
        return jarFile;
    }

    Path dataFolder;

    @NotNull
    @Override
    public Path getDataFolder() {
        if (dataFolder == null) {
            dataFolder = Paths.get(CloudDriver.getInstance().getNetworkExecutor().getPacketChannel().prepareSingleQuery().execute(new RemoteModuleControllerPacket(this.moduleConfig, RemoteModuleControllerPacket.PayLoad.GET_DATA_FOLDER)).syncUninterruptedly().get().buffer().readString());
        }
        return dataFolder;
    }

    @NotNull
    @Override
    public StorableDocument reloadConfig() {
        return DocumentFactory.newStorableDocument(CloudDriver.getInstance().getNetworkExecutor().getPacketChannel().prepareSingleQuery().execute(new RemoteModuleControllerPacket(this.moduleConfig, RemoteModuleControllerPacket.PayLoad.RELOAD_CONFIG)).syncUninterruptedly().get().buffer().readDocument(), getJarFile());
    }

    @NotNull
    @Override
    public ModuleClassLoader getClassLoader() {
        throw new IncompatibleDriverEnvironment(DriverEnvironment.NODE);
    }

    @Override
    public void registerModuleTasks(Object classHolder) {

    }

    @Override
    public void initConfig() throws Exception {
        CloudDriver.getInstance().getNetworkExecutor().sendPacket(new RemoteModuleControllerPacket(this.moduleConfig, RemoteModuleControllerPacket.PayLoad.INIT_CONFIG));
    }

    @Override
    public void initModule() throws Exception {
        CloudDriver.getInstance().getNetworkExecutor().sendPacket(new RemoteModuleControllerPacket(this.moduleConfig, RemoteModuleControllerPacket.PayLoad.INIT_MODULE));

    }

    @Override
    public void applyBuffer(BufferState s, @NotNull PacketBuffer buf) throws IOException {
        if (s == BufferState.READ) {
            enabled = buf.readBoolean();
            initialized = buf.readBoolean();

            state = buf.readEnum(ModuleState.class);
            moduleConfig = buf.readObject(ModuleConfig.class);

            config = buf.readDocument();
        }
    }
}
