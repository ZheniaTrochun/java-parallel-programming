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

    public static <T, S> CompletableFuture<Pair<S, T>> getPairOrFail(CompletableFuture<Pair<S, Optional<T>>> futureOption,
                                                                     Supplier<Throwable> failError) {

        return futureOption.thenCompose(opt ->
                Pair.traverse(opt)
                        .map(CompletableFuture::completedFuture)
                        .orElseGet(() -> FutureUtils.failed(failError.get()))
        );
    }

    public static <T> CompletableFuture<Stream<T>> getStreamOrFail(CompletableFuture<Stream<Optional<T>>> futureOption,
                                                                   Supplier<Throwable> failError) {

        return futureOption.thenCompose(stream ->
                stream.anyMatch(opt -> !opt.isPresent()) ?
                        FutureUtils.failed(failError.get()) :
                        CompletableFuture.completedFuture(stream.map(Optional::get))
        );
    }

    public static <T> CompletableFuture<Stream<T>> getFutureStreamOrFail(Stream<Optional<T>> stream,
                                                                         Supplier<Throwable> failError) {

        List<Optional<T>> list = stream.collect(Collectors.toList());

        return list.contains(Optional.<T>empty()) ?
                FutureUtils.failed(failError.get()) :
                CompletableFuture.completedFuture(list.stream().map(Optional::get));
    }


    public static <T> CompletableFuture<List<T>> getFutureListOrFail(List<Optional<T>> list,
                                                                     Supplier<Throwable> failError) {

        return list.contains(Optional.<T>empty()) ?
                FutureUtils.failed(failError.get()) :
                CompletableFuture.completedFuture(list.stream().map(Optional::get).collect(Collectors.toList()));
    }


    public static <T> CompletableFuture<Stream<T>> getFutureStreamOptionOrFail(CompletableFuture<Stream<Optional<T>>> future,
                                                                               Supplier<Throwable> failError) {

        return future.thenCompose(stream ->
                stream.anyMatch(opt -> !opt.isPresent()) ?
                        FutureUtils.failed(failError.get()) :
                        CompletableFuture.completedFuture(stream.map(Optional::get))
        );
    }


    public static <T, R> Stream<CompletableFuture<R>> iterateParallel(Stream<T> range, Function<T, R> mapper) {
        return range.map(item -> CompletableFuture.supplyAsync(() -> mapper.apply(item)));
    }

    public static <R> List<CompletableFuture<R>> iterateParallel(int times, Function<Integer, R> mapper) {
        return IntStream.range(0, times)
                .boxed()
                .map(item -> CompletableFuture.supplyAsync(() -> mapper.apply(item)))
                .collect(Collectors.toList());
    }
}
