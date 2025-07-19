
package cloud.hytora.simplejson.api.enums;

public enum JsonFormat {

    /**
     * Should never be used
     */
    UNKNOWN,

    /**
     * This is the raw json format (e.g. {"key": "value"} )
     */
    RAW,

    /**
     * This is the formatted json format (e.g.
     * {
     *    "key" : "value"
     * } )
     */
    FORMATTED,

    /**
     * This is the simple json format (e.g.
     *
     * {
     *     key : value
     * } )
     */
    SIMPLE;


}

