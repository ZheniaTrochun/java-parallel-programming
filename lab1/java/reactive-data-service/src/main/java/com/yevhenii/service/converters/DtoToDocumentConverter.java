package com.yevhenii.service.converters;

import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import com.yevhenii.service.models.dto.DataObjectDto;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class DtoToDocumentConverter implements Function<DataObjectDto, Document<DataObject>> {
    @Override
    public Document<DataObject> apply(DataObjectDto dto) {
        return new Document<>(
                dto.getId().toString(),
                new DataObject(dto.getName(), dto.getAge(), dto.getRandomString(), dto.getRandomNumber()),
                1L
        );
    }
}
