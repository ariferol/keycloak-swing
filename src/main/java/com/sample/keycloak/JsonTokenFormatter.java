package com.sample.keycloak;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

/** Formats token JSON response into a map of key->formatted value. */
public class JsonTokenFormatter {
    private static final Gson GSON = new Gson();

    public static Map<String, String> parse(String json) {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            JsonElement root = GSON.fromJson(json, JsonElement.class);
            if (root != null && root.isJsonObject()) {
                JsonObject obj = root.getAsJsonObject();
                for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
                    String key = e.getKey();
                    String value = e.getValue().isJsonPrimitive() ? e.getValue().getAsString() : e.getValue().toString();
                    result.put(key, value);
                }
            }
        } catch (Exception ex) {
            result.put("raw", json);
        }
        return result;
    }
}
