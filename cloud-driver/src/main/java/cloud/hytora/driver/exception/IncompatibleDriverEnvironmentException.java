package cloud.hytora.driver.exception;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;

public class IncompatibleDriverEnvironmentException extends CloudException {

    public IncompatibleDriverEnvironmentException(DriverEnvironment rightEnvironment) {
        super("This action can't be performed on Environment " + CloudDriver.getInstance().getEnvironment() + " but on " + rightEnvironment + ".");
    }

    public static void throwIfNeeded(DriverEnvironment rightEnvironment) {
        if (CloudDriver.getInstance().getEnvironment() != rightEnvironment) {
            throw new IncompatibleDriverEnvironmentException(rightEnvironment);
        }
    }
}
