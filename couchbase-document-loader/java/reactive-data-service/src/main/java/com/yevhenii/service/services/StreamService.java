package com.yevhenii.service.services;

import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;

import java.io.IOException;
import java.util.List;

public interface StreamService {

    List<Document<DataObject>> upload() throws IOException;

    List<Document<DataObject>> download(int page);
}
