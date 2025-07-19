package cloud.hytora.driver.networking.query;

public enum QueryState {

    SUCCESS,

    FAILED,

    ERROR;


    public static QueryState fromBoolean(boolean b) {
        return (b ? SUCCESS : FAILED);
    }


    public boolean toBoolean() {
        return this == SUCCESS;
    }
}
