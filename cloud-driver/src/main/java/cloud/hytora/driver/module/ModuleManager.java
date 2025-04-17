package cloud.hytora.driver.module;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.List;


public interface ModuleManager {

	void resolveModules();

	void loadModules();

	void enableModules();

	void disableModules();

	void unregisterModules();

	@Nonnull
	Path getModulesDirectory();

	void setModulesDirectory(@Nonnull Path directory);

	@Nonnull
	List<ModuleController> getModules();

	default ModuleController getModule(String name) {
		return getModules().stream().filter(m -> m.getModuleConfig().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	default void removeModule(ModuleController moduleController) {}

}
