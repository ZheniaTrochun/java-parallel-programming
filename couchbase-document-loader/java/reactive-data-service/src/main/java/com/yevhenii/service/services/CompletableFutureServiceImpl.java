package com.yevhenii.service.services;

import com.google.gson.JsonParseException;
import com.yevhenii.service.configs.AppPropertyHolder;
import com.yevhenii.service.converters.Converters;
import com.yevhenii.service.dao.CouchbaseDao;
import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import com.yevhenii.service.models.dto.DataObjectDto;
import com.yevhenii.service.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Slf4j
@Service
@EnableConfigurationProperties(AppPropertyHolder.class)
public class CompletableFutureServiceImpl implements CompletableFutureService {

    private final String DEFAULT_FILE;
    private final Integer PARALLELISM;

    private final CouchbaseDao<DataObject> dao;

    @Autowired
    public CompletableFutureServiceImpl(CouchbaseDao<DataObject> dao, AppPropertyHolder properties) {
        this.dao = dao;
        this.DEFAULT_FILE = properties.getDatafile();
        this.PARALLELISM = properties.getParallelism();
    }

    @Override
    public CompletableFuture<List<Document<DataObject>>> loadDataFromFile() {

        return FutureUtils.traverse(readFileParallel())
                .thenApply(this::collect)
                .thenApply(Utils::splitByLines)
                .thenApply(lines -> Utils.divideIntoParts(lines, PARALLELISM))
                .thenComposeAsync(this::deserializeAndSave)
                .thenApply(this::flatten);
    }

    @Override
    public CompletableFuture<List<Document<DataObject>>> readPage(int page) {
        int pageSize = dao.getPageSize();
        int initialOffset = pageSize * (page - 1);
        int partSize = dao.getPageSize() / PARALLELISM;

        CompletableFuture<List<List<Document<DataObject>>>> future = FutureUtils.traverse(
                FutureUtils.iterateParallel(
                        IntStream.range(0, PARALLELISM),
                        i -> dao.findAll(initialOffset + i * partSize, partSize)
                )
        );

        return future.thenApply(streams -> streams.stream().flatMap(Collection::stream).collect(Collectors.toList()));
    }

    private List<CompletableFuture<String>> readFileParallel() {
        File file = new File(DEFAULT_FILE);
        int partSize = (int) Math.ceil((double) file.length() / PARALLELISM);

        return FutureUtils.iterateParallel(
                IntStream.range(0, PARALLELISM),
                i -> FileUtils.readPart(file, i * partSize, partSize)
                        .orElseThrow(() -> new RuntimeException("Read failed"))
        );
    }

    private String collect(List<String> parts) {
        return parts.stream()
                .map(StringBuffer::new)
                .reduce(new StringBuffer(), StringBuffer::append)
                .toString();
    }

    private <T> List<T> flatten(List<List<T>> lists) {
        return lists.stream()
                .map(List::stream)
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }


    private CompletableFuture<List<List<Document<DataObject>>>> deserializeAndSave(List<List<String>> jsons) {
        return FutureUtils.traverse(
                jsons.stream()
                        .map(this::writePart)
                        .collect(Collectors.toList())
        );
    }

    private CompletableFuture<List<Document<DataObject>>> writePart(List<String> jsons) {

        return CompletableFuture.supplyAsync(() -> deserialize(jsons))
                .thenCompose(parsed ->
                        FutureUtils.getFutureListOrFail(parsed, () -> new JsonParseException("Failed to parse JSON")))
                .thenApply(this::convertAndSave)
                .exceptionally(e -> {
                    log.error(e.getMessage(), e);
                    return new ArrayList<>();
                });
    }

    private List<Document<DataObject>> convertAndSave(List<DataObjectDto> objects) {
        return objects.stream()
                .map(Converters::dtoToDocumentConverter)
                .map(dao::insert)
                .collect(Collectors.toList());
    }

    private List<Optional<DataObjectDto>> deserialize(List<String> jsons) {
        return jsons.stream()
                .map(str -> JsonUtils.readJson(str, DataObjectDto.class))
                .collect(Collectors.toList());
    }
}
