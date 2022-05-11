package cloud.hytora.driver.exception;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;

public class IncompatibleDriverEnvironment extends CloudException {

    public IncompatibleDriverEnvironment(DriverEnvironment rightEnvironment) {
        super("This action can't be performed on Environment " + CloudDriver.getInstance().getEnvironment() + " but on " + rightEnvironment + ".");
    }

    public static void throwIfNeeded(DriverEnvironment rightEnvironment) {
        if (CloudDriver.getInstance().getEnvironment() != rightEnvironment) {
            throw new IncompatibleDriverEnvironment(rightEnvironment);
        }
    }
}
