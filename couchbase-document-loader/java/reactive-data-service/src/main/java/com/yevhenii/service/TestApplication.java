package com.yevhenii.service;

import com.google.gson.JsonParseException;
import com.yevhenii.service.models.dto.DataObjectDto;
import com.yevhenii.service.utils.FileUtils;
import com.yevhenii.service.utils.FutureUtils;
import com.yevhenii.service.utils.JsonUtils;
import com.yevhenii.service.utils.RxUtils;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class TestApplication {

    public static void main(String[] args) throws InterruptedException {
        File file = new File("C:\\Users\\Yevhenii\\IdeaProjects\\kpi\\java-parallel-programming\\lab-2-new\\lab1\\tools\\data1.txt");

        int PARALLELISM = 2;

        int partSize = (int) file.length() / PARALLELISM;

        Maybe<String> string =
                RxUtils.parallel(PARALLELISM, (i) -> FileUtils.readPart(file, i * partSize, partSize))
                        .map(Optional::get)
                        .reduce(String::concat)
                        .map(s -> {log.debug("file:\n" + s + "\nEOF"); return s;});

        final Flowable<DataObjectDto> dataObjectDtoFlowable = string.toFlowable().flatMap(str ->
                RxUtils.parallel(
                        Arrays.stream(str.split("\n"))
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList()),
                        PARALLELISM,
                        (element) -> Optional.of(element)
                                .map(e -> {log.debug("part: " + e); return e;})
                                .flatMap(elem -> JsonUtils.readJson(elem, DataObjectDto.class))
                                .orElseThrow(() -> new JsonParseException("invalid json!"))
                ).toFlowable(BackpressureStrategy.BUFFER)
        );

//        log.debug(string.blockingGet());

        final Flowable<DataObjectDto> dataObjectDtoFlowable1 = dataObjectDtoFlowable.doOnError(s -> log.debug(s.toString())).doOnNext(s -> log.debug(s.toString()));
        final Disposable subscribe = dataObjectDtoFlowable1.subscribe(s -> log.debug(s.toString()));
//        System.out.println("finish");
//        System.out.println(subscribe);
//        Disposable d = dataObjectDtoFlowable.subscribe(System.out::println);

        Thread.sleep(5000);
    }
}
