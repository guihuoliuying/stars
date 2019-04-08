package com.stars.network.server.handler;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/1/10.
 */
public class GmJsonDeserializer implements JsonDeserializer<HashMap<String, Object>> {
    @Override
    public HashMap<String, Object> deserialize(JsonElement jelem, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        HashMap<String, Object> map = new HashMap<>();
        JsonObject jobject = jelem.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : jobject.entrySet()) {
            if (entry.getValue().isJsonPrimitive()) {
                map.put(entry.getKey(), entry.getValue().getAsString());
            } else if (entry.getValue().isJsonObject()) {
                map.put(entry.getKey(), deserialize(entry.getValue(), type, ctx));
            } else if (entry.getValue().isJsonArray()) {
                map.put(entry.getKey(), deserializeArray(entry.getValue(), type, ctx));
            }
        }
        return map;
    }
    private List<Object> deserializeArray(JsonElement jelem, Type type, JsonDeserializationContext ctx) {
        List<Object> list = new ArrayList<Object>();
        for (JsonElement jitem : jelem.getAsJsonArray()) {
            if (jitem.isJsonPrimitive()) {
                list.add(jitem.getAsString());
            } else if (jitem.isJsonObject()) {
                list.add(deserialize(jitem, type, ctx));
            } else if (jitem.isJsonArray()) {
                list.add(deserializeArray(jitem, type, ctx));
            }
        }
        return list;
    }
}
