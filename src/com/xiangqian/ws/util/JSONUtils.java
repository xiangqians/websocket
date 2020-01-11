package com.xiangqian.ws.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xiangqian
 * @date 13:57 2020/01/11
 */
@Slf4j
public class JSONUtils {

    public static void main(String[] args) {
        try {


        } catch (Exception e) {
            log.error("", e);
        }
    }

    public static String toJson(Object obj) {
        try {
            return new Gson().toJson(obj);
        } catch (Exception e) {
            log.warn("", e);
            return null;
        }
    }

    public static <T> T toEntity(String json, TypeToken typeToken) {
        try {
            return new Gson().fromJson(json, typeToken.getType());
        } catch (Exception e) {
            log.warn("", e);
            return null;
        }
    }

    public static <T> T toEntity(String json, Class<T> t) {
        try {
            return new Gson().fromJson(json, t);
        } catch (Exception e) {
            log.warn("", e);
            return null;
        }
    }

    public static JsonObject getAsJsonObject(String json) {
        try {
            JsonElement jsonElement = new JsonParser().parse(json);
            if (jsonElement.isJsonObject()) {
                return jsonElement.getAsJsonObject();
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }
}
