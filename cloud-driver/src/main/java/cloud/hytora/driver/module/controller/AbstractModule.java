package cloud.hytora.driver.module.controller;

import cloud.hytora.http.api.HttpServer;
import cloud.hytora.driver.module.IModule;
import cloud.hytora.driver.module.ModuleController;
import lombok.Getter;
import lombok.Setter;

@Getter
public class AbstractModule implements IModule {

	/**
	 * The controller for this module (is being set when being loaded)
	 */
	@Setter
	protected ModuleController controller;

	/**
	 * The instance that is being set from Node loader
	 */
	@Setter
	protected HttpServer httpServer;

	public void registerTasks(Object moduleTasksClassInstance) {
		controller.registerModuleTasks(moduleTasksClassInstance);
	}

	@Override
	public String toString() {
		return getController().getModuleConfig().getFullName() + " (" + getController().getJarFile().getFileName() + ")";
	}

}
