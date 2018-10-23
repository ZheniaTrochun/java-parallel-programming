package com.yevhenii.service.converters;

import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import com.yevhenii.service.models.dto.DataObjectDto;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class DocumentToDtoConverter implements Function<Document<DataObject>, DataObjectDto> {
    @Override
    public DataObjectDto apply(Document<DataObject> doc) {
        return DataObjectDto.builder()
                .id(new Long(doc.getId()))
                .name(doc.getContent().getName())
                .age(doc.getContent().getAge())
                .randomString(doc.getContent().getSecretStr())
                .randomNumber(doc.getContent().getSecretNum())
                .build();
    }
}
