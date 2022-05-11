package cloud.hytora.driver.event;


public enum EventOrder {

	/** Executed last, after {@link #LATE} */
	LAST,
	/** Executed after {@link #NORMAL} */
	LATE,
	/** Executed after {@link #EARLY}, default value */
	NORMAL,
	/** Executed after {@link #FIRST} */
	EARLY,
	/** Executed first */
	FIRST,

}
