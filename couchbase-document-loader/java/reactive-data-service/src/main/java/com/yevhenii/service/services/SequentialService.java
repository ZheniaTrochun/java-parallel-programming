package com.yevhenii.service.services;

import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;

import java.io.IOException;
import java.util.List;


public interface SequentialService {
    List<Document<DataObject>> loadDataFromFile() throws IOException;

    List<Document<DataObject>> readPage(int page);
}
