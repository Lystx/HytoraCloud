package cloud.hytora.document.json.serializers;

import cloud.hytora.document.Bundle;
import cloud.hytora.document.json.JsonBundle;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;
import cloud.hytora.simplejson.elements.JsonEntity;

import java.lang.reflect.Field;


public class BundleSerializer extends JsonSerializer<Bundle> {


	@Override
	public Bundle deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {
		if (element != null && element.isArray()) {
			return new JsonBundle(element.asJsonArray());
		} else {
			return null;
		}
	}

	@Override
	public JsonEntity serialize(Bundle obj, Json json, Field field) {
		if (obj instanceof JsonBundle) {
			JsonBundle jsonBundle = (JsonBundle) obj;
			return jsonBundle.getJsonArray();
		}

		JsonBundle jsonBundle = new JsonBundle(obj.toList());
		return jsonBundle.getJsonArray();
	}
}
