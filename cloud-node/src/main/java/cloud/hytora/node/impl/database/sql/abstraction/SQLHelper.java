package cloud.hytora.node.impl.database.sql.abstraction;

import cloud.hytora.common.misc.GsonUtils;
import cloud.hytora.document.JsonEntity;
import cloud.hytora.document.gson.GsonDocument;
import cloud.hytora.document.gson.GsonHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public final class SQLHelper {

	private SQLHelper() {}

	public static void fillParams(@Nonnull PreparedStatement statement, @Nonnull Object... params) throws SQLException {
		for (int i = 0; i < params.length; i++) {
			Object param = serializeObject(params[i]);
			statement.setObject(i + 1 /* in sql we count from 1 */, param);
		}
	}

	@Nullable
	public static Object serializeObject(@Nullable Object object) {
		if (object == null)                 return null;
		if (object instanceof Number)       return object;
		if (object instanceof Boolean)      return object;
		if (object instanceof Enum<?>)      return ((Enum<?>)object).name();
		if (object instanceof JsonEntity)         return ((JsonEntity)object).asRawJsonString();
		if (object instanceof Map)          return new GsonDocument((Map<String, Object>) object).asRawJsonString();
		if (object instanceof Iterable)     return GsonUtils.convertIterableToJsonArray(GsonHelper.DEFAULT_GSON, (Iterable<?>) object).toString();
		if (object.getClass().isArray())    return GsonUtils.convertArrayToJsonArray(GsonHelper.DEFAULT_GSON, object).toString();
		return object.toString();
	}

}
