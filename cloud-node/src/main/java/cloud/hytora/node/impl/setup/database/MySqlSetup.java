package cloud.hytora.node.impl.setup.database;

import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.setup.Setup;
import cloud.hytora.driver.setup.SetupHeaderBehaviour;
import cloud.hytora.driver.setup.annotations.Question;
import lombok.Getter;

@Getter
public class MySqlSetup extends Setup<MySqlSetup> {

    @Question(id = 1, question = "What's the host of your database?")
    private String databaseHost;

    @Question(id = 2, question = "What's the port of your database?")
    private int databasePort;

    @Question(id = 3, question = "What's the username of your database?")
    private String databaseUser;

    @Question(id = 4, question = "What's the password of your database?")
    private String databasePassword;

    @Question(id = 5, question = "What's the name of your database?")
    private String databaseName;


    @Override
    public boolean isCancellable() {
        return true;
    }

    @Override
    public boolean shouldPrintHeader() {
        return true;
    }

    @Override
    public SetupHeaderBehaviour headerBehaviour() {
        return SetupHeaderBehaviour.RESTORE_PREVIOUS_LINES;
    }


    public static class Checker implements BiSupplier<String, Boolean> {

        @Override
        public Boolean supply(String name) {
            return CloudDriver.getInstance().getConfigurationManager().getConfigurationByName(name).isPresent();
        }
    }
}
