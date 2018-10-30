package com.yevhenii.service.converters;

import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import com.yevhenii.service.models.dto.DataObjectDto;

import java.util.function.Function;

public class Converters {

    public static final Function<Document<DataObject>, DataObjectDto> documentToDtoConverter = (doc) ->
            DataObjectDto.builder()
                    .id(new Long(doc.getId()))
                    .name(doc.getContent().getName())
                    .age(doc.getContent().getAge())
                    .randomString(doc.getContent().getSecretStr())
                    .randomNumber(doc.getContent().getSecretNum())
                    .build();
}
