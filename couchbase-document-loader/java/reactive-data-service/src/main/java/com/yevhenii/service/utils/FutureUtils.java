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

    public static <T> CompletableFuture<List<T>> traverse(List<CompletableFuture<T>> futureStream) {
//        return traverseLoop(futureStream, CompletableFuture.completedFuture(Stream.empty()));
        return traverseLoop(futureStream, CompletableFuture.completedFuture(new ArrayList<>()));

//        return futureStream.stream().collect(Collector.<CompletableFuture<T>, CompletableFuture<List<T>>>of(
//                () -> CompletableFuture.completedFuture(new LinkedList<>()),
//
//                (acc, curr) ->
//                        acc.thenCompose(list ->
//                                curr.thenApply(c -> {
//                                            list.add(c);
//                                            return list;
//                                        }
//                                )),
//
//                (res1, res2) ->
//                        res1.thenCompose(r1 ->
//                                res2.thenApply(r2 -> {
//                                            r1.addAll(r2);
//                                            return r1;
//                                        }
//                                ))
//        ));

//
//        return futureStream.collect(Collector.of(
//                () -> CompletableFuture.completedFuture(Stream.<T>empty()),
//
//                (acc, curr) ->
//                        acc.thenComposeAsync(stream ->
//                                curr.thenApplyAsync(c ->
//                                        Stream.concat(stream, Stream.of(c)))),
//
//                (res1, res2) ->
//                        res1.thenComposeAsync(r1 ->
//                                res2.thenApplyAsync(r2 ->
//                                        Stream.concat(r1, r2)))
//        ));
    }

    public static <T> CompletableFuture<Stream<T>> traverse(Stream<CompletableFuture<T>> futureStream) {
        return traverseLoop(futureStream, CompletableFuture.completedFuture(Stream.empty()));

//        return CompletableFuture.supplyAsync(() -> futureStream.map(CompletableFuture::join));

//        return futureStream.collect(Collector.<CompletableFuture<T>, CompletableFuture<List<T>>>of(
//                () -> CompletableFuture.completedFuture(new LinkedList<>()),
//
//                (acc, curr) ->
//                        acc.thenCompose(list ->
//                                curr.thenApply(c -> {
//                                            list.add(c);
//                                            return list;
//                                        }
//                                        )),
//
//                (res1, res2) ->
//                        res1.thenCompose(r1 ->
//                                res2.thenApply(r2 -> {
//                                            r1.addAll(r2);
//                                            return r1;
//                                        }
//                                        ))
//        )).thenApply(Collection::stream);
//
//        return futureStream.collect(Collector.of(
//                () -> CompletableFuture.completedFuture(Stream.<T>empty()),
//
//                (acc, curr) ->
//                        acc.thenComposeAsync(stream ->
//                                curr.thenApplyAsync(c ->
//                                        Stream.concat(stream, Stream.of(c)))),
//
//                (res1, res2) ->
//                        res1.thenComposeAsync(r1 ->
//                                res2.thenApplyAsync(r2 ->
//                                        Stream.concat(r1, r2)))
//        ));
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
//
//        return stream.anyMatch(opt -> !opt.isPresent()) ?
//                        FutureUtils.failed(failError.get()) :
//                        CompletableFuture.completedFuture(stream.map(Optional::get));
    }


    public static <T> CompletableFuture<List<T>> getFutureListOrFail(List<Optional<T>> list,
                                                                     Supplier<Throwable> failError) {


        return list.contains(Optional.<T>empty()) ?
                FutureUtils.failed(failError.get()) :
                CompletableFuture.completedFuture(list.stream().map(Optional::get).collect(Collectors.toList()));
//
//        return stream.anyMatch(opt -> !opt.isPresent()) ?
//                        FutureUtils.failed(failError.get()) :
//                        CompletableFuture.completedFuture(stream.map(Optional::get));
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

    private static <T> CompletableFuture<Stream<T>> traverseLoop(Stream<CompletableFuture<T>> futureStream,
                                                                 CompletableFuture<Stream<T>> current) {

        List<CompletableFuture<T>> futureList = futureStream.collect(Collectors.toList());

        Optional<CompletableFuture<T>> head = futureList.stream().findFirst();

        if (!head.isPresent()) {
            return current;
        }

        CompletableFuture<T> future = head.get();

        return traverseLoop(
                futureList.stream().skip(1),
                current.thenComposeAsync(stream -> future.thenApplyAsync(curr -> Stream.concat(stream, Stream.of(curr))))
        );
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
}
