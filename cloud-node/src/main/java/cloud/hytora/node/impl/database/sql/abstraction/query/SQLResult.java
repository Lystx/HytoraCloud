package cloud.hytora.node.impl.database.sql.abstraction.query;

import cloud.hytora.document.Document;
import cloud.hytora.document.empty.EmptyDocument;
import cloud.hytora.document.gson.GsonDocument;
import cloud.hytora.document.map.MapDocument;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SQLResult extends MapDocument {

	public SQLResult(@Nonnull Map<Object, Object> values) {
		super(values,  new AtomicBoolean(false));
	}

	@Nonnull
	@Override
	public Document getDocument(@Nonnull String path) {
		try {
			return new GsonDocument(getString(path));
		} catch (Exception ex) {
			return new EmptyDocument();
		}
	}

	@Override
	public boolean canEdit() {
		return false;
	}

}
