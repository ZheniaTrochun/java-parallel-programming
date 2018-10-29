package com.yevhenii.service.controllers;

import com.yevhenii.service.converters.DocumentToDtoConverter;
import com.yevhenii.service.models.dto.DataObjectDto;
import com.yevhenii.service.profiling.Profilers;
import com.yevhenii.service.profiling.ProfilingResult;
import com.yevhenii.service.profiling.SimpleProfilingResult;
import com.yevhenii.service.services.CompletableFutureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@RestController
public class CompletableFutureControllerImpl implements CompletableFutureController {

    private final CompletableFutureService service;
    private final DocumentToDtoConverter converter;

    @Autowired
    public CompletableFutureControllerImpl(CompletableFutureService service,
                                           DocumentToDtoConverter converter) {
        this.service = service;
        this.converter = converter;
    }

    @Override
    @RequestMapping(path = "/data/upload/completablefuture")
    public ResponseEntity<Integer> load() throws ExecutionException, InterruptedException {

        CompletableFuture<Integer> future = new CompletableFuture<>();

        service.loadDataFromFile(Optional.empty())
                .thenApply(list -> list.stream().peek(System.out::println).collect(Collectors.toList()))
                .thenApply(List::size).whenComplete((count, err) -> {
            System.out.println("count = " + count);
            System.out.println("error = " + err);
            future.complete(count);
        });


//        System.out.println("result = " + res);
        System.out.println("test123123123");
        return ResponseEntity.ok(
                        future.get()
                );
    }

//    TODO rewrite cardinally
    @Override
    @Async
    @RequestMapping(path = "/data/upload/debug/completablefuture")
    public DeferredResult<ResponseEntity<ProfilingResult<Integer>>> loadProfiled() {
        DeferredResult<ResponseEntity<ProfilingResult<Integer>>> output = new DeferredResult<>();

        ForkJoinPool.commonPool().submit(
                () -> {
                    ProfilingResult<Optional<Integer>> res =
                            Profilers.profile(() -> {
                                try {
                                    return Optional.of(
                                            service.loadDataFromFile(Optional.empty())
                                                    .thenApply(List::size).get()
                                    );
                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();

                                    return Optional.empty();
                                }
                            });

                    output.setResult(res.getResult()
                            .map(value -> ResponseEntity.ok((ProfilingResult<Integer>) new SimpleProfilingResult<>(res.getElapsedTime(), value)))
                            .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
                }
        );

        return output;
    }

    @Override
    @RequestMapping(path = "/data/download/completablefuture/{page}")
    public Callable<ResponseEntity<List<DataObjectDto>>> read(Integer page) {
        return () ->
                ResponseEntity.ok(
                        service.readPage(page)
                                .thenApply(list ->
                                        list.stream()
                                                .map(converter)
                                                .collect(Collectors.toList())
                                )
                                .join()
                );
    }

    @Override
    @RequestMapping(path = "/data/download/debug/completablefuture/{page}")
    public Callable<ResponseEntity<ProfilingResult<List<DataObjectDto>>>> readProfiled(Integer page) {

        return () -> {
            ProfilingResult<Optional<List<DataObjectDto>>> res = Profilers.profile(() -> {
                        try {
                            return Optional.of(
                                    service.readPage(page)
                                            .thenApply(list ->
                                                    list.stream()
                                                            .map(converter)
                                                            .collect(Collectors.toList()))
                                            .get()
                            );
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();

                            return Optional.empty();
                        }
                    }
            );

            return res.getResult()
                    .map(value -> ResponseEntity.ok((ProfilingResult<List<DataObjectDto>>) new SimpleProfilingResult<>(res.getElapsedTime(), value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        };
    }
}
