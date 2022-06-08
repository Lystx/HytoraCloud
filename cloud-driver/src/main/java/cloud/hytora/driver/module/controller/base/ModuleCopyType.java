package cloud.hytora.driver.module.controller.base;


import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;

import javax.annotation.Nonnull;


public enum ModuleCopyType {

	ALL,
	NODE,
	PROXY,
	SERVER,
	NONE;

	public boolean applies(@Nonnull SpecificDriverEnvironment type) {
		switch (this) {
			case ALL:       return true;
			case PROXY:     return type == SpecificDriverEnvironment.PROXY_SERVER;
			case SERVER:    return type == SpecificDriverEnvironment.MINECRAFT_SERVER;
			case NONE:
			default:        return false;
		}
	}

}
