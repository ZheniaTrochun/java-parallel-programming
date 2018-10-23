package com.yevhenii.service.profiling;

import org.apache.logging.slf4j.SLF4JLogger;

public interface ProfilingResult<T> {

    Long getElapsedTime();

    T getResult();

    void log(SLF4JLogger logger);
}
