package com.yevhenii.service.services;

import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import com.yevhenii.service.utils.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface SequentialService {
    List<Document<DataObject>> loadDataFromFile() throws IOException;

    List<Document<DataObject>> readPage(int page);
}
