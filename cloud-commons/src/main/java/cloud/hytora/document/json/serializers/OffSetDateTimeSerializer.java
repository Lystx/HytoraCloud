package cloud.hytora.document.json.serializers;

import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;
import cloud.hytora.simplejson.elements.JsonEntity;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;


public class OffSetDateTimeSerializer extends JsonSerializer<OffsetDateTime> {


	@Override
	public OffsetDateTime deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {
		try {
			return OffsetDateTime.parse(element.asString());
		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public JsonEntity serialize(OffsetDateTime obj, Json json, Field field) {
		return JsonEntity.valueOf(obj.toString());
	}
}
