package com.yevhenii.service.services;

import com.google.gson.JsonParseException;
import com.yevhenii.service.configs.AppPropertyHolder;
import com.yevhenii.service.converters.DocumentToDtoConverter;
import com.yevhenii.service.converters.DtoToDocumentConverter;
import com.yevhenii.service.dao.CouchbaseDao;
import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import com.yevhenii.service.models.dto.DataObjectDto;
import com.yevhenii.service.profiling.Profilers;
import com.yevhenii.service.utils.FileUtils;
import com.yevhenii.service.utils.FutureUtils;
import com.yevhenii.service.utils.JsonUtils;
import com.yevhenii.service.utils.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CompletableFutureServiceImpl implements CompletableFutureService {

    private final String DEFAULT_FILE;
    private final Integer PARALLELISM;

    private final CouchbaseDao<DataObject> dao;

    private final DtoToDocumentConverter toDocumentConverter;

    @Autowired
    public CompletableFutureServiceImpl(CouchbaseDao<DataObject> dao,
                                        DtoToDocumentConverter toDocumentConverter,
                                        AppPropertyHolder properties) {
        this.dao = dao;
        this.toDocumentConverter = toDocumentConverter;
        this.DEFAULT_FILE = "C:\\Users\\Yevhenii\\IdeaProjects\\kpi\\java-parallel-programming\\lab-2-new\\lab1\\tools\\data.txt";
//        this.DEFAULT_FILE = properties.getDatafile();
        this.PARALLELISM = 10;
//        this.PARALLELISM = properties.getParallelism();
    }

    @Override
    public CompletableFuture<List<Document<DataObject>>> loadDataFromFile(Optional<String> filename) {
        File file = new File(filename.orElse(DEFAULT_FILE));

        int partSize = (int) file.length() / PARALLELISM;

        List<CompletableFuture<Pair<Integer, String>>> futureParts =
                FutureUtils.iterateParallel(PARALLELISM, (i) -> Pair.of(i, FileUtils.readPart(file, i * partSize, partSize)))
                        .stream()
                        .map(future ->
                                FutureUtils.getPairOrFail(
                                        future,
                                        () -> new IOException("Error reading file: " + file.getName())
                                )
                        )
                        .collect(Collectors.toList());


        return FutureUtils.traverse(futureParts)
                .thenApply(stream ->
                        stream.stream()
                                .sorted(Comparator.comparing(Pair::getLeft))
                                .map(Pair::getRight)
                                .peek(System.out::println)
                                .reduce("", String::concat)
                )
                .thenApply(this::splitIntoParts)
                .thenApply(parts ->
                        parts.stream()
                                .map(this::writePart)
                                .collect(Collectors.toList())
                )
                .thenCompose(FutureUtils::traverse)
                .thenApply(streams ->
                        streams.stream()
                                .map(List::stream)
                                .flatMap(Function.identity()).collect(Collectors.toList()));

    }

    @Override
    public CompletableFuture<List<Document<DataObject>>> readPage(int page) {
        int partSize = dao.getPageSize() / PARALLELISM;

        CompletableFuture<List<List<Document<DataObject>>>> future = FutureUtils.traverse(
                FutureUtils.iterateParallel(PARALLELISM, readPartFromDbProfiled(partSize))
        );

        return future.thenApply(streams -> streams.stream().flatMap(Collection::stream).collect(Collectors.toList()));
    }


    private Function<Integer, List<Document<DataObject>>> readPartFromDbProfiled(int partSize) {
        return Profilers.simpleProfiler(
                        "Reading part of data from db",
                        (i) -> dao.findAll(i * partSize, partSize)
                );
    }

    private Function<Integer, Optional<String>> readPartFromFileProfiled(File file, int partSize) {
        return Profilers.simpleProfiler(
                        "Reading part of data from db",
                        (i) -> FileUtils.readPart(file, i * partSize, partSize)
                );
    }

//    TODO rewrite
    private List<List<String>> splitIntoParts(String str) {
        List<String> splitted = Arrays.asList(str.split("\n"));
        System.out.println(str.chars().filter(c -> c == '\n').count());
        System.out.println(splitted.size());
        int partSize = splitted.size() / PARALLELISM;
        System.out.println(partSize);

        int partitionSize = 1000;
        List<List<String>> partitions = new LinkedList<>();
        for (int i = 0; i < splitted.size(); i += partitionSize) {
            partitions.add(splitted.subList(i,
                    Math.min(i + partitionSize, splitted.size())));
        }

        return partitions;
    }

    private CompletableFuture<List<Document<DataObject>>> writePart(List<String> strings) {
        return CompletableFuture.supplyAsync(() -> strings.stream().map(str -> JsonUtils.readJson(str, DataObjectDto.class)).collect(Collectors.toList()))
                .thenCompose(parsed ->
                        FutureUtils.getFutureListOrFail(parsed, () -> new JsonParseException("Failed to parse JSON")))
                .thenApply(parsed -> parsed.stream().map(toDocumentConverter).map(dao::insert).collect(Collectors.toList()))
                .exceptionally(e -> {
                    System.out.println(e.getMessage());
                    return null;
                });
    }
}