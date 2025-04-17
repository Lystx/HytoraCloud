package cloud.hytora.driver.database.api.action.hierarchy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface OrderedAction {

	@Nullable
	OrderedAction orderBy(@Nonnull String field, @Nonnull Order order);

	enum Order {

		HIGHEST,
		LOWEST

	}
}
