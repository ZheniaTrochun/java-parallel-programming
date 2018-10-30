package com.yevhenii.service.controllers;

import com.yevhenii.service.converters.DocumentToDtoConverter;
import com.yevhenii.service.models.dto.DataObjectDto;
import com.yevhenii.service.profiling.Profilers;
import com.yevhenii.service.profiling.ProfilingResult;
import com.yevhenii.service.services.SequentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class SequentialControllerImpl implements SequentialController {

    private final SequentialService service;
    private final DocumentToDtoConverter toDtoConverter;

    @Autowired
    public SequentialControllerImpl(SequentialService service,
                                    DocumentToDtoConverter toDtoConverter) {
        this.service = service;
        this.toDtoConverter = toDtoConverter;
    }

    @Override
    @RequestMapping(path = "/data/upload/debug/sequential")
    public ResponseEntity<ProfilingResult<Integer>> loadProfiled() {

        ProfilingResult<Integer> profiled = Profilers.profile(() -> {
                try {
                    return service.loadDataFromFile(Optional.empty()).size();
                } catch (IOException e) {
                    e.printStackTrace();
                    return -1;
                }
            });

        return ResponseEntity.ok(profiled);
    }

//    @Override
//    @Async
//    @RequestMapping(path = "/data/download/sequential/{page}")
//    public ResponseEntity<List<DataObjectDto>> read(@PathVariable Integer page) {
//
//        return ResponseEntity.ok(
//                service.readPage(page).stream()
//                        .map(toDtoConverter)
//                        .collect(Collectors.toList())
//        );
//    }

    @Override
    @RequestMapping(path = "/data/download/debug/sequential/{page}")
    public ResponseEntity<ProfilingResult<List<DataObjectDto>>> readProfiled(@PathVariable Integer page) {
        ProfilingResult<List<DataObjectDto>> profiled = Profilers.profile(() ->
                service.readPage(page).stream()
                        .map(toDtoConverter)
                        .collect(Collectors.toList())
        );

        return ResponseEntity.ok(profiled);
    }
}
