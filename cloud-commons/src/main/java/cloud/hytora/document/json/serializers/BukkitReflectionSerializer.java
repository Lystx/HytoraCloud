package cloud.hytora.document.json.serializers;

import cloud.hytora.common.misc.BukkitReflectionSerializationUtils;
import cloud.hytora.document.json.JsonHelper;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;
import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.elements.JsonLiteral;
import cloud.hytora.simplejson.elements.object.JsonObject;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;


public class BukkitReflectionSerializer extends JsonSerializer<Object> {

	public static final String ALTERNATE_KEY = "classOfType", KEY = "==";

	private JsonEntity findClassContainer(@Nonnull JsonObject json) {
		if (json.has(ALTERNATE_KEY))
			return json.get(ALTERNATE_KEY);
		return json.get(KEY);
	}

	@Override
	public Object deserialize(JsonEntity element, Field field, Json jsonInstance, Class<?>... arguments) {

		if (element == null || !element.isJsonObject()) return null;

		JsonObject json = element.asJsonObject();
		String classOfType = Optional.ofNullable(findClassContainer(json)).filter(JsonEntity::isPrimitive).map(JsonEntity::asString).orElse(null);

		Class<?> clazz = null;
		try {
			clazz = Class.forName(classOfType);
		} catch (ClassNotFoundException | NullPointerException ex) {
		}

		Map<String, Object> map = JsonHelper.convertJsonObjectToMap(json);
		return BukkitReflectionSerializationUtils.deserializeObject(map, clazz);

	}

	@Override
	public JsonEntity serialize(Object obj, Json jsonInstance, Field field) {
		Map<String, Object> map = BukkitReflectionSerializationUtils.serializeObject(obj);
		if (map == null) return JsonLiteral.NULL;

		JsonObject json = new JsonObject();
		json.addProperty(KEY, BukkitReflectionSerializationUtils.getSerializationName(obj.getClass()));
		JsonHelper.setDocumentProperties(jsonInstance, json, map);
		return json;
	}
}
