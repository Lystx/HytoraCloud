package cloud.hytora.document;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * JsonEntities are json elements that are stored inside a JsonObject
 * Following types are possible to appear:
 *
 * => Bundle (JsonArray)
 * => Document (JsonObject)
 *
 *
 * @see Bundle
 * @see Document
 *
 * @since DEV-1.0
 * @version STABLE-1.0
 */
public interface JsonEntity {

	/**
	 * @return this entity as a raw string (single lined)
	 */
	@Nonnull
	@CheckReturnValue
	String asRawJsonString();

	/**
	 * @return this entity as a formatted string (multi lined)
	 */
	@Nonnull
	@CheckReturnValue
	String asFormattedJsonString();

}
