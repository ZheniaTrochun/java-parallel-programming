package com.yevhenii.service.profiling;

import com.yevhenii.service.utils.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.slf4j.SLF4JLogger;

import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class Profilers {

    public static <T, R> Function<T, R> simpleProfiler(String message, Function<T, R> subject) {
        return input -> {
            long start = System.currentTimeMillis();
            R res = subject.apply(input);
            long end = System.currentTimeMillis();

            log.debug(String.format("%s completed in %d ms", message, end - start));

            return res;
        };
    }

//    public static <T, R> Function<? super T, ? extends R> simpleProfiler(String message, Function<? super T, ? extends R> subject) {
//        return input -> {
//            long start = System.currentTimeMillis();
//            R res = subject.apply(input);
//            long end = System.currentTimeMillis();
//
//            log.debug(String.format("%s completed in %d ms", message, end - start));
//
//            return res;
//        };
//    }

    public static <T> Supplier<T> withProfiler(Supplier<T> subject, String message) {
        return () -> {
            long start = System.currentTimeMillis();
            T res = subject.get();
            long end = System.currentTimeMillis();

            log.debug(String.format("%s completed in %d ms", message, end - start));

            return res;
        };
    }

    public static <T> Supplier<Pair<Long, T>> withProfiler(Supplier<T> subject) {
        return () -> {
            long start = System.currentTimeMillis();
            T res = subject.get();
            long end = System.currentTimeMillis();

            return Pair.of(end - start, res);
        };
    }

    public static <T, R> Function<? super T, ? extends Pair<Long, R>> withProfiler(Function<? super T, ? extends R> subject) {
        return (prev) -> {
            long start = System.currentTimeMillis();
            R res = subject.apply(prev);
            long end = System.currentTimeMillis();

            return Pair.of(end - start, res);
        };
    }

    public static <T, R> Function<? super Pair<Long, T>, ? extends Pair<Long, R>> withProfilerChained(Function<? super T, ? extends R> subject) {
        return (prev) -> {
            long start = System.currentTimeMillis();
            R res = subject.apply(prev.getRight());
            long end = System.currentTimeMillis();

            return Pair.of(end - start + prev.getLeft(), res);
        };
    }

    public static <T, R> Function<? super ProfilingResult<T>, ? extends ProfilingResult<R>> profile(Function<? super T, ? extends R> subject) {
        return (prev) -> {
            long start = System.currentTimeMillis();
            R res = subject.apply(prev.getResult());
            long end = System.currentTimeMillis();

            return new SimpleProfilingResult<>(end - start + prev.getElapsedTime(), res);
        };
    }

    public static <T> ProfilingResult<T> profile(Supplier<T> subject) {
        long start = System.currentTimeMillis();
        T res = subject.get();
        long end = System.currentTimeMillis();

        return new SimpleProfilingResult<>(end - start, res);
    }
}
