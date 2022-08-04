package cloud.hytora.driver.module.controller.base;


import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;

import javax.annotation.Nonnull;


public enum ModuleCopyType {

	ALL,
	NODE,
	PROXY,
	SERVER,
	SERVER_FALLBACK,

	NONE;

	public boolean applies(@Nonnull SpecificDriverEnvironment type) {
		switch (this) {
			case ALL:       return true;
			case PROXY:     return type == SpecificDriverEnvironment.PROXY;
			case SERVER:    return type == SpecificDriverEnvironment.MINECRAFT;
			case NONE:
			default:        return false;
		}
	}

}
