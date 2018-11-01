package com.yevhenii.service.controllers;

import com.couchbase.client.core.CouchbaseException;
import com.yevhenii.service.services.CompletableFutureService;
import com.yevhenii.service.services.RxService;
import com.yevhenii.service.services.SequentialService;
import com.yevhenii.service.utils.ControllerUtils;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
public class UploadControllerImpl implements UploadController {

    private final SequentialService sequentialService;
    private final CompletableFutureService completableFutureService;
    private final RxService rxService;

    @Autowired
    public UploadControllerImpl(SequentialService sequentialService,
                                CompletableFutureService completableFutureService,
                                RxService rxService) {

        this.sequentialService = sequentialService;
        this.completableFutureService = completableFutureService;
        this.rxService = rxService;
    }

    @Override
    @RequestMapping(method = RequestMethod.GET, path = "/data/upload/completable-future")
    public ResponseEntity<Integer> completableFutureUpload() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future =
                completableFutureService.loadDataFromFile()
                        .thenApply(List::size);

        return future.handle((count, err) ->
                Optional.ofNullable(err)
                        .map(e ->
                                ControllerUtils.<Integer>failWithLogging(e,
                                        "Error occurred during processing completable-future upload", log)
                        )
                        .orElseGet(() -> ResponseEntity.ok(count))
        ).get();
    }

    @Override
    @RequestMapping(method = RequestMethod.GET, path = "/data/upload/sequential")
    public ResponseEntity<Integer> sequentialUpload() {
        try {
            return ResponseEntity.ok(
                    sequentialService.loadDataFromFile().size()
            );
        } catch (IOException e) {
            return ControllerUtils.failWithLogging(e,
                    "Error occurred during processing sequential upload", log);
        } catch (CouchbaseException e) {
            return ControllerUtils.failWithLogging(e,
                    "Error occurred during processing sequential upload in couchbase", log);
        }
    }

    @Override
    @RequestMapping(method = RequestMethod.GET, path = "/data/upload/rx")
    public Single<Integer> reactiveUpload() {
        return rxService.loadDataFromFile()
                .count()
                .map(Long::intValue);
    }
}

