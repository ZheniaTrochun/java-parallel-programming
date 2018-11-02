package com.yevhenii.service.controllers;

import io.reactivex.Single;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.ExecutionException;

public interface DownloadController {

    ResponseEntity<Long> completableFutureRead(Integer page) throws ExecutionException, InterruptedException;

    ResponseEntity<Long> sequentialRead(Integer page);

    Single<Long> reactiveRead(Integer page);
}
