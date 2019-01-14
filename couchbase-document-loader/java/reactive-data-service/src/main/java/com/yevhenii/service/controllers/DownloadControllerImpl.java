package com.yevhenii.service.controllers;

import com.yevhenii.service.converters.Converters;
import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import com.yevhenii.service.models.dto.DataObjectDto;
import com.yevhenii.service.services.CompletableFutureService;
import com.yevhenii.service.services.RxService;
import com.yevhenii.service.services.SequentialService;
import com.yevhenii.service.utils.ControllerUtils;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

@Slf4j
@RestController
public class DownloadControllerImpl implements DownloadController {

    private final SequentialService sequentialService;
    private final CompletableFutureService completableFutureService;
    private final RxService rxService;

    private final Function<Document<DataObject>, DataObjectDto> dtoConverter = Converters.documentToDtoConverter;

    public DownloadControllerImpl(SequentialService sequentialService,
                                  CompletableFutureService completableFutureService,
                                  RxService rxService) {

        this.sequentialService = sequentialService;
        this.completableFutureService = completableFutureService;
        this.rxService = rxService;
    }

    @Override
    @RequestMapping(method = RequestMethod.GET, path = "/data/download/completable-future/{page}")
    public ResponseEntity<Long> completableFutureRead(@PathVariable Integer page) throws ExecutionException, InterruptedException {

        return completableFutureService
                .readPage(page)
                .thenApplyAsync(list -> list.stream()
                        .map(dtoConverter)
                        .count()
                )
                .handle((list, err) ->
                    Optional.ofNullable(err)
                            .map(e ->
                                    ControllerUtils.<Long>failWithLogging(e,
                                            "Error occurred during processing sequential upload", log)
                            )
                            .orElseGet(() -> ResponseEntity.ok(list))
                )
                .get();
    }

    @Override
    @RequestMapping(method = RequestMethod.GET, path = "/data/download/sequential/{page}")
    public ResponseEntity<Long> sequentialRead(@PathVariable Integer page) {
        return ResponseEntity.ok(
                sequentialService.readPage(page).stream()
                        .map(dtoConverter)
                        .count()
        );
    }

    @Override
    public ResponseEntity<Long> streamsRead(Integer page) {
        return null;
    }

    @Override
    @RequestMapping(method = RequestMethod.GET, path = "/data/download/rx/{page}")
    public Single<Long> reactiveRead(@PathVariable Integer page) {
        return rxService.readPage(page)
                .toFlowable(BackpressureStrategy.BUFFER)
                .parallel()
                .map(dtoConverter::apply)
                .sequential()
                .count();
    }
}
