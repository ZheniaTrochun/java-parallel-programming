package com.yevhenii.service.controllers;

import com.yevhenii.service.converters.DocumentToDtoConverter;
import com.yevhenii.service.models.dto.DataObjectDto;
import com.yevhenii.service.services.RxService;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class RxControllerImpl implements RxController {

    private final RxService service;
    private final DocumentToDtoConverter toDtoConverter;

    @Autowired
    public RxControllerImpl(RxService service, DocumentToDtoConverter toDtoConverter) {
        this.service = service;
        this.toDtoConverter = toDtoConverter;
    }

//    @Override
//    @RequestMapping(path = "/data/download/rx/{page}")
//    public Observable<DataObjectDto> read(Integer page) {
//
//        return service.readPage(page)
//                .map(toDtoConverter::apply);
//    }
}
