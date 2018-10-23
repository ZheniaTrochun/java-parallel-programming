package com.yevhenii.service.converters;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import com.yevhenii.service.utils.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class DataObjectToJsonDocumentConverter implements Function<Document<DataObject>, JsonDocument> {
    @Override
    public JsonDocument apply(Document<DataObject> document) {
        return JsonDocument.create(
                document.getId(),
                JsonObject.fromJson(JsonUtils.toJson(document.getContent())),
                document.getVersion()
        );
    }
}
