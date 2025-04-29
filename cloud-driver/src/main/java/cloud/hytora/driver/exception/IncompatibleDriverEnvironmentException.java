package cloud.hytora.driver.exception;

import cloud.hytora.driver.CloudDriver;

public class IncompatibleDriverEnvironmentException extends CloudException {

    public IncompatibleDriverEnvironmentException(CloudDriver.Environment rightEnvironment) {
        super("This action can't be performed on Environment " + CloudDriver.getInstance().getEnvironment() + " but on " + rightEnvironment + ".");
    }

    public static void throwIfNeeded(CloudDriver.Environment rightEnvironment) {
        if (CloudDriver.getInstance().getEnvironment() != rightEnvironment) {
            throw new IncompatibleDriverEnvironmentException(rightEnvironment);
        }
    }
}
