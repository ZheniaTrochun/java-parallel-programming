package com.yevhenii.service.services;

import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface CompletableFutureService {

    CompletableFuture<List<Document<DataObject>>> loadDataFromFile(Optional<String> filename);

    CompletableFuture<List<Document<DataObject>>> readPage(int page);
}
