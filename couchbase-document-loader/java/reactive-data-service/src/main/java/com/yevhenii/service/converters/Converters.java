package com.yevhenii.service.converters;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import com.yevhenii.service.models.dto.DataObjectDto;
import com.yevhenii.service.utils.JsonUtils;

import java.util.function.Function;

public class Converters {

    public static final Function<Document<DataObject>, DataObjectDto> documentToDtoConverter = doc ->
            DataObjectDto.builder()
                    .id(new Long(doc.getId()))
                    .name(doc.getContent().getName())
                    .age(doc.getContent().getAge())
                    .randomString(doc.getContent().getSecretStr())
                    .randomNumber(doc.getContent().getSecretNum())
                    .build();

    public static final Function<Document<DataObject>, JsonDocument> toJsonDocumentConverter = doc ->
            JsonDocument.create(
                    doc.getId(),
                    JsonObject.fromJson(JsonUtils.toJson(doc.getContent())),
                    doc.getVersion()
            );
}
