package com.yevhenii.service.utils;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RxUtils {

    public static <T> Observable<T> parallel(int parallelism, Function<Integer, T> func) {
        Observable<Integer> range = Observable.range(0, parallelism);

        return range.concatMap(i ->
                Observable.just(i)
                        .subscribeOn(Schedulers.computation())
                        .map(func::apply)
        );
    }

    public static <T, R> Observable<R> parallel(List<T> source, int parallelism, Function<T, R> func) {
        int size = source.size();
        int partSize = (int) Math.ceil((double) size / parallelism);

        Observable<List<R>> lists = parallel(parallelism, (i) -> {
            int finalSize = Math.min(partSize * i + partSize, size) - (partSize * i);
            return source.stream()
                    .skip(partSize * i)
                    .limit(finalSize)
                    .map(func)
                    .collect(Collectors.toList());
        });

        return lists.flatMap(Observable::fromIterable);
    }

    public static <T, R> Observable<R> parallelSingle(List<T> source, int parallelism, Function<T, Single<R>> func) {
        int size = source.size();
        int partSize = (int) Math.ceil((double) size / parallelism);

        Observable<List<Single<R>>> lists = parallel(parallelism, (i) -> {
            int finalSize = Math.min(partSize, size);
            return source.stream()
                    .skip(partSize * i)
                    .limit(finalSize)
                    .map(func::apply)
                    .collect(Collectors.toList());
        });

        return lists.flatMap(list -> Observable.fromIterable(list).flatMap(Single::toObservable));
    }

}
