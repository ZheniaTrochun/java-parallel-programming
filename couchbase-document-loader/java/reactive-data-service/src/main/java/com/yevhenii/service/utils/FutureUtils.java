package com.yevhenii.service.utils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FutureUtils {

    public static <T> CompletableFuture<T> failed(Throwable err) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(err);
        return future;
    }

    public static <T> CompletableFuture<T> getOrFail(CompletableFuture<Optional<T>> futureOption,
                                                     Supplier<Throwable> failError) {

        return futureOption.thenCompose(opt ->
                opt.map(CompletableFuture::completedFuture)
                        .orElseGet(() -> FutureUtils.failed(failError.get()))
        );
    }

    public static <T> CompletableFuture<List<T>> getFutureListOrFail(List<Optional<T>> list,
                                                                     Supplier<Throwable> failError) {

        return list.contains(Optional.<T>empty()) ?
                FutureUtils.failed(failError.get()) :
                CompletableFuture.completedFuture(list.stream().map(Optional::get).collect(Collectors.toList()));
    }

    public static <R> List<CompletableFuture<R>> iterateParallel(IntStream range, Function<Integer, R> mapper) {
        return range.boxed()
                .map(item -> CompletableFuture.supplyAsync(() -> mapper.apply(item)))
                .collect(Collectors.toList());
    }


    public static <T> CompletableFuture<List<T>> traverse(List<CompletableFuture<T>> futures) {
        return traverseLoop(futures, CompletableFuture.completedFuture(new ArrayList<>()));
    }

//    tail recursive
    private static <T> CompletableFuture<List<T>> traverseLoop(List<CompletableFuture<T>> futureList,
                                                               CompletableFuture<List<T>> current) {
        Optional<CompletableFuture<T>> head = futureList.stream().findFirst();
        if (!head.isPresent()) {
            return current;
        }
        CompletableFuture<T> future = head.get();
        return traverseLoop(
                futureList.stream().skip(1).collect(Collectors.toList()),
                current.thenComposeAsync(stream ->
                        future.thenApplyAsync(curr ->
                                Stream.concat(stream.stream(), Stream.of(curr)).collect(Collectors.toList())
                        )
                )
        );
    }

}
