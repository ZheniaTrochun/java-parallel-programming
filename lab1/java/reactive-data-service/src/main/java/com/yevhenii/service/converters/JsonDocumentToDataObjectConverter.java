package com.yevhenii.service.converters;

import com.couchbase.client.java.document.JsonDocument;
import com.google.gson.JsonParseException;
import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import com.yevhenii.service.utils.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class JsonDocumentToDataObjectConverter implements Function<JsonDocument, Document<DataObject>> {
    @Override
    public Document<DataObject> apply(JsonDocument document) {
        return JsonUtils.readJson(document.content().toString(), DataObject.class)
                .map(content -> new Document<>(document.id(), content, document.cas()))
                .orElseThrow(() -> new JsonParseException("invalid json!"));
    }
}
