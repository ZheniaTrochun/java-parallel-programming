package com.yevhenii.service.profiling;

import org.apache.logging.slf4j.SLF4JLogger;

public class SimpleProfilingResult<T> implements ProfilingResult<T> {

    private final long time;
    private final T result;

    public SimpleProfilingResult(long time, T result) {
        this.time = time;
        this.result = result;
    }

    @Override
    public Long getElapsedTime() {
        return time;
    }

    @Override
    public T getResult() {
        return result;
    }

    @Override
    public void log(SLF4JLogger logger) {
        logger.debug("Action completed in " + time + " ms");
    }

    @Override
    public String toString() {
        return "SimpleProfilingResult{" +
                "time=" + time +
                ", result=" + result +
                '}';
    }
}
