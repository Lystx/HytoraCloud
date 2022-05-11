package cloud.hytora.document;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.function.Supplier;

public interface JsonEntity {

	@Nonnull
	@CheckReturnValue
	String asRawJsonString();

	@Nonnull
	@CheckReturnValue
	String asFormattedJsonString();

}
