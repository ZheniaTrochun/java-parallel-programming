package com.yevhenii.service.controllers;

import com.yevhenii.service.models.dto.DataObjectDto;
import io.reactivex.Observable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface DownloadController {

    ResponseEntity<List<DataObjectDto>> completableFutureRead(Integer page) throws ExecutionException, InterruptedException;

    ResponseEntity<List<DataObjectDto>> sequentialRead(Integer page);

    Observable<DataObjectDto> reactiveRead(Integer page);
}
