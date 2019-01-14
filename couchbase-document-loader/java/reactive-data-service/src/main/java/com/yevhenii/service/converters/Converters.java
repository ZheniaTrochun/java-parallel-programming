package com.yevhenii.service.converters;

import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import com.yevhenii.service.models.dto.DataObjectDto;

import java.util.function.Function;

public class Converters {

    public static DataObjectDto documentToDtoConverter(Document<DataObject> doc) {
        return DataObjectDto.builder()
                .id(new Long(doc.getId()))
                .name(doc.getContent().getName())
                .age(doc.getContent().getAge())
                .randomString(doc.getContent().getSecretStr())
                .randomNumber(doc.getContent().getSecretNum())
                .build();
    }

    public static Document<DataObject> dtoToDocumentConverter(DataObjectDto dto) {
        return new Document<>(
                dto.getId().toString(),
                new DataObject(dto.getName(), dto.getAge(), dto.getRandomString(), dto.getRandomNumber()),
                1L
        );
    }
}
