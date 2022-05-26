package cloud.hytora.driver.module.controller.base;


import cloud.hytora.driver.DriverEnvironment;

import javax.annotation.Nonnull;
import java.util.function.Predicate;


public enum ModuleEnvironment {

	ALL(environment -> true),
	NODE(environment -> environment == DriverEnvironment.NODE),
	SERVICE(environment -> environment == DriverEnvironment.SERVICE),
	NONE(environment -> false);

	private final Predicate<DriverEnvironment> filter;

	ModuleEnvironment(@Nonnull Predicate<DriverEnvironment> filter) {
		this.filter = filter;
	}


	public boolean applies(@Nonnull DriverEnvironment environment) {
		return filter.test(environment);
	}

}
