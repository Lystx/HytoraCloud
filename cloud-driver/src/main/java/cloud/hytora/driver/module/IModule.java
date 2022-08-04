package cloud.hytora.driver.module;

import cloud.hytora.driver.module.controller.ModuleClassLoader;

import javax.annotation.Nonnull;


public interface IModule {

	@Nonnull
	ModuleController getController();

	@Nonnull
	static IModule get(@Nonnull Class<?> clazz) {
		ClassLoader loader = clazz.getClassLoader();
		if (!(loader instanceof ModuleClassLoader)) {
			throw new IllegalStateException(clazz.getName() + " was not loaded by a module (" + loader.getClass().getName() + ")");
		}
		ModuleClassLoader moduleLoader = (ModuleClassLoader) loader;
		return moduleLoader.getModule();
	}

}
