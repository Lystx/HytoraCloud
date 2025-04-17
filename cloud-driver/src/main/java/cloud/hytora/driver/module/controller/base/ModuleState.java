package cloud.hytora.driver.module.controller.base;

import cloud.hytora.driver.module.ModuleController;


public enum ModuleState {

	/**
	 * @see ModuleController#loadModule()
	 */
	LOADED,

	/**
	 * @see ModuleController#enableModule()
	 */
	ENABLED,

	/**
	 * @see ModuleController#reloadModule() ()
	 */
	RELOADING,

	/**
	 * @see ModuleController#disableModule()
	 */
	DISABLED,

	/**
	 * @see ModuleController#unregisterModule()
	 */
	UNREGISTERED;


}
