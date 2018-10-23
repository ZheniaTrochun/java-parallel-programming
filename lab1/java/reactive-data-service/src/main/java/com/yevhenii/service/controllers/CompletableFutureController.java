package com.yevhenii.service.controllers;

import com.yevhenii.service.models.dto.DataObjectDto;
import com.yevhenii.service.profiling.ProfilingResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public interface CompletableFutureController {

    ResponseEntity<Integer> load() throws ExecutionException, InterruptedException;

    DeferredResult<ResponseEntity<ProfilingResult<Integer>>> loadProfiled();

    Callable<ResponseEntity<List<DataObjectDto>>> read(Integer page);

    Callable<ResponseEntity<ProfilingResult<List<DataObjectDto>>>> readProfiled(Integer page);
}
