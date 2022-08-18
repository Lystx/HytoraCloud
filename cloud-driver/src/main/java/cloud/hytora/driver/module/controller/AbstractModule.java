package cloud.hytora.driver.module.controller;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.http.api.HttpServer;
import cloud.hytora.driver.module.IModule;
import cloud.hytora.driver.module.ModuleController;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

	public void registerEvent(Object eventClassInstance) {
		CloudDriver.getInstance().getEventManager().registerListener(eventClassInstance);
	}

	public void registerCommand(Object commandClassInstance) {
		CloudDriver.getInstance().getCommandManager().registerCommand(commandClassInstance);
	}

	@Override
	public String toString() {
		return getController().getModuleConfig().getFullName() + " (" + getController().getJarFile().getFileName() + ")";
	}

}
