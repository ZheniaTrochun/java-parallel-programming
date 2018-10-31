package com.yevhenii.service.services;

import com.google.gson.JsonParseException;
import com.yevhenii.service.configs.AppPropertyHolder;
import com.yevhenii.service.converters.Converters;
import com.yevhenii.service.dao.CouchbaseDao;
import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import com.yevhenii.service.models.dto.DataObjectDto;
import com.yevhenii.service.profiling.Profilers;
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
import java.util.stream.Stream;

@Slf4j
@Service
@EnableConfigurationProperties(AppPropertyHolder.class)
public class CompletableFutureServiceImpl implements CompletableFutureService {

    private final String DEFAULT_FILE;
    private final Integer PARALLELISM;

    private final CouchbaseDao<DataObject> dao;

    private final Function<DataObjectDto, Document<DataObject>> toDocumentConverter = Converters.dtoToDocumentConverter;

    @Autowired
    public CompletableFutureServiceImpl(CouchbaseDao<DataObject> dao,
                                        AppPropertyHolder properties) {
        this.dao = dao;
        this.DEFAULT_FILE = properties.getDatafile();
        this.PARALLELISM = properties.getParallelism();
    }

    @Override
    public CompletableFuture<List<Document<DataObject>>> loadDataFromFile() {

        return traverse(readFileParallel())
                .thenApply(this::collect)
                .thenApply(Utils::splitByLines)
                .thenApply(lines -> Utils.divideIntoParts(lines, PARALLELISM))
                .thenComposeAsync(this::deserializeAndSave)
                .thenApply(this::flatten);
    }

    @Override
    public CompletableFuture<List<Document<DataObject>>> readPage(int page) {
        int partSize = dao.getPageSize() / PARALLELISM;

        CompletableFuture<List<List<Document<DataObject>>>> future = traverse(
                FutureUtils.iterateParallel(PARALLELISM, readPartFromDbProfiled(partSize))
        );

        return future.thenApply(streams -> streams.stream().flatMap(Collection::stream).collect(Collectors.toList()));
    }


    private Function<Integer, List<Document<DataObject>>> readPartFromDbProfiled(int partSize) {
        return Profilers.simpleProfiler(
                        "Reading part of data from db",
                        i -> dao.findAll(i * partSize, partSize)
                );
    }

    private Function<Integer, Optional<String>> readPartFromFileProfiled(File file, int partSize) {
        return Profilers.simpleProfiler(
                        "Reading part of data from db",
                        i -> FileUtils.readPart(file, i * partSize, partSize)
                );
    }

    private List<CompletableFuture<String>> readFileParallel() {
        File file = new File(DEFAULT_FILE);
        int partSize = (int) Math.ceil((double) file.length() / PARALLELISM);

        return IntStream.range(0, PARALLELISM).boxed()
                .map(i ->
                        CompletableFuture.supplyAsync(() ->
                                FileUtils.readPart(file, i * partSize, partSize)
                                        .orElseThrow(() -> new RuntimeException("Read failed"))
                        )
                )
                .collect(Collectors.toList());
    }

    private <T> CompletableFuture<List<T>> traverse(List<CompletableFuture<T>> futures) {
        return traverseLoop(futures, CompletableFuture.completedFuture(new ArrayList<>()));
    }

    private static <T> CompletableFuture<List<T>> traverseLoop(List<CompletableFuture<T>> futureList,
                                                               CompletableFuture<List<T>> current) {
        Optional<CompletableFuture<T>> head = futureList.stream().findFirst();
        if (!head.isPresent()) {
            return current;
        }
        CompletableFuture<T> future = head.get();
        return traverseLoop(
                futureList.stream().skip(1).collect(Collectors.toList()),
                current.thenComposeAsync(stream -> future.thenApplyAsync(curr -> Stream.concat(stream.stream(), Stream.of(curr)).collect(Collectors.toList())))
        );
    }

//    todo dangerous part
    private String collect(List<String> parts) {
        String str = parts.stream()
                .map(StringBuffer::new)
                .reduce(new StringBuffer(), (left, right) -> left.append(right))
                .toString();

        return str;
    }

    private <T> List<T> flatten(List<List<T>> lists) {
        return lists.stream()
                .map(List::stream)
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

//    TODO rewrite
    private List<List<String>> splitIntoParts(String str) {

        List<String> splitted = Arrays.asList(str.split("\n"));
        System.out.println(str.chars().filter(c -> c == '\n').count());
        System.out.println(splitted.size());
        int partSize = splitted.size() / PARALLELISM;
        System.out.println(partSize);
        System.out.println("last: " + splitted.get(splitted.size() - 1));

        List<List<String>> partitions = new LinkedList<>();

        for (int i = 0; i < splitted.size(); i += partSize) {
            partitions.add(splitted.subList(i, Math.min(i + partSize, splitted.size())));
        }

        return partitions;
    }

    private CompletableFuture<List<List<Document<DataObject>>>> deserializeAndSave(List<List<String>> jsons) {
        log.info("Deserialize and save: " + jsons.size());

        return traverse(
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
                    System.out.println(e.getMessage());
                    return new ArrayList<>();
                });
    }

    private List<Document<DataObject>> convertAndSave(List<DataObjectDto> objects) {
        return objects.stream()
                .map(toDocumentConverter)
//                .map(dao::insert)
                .collect(Collectors.toList());
    }

    private List<Optional<DataObjectDto>> deserialize(List<String> jsons) {
        log.info("Deserialize: " + jsons.size());
        return jsons.stream()
                .map(str -> JsonUtils.readJson(str, DataObjectDto.class))
                .collect(Collectors.toList());
    }
}
