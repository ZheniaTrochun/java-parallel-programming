package com.yevhenii.service.controllers;

import io.reactivex.Single;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.ExecutionException;

public interface UploadController {

    ResponseEntity<Integer> completableFutureUpload() throws ExecutionException, InterruptedException;

    ResponseEntity<Integer> sequentialUpload();

    Single<Integer> reactiveUpload();
}
