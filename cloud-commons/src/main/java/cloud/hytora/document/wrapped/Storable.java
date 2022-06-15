package cloud.hytora.document.wrapped;

import cloud.hytora.common.task.Task;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;

/**

 *
 * @see StorableDocument
 * @see StorableBundle
 */
public interface Storable {

	@Nonnull
	Path getPath();

	@Nonnull
	File getFile();

	default void save() {
		try {
			saveExceptionally();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void saveExceptionally() throws Exception;

	@Nonnull
	default Task<Void> saveAsync() {
		return Task.runAsyncExceptionally(this::saveExceptionally);
	}

}
