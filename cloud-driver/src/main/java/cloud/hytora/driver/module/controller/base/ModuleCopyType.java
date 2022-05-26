package cloud.hytora.driver.module.controller.base;


import javax.annotation.Nonnull;


public enum ModuleCopyType {

	ALL,
	NODE,
	PROXY,
	SERVER,
	NONE;

	public boolean applies(@Nonnull String type) {
		switch (this) {
			case ALL:       return true;
			case PROXY:     return type.equalsIgnoreCase("PROXY");
			case SERVER:    return type.equalsIgnoreCase("SERVER");
			case NONE:
			default:        return false;
		}
	}

}
