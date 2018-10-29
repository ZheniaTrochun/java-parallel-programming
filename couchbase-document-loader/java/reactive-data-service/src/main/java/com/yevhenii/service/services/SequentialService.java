package com.yevhenii.service.services;

import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import com.yevhenii.service.utils.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface SequentialService {
    List<Document<DataObject>> loadDataFromFile(Optional<String> file) throws IOException;

    List<Document<DataObject>> readPage(int page);

    Pair<Long, List<Document<DataObject>>> loadDataFromFileProfiled(Optional<String> filename) throws IOException;

    Pair<Long, List<Document<DataObject>>> readPageProfiled(int page);
}
