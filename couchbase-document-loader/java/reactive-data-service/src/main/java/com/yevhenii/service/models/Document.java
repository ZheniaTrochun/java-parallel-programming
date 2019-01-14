package com.yevhenii.service.models;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.google.gson.JsonParseException;
import com.yevhenii.service.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class Document<T> {
    private String id;
    private T content;
    private Long version;

    public Document(String id, T content, long version) {
        this.id = id;
        this.content = content;
        this.version = version;
    }

    public JsonDocument toJsonDocument() {
        return JsonDocument.create(
                id,
                JsonObject.fromJson(JsonUtils.toJson(content)),
                version
        );
    }

    public static <T> Document<T> read(JsonDocument doc, Class<T> clazz) {
        return JsonUtils.readJson(doc.content().toString(), clazz)
                .map(content -> new Document<>(doc.id(), content, doc.cas()))
                .orElseThrow(() -> new JsonParseException(String.format("invalid json! [%s]", doc.toString())));
    }

    public static <T> Document<T> read(JsonObject object, Class<T> clazz) {
        return JsonUtils.readJson(object.toString(), clazz)
                .map(content ->
                        new Document<>(
                                object.getString("id"),
                                content,
                                object.getLong("cas")
                        )
                )
                .orElseThrow(() -> new JsonParseException(String.format("invalid json! [%s]", object.toString())));
    }
}
