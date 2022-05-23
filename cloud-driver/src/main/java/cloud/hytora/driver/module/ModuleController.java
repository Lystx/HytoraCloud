package cloud.hytora.driver.module;

import cloud.hytora.document.wrapped.StorableDocument;
import cloud.hytora.driver.module.controller.base.ModuleConfig;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.ModuleClassLoader;
import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;

import javax.annotation.Nonnull;
import java.nio.file.Path;


public interface ModuleController extends Bufferable {

	boolean isEnabled();

	boolean isEnabled(boolean defaultValue);

	boolean isInitialized();

	@Nonnull
	Module getModule();

	void loadModule();

	void enableModule();

	void disableModule();

	void unregisterModule();

	@Nonnull
	ModuleManager getManager();

	@Nonnull
	Path getJarFile();

	@Nonnull
	Path getDataFolder();

	@Nonnull
	StorableDocument getConfig();

	@Nonnull
	StorableDocument reloadConfig();

	@Nonnull
    ModuleState getState();

	@Nonnull
    ModuleConfig getModuleConfig();

	@Nonnull
    ModuleClassLoader getClassLoader();

}
