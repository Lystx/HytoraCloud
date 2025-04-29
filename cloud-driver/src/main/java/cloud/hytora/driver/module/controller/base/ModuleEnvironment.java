package cloud.hytora.driver.module.controller.base;


import cloud.hytora.driver.CloudDriver;

import javax.annotation.Nonnull;
import java.util.function.Predicate;


public enum ModuleEnvironment {

	ALL(environment -> true),
	NODE(environment -> environment == CloudDriver.Environment.NODE),
	SERVICE(environment -> environment == CloudDriver.Environment.SERVICE),
	NONE(environment -> false);

	private final Predicate<CloudDriver.Environment> filter;

	ModuleEnvironment(@Nonnull Predicate<CloudDriver.Environment> filter) {
		this.filter = filter;
	}


	public boolean applies(@Nonnull CloudDriver.Environment environment) {
		return filter.test(environment);
	}

}
