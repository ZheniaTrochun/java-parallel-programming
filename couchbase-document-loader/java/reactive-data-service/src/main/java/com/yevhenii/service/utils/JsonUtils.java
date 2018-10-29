package com.yevhenii.service.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class JsonUtils {

    private static final Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    public static <T> String toJson(T obj) {
        return gson.toJson(obj);
    }

    public static <T> Optional<T> readJson(String json, Class<T> clazz) {
        return Optional.ofNullable(gson.fromJson(json, clazz));
    }

    public static <T> Optional<T> tryReadJson(String json, Class<T> clazz) {
        try {
            return Optional.ofNullable(gson.fromJson(json, clazz));
        } catch (Throwable e) {
            e.printStackTrace();
            log.warn(json);
            return Optional.empty();
        }
    }
}
