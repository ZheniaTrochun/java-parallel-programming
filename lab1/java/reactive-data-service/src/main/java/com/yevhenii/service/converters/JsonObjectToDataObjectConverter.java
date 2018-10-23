package com.yevhenii.service.converters;


import com.couchbase.client.java.document.json.JsonObject;
import com.google.gson.JsonParseException;
import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import com.yevhenii.service.utils.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class JsonObjectToDataObjectConverter implements Function<JsonObject, Document<DataObject>> {
    @Override
    public Document<DataObject> apply(JsonObject object) {
        return JsonUtils.readJson(object.toString(), DataObject.class)
                .map(content ->
                        new Document<>(
                                object.getString("id"),
                                content,
                                object.getLong("cas")
                        )
                )
                .orElseThrow(() -> new JsonParseException("invalid json!"));
    }
}
