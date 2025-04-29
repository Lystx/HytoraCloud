package cloud.hytora.driver;

public enum PublishingType {


    INTERNAL,

    PROTOCOL,

    GLOBAL;


    public static PublishingType get(PublishingType[] types) {
        if (types == null || types.length == 0) {
            return GLOBAL;
        }
        return types[0];
    }
    public static PublishingType get(PublishingType[] types, PublishingType defValue) {
        if (types == null || types.length == 0) {
            return defValue;
        }
        return types[0];
    }
}
