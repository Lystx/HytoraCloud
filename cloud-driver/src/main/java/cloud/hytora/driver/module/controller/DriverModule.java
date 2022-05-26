package cloud.hytora.driver.module.controller;

import cloud.hytora.driver.module.Module;
import cloud.hytora.driver.module.ModuleController;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
public abstract class DriverModule implements Module {

	/**
	 * The controller for this module (is being set when being loaded)
	 */
	@Setter
	protected ModuleController controller;

	@Override
	public String toString() {
		return getController().getModuleConfig().getFullName() + " (" + getController().getJarFile().getFileName() + ")";
	}

}
