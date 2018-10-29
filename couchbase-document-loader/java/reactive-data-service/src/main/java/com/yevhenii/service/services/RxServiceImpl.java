package com.yevhenii.service.services;

import com.google.gson.JsonParseException;
import com.yevhenii.service.configs.AppPropertyHolder;
import com.yevhenii.service.converters.DtoToDocumentConverter;
import com.yevhenii.service.dao.ReactiveCouchbaseDao;
import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import com.yevhenii.service.models.dto.DataObjectDto;
import com.yevhenii.service.utils.FileUtils;
import com.yevhenii.service.utils.JsonUtils;
import com.yevhenii.service.utils.RxUtils;
import io.reactivex.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RxServiceImpl implements RxService {

    private final String DEFAULT_FILE;
    private final int PARALLELISM;

    private final ReactiveCouchbaseDao<DataObject> dao;
    private final Function<DataObjectDto, Document<DataObject>> toDocumentConverter;

    @Autowired
    public RxServiceImpl(ReactiveCouchbaseDao<DataObject> dao,
                         DtoToDocumentConverter toDocumentConverter,
                         AppPropertyHolder properties) {
        this.dao = dao;
        this.toDocumentConverter = toDocumentConverter;
        this.DEFAULT_FILE = "data.txt";
//        this.DEFAULT_FILE = properties.getDatafile();
        this.PARALLELISM = 10;
//        this.PARALLELISM = properties.getParallelism();
    }

    //  TODO think about this
    @Override
    public Flowable<Document<DataObject>> loadDataFromFile(Optional<String> filename) {

        File file = new File(filename.orElse(DEFAULT_FILE));

        int partSize = (int) Math.ceil((double) file.length() / PARALLELISM);

        Maybe<String> string =
                RxUtils.parallel(PARALLELISM, (i) -> FileUtils.readPart(file, i * partSize, partSize))
                        .map(Optional::get)
                        .reduce(String::concat);

//        string.subscribe(str -> log.debug("file:\n" + str + "\nEOF"));

//                Flowable.fromIterable(IntStream.range(0, PARALLELISM).boxed().collect(Collectors.toList()))
//                        .flatMap(i -> Flowable.just(i).subscribeOn(Schedulers.computation()))
//                        .map(i -> FileUtils.readPart(file, i * partSize, partSize))
//                        .map(Optional::get)
//                        .collect(() -> "", String::concat);

        return string.toFlowable()
                .map(str -> str.replace("}{", "}\n{"))
                .flatMap(str ->
                        RxUtils.parallelSingle(
                                Arrays.stream(str.split("\n"))
                                        .filter(s -> !s.isEmpty())
                                        .collect(Collectors.toList()),
                                PARALLELISM,
                                (elem) -> JsonUtils.tryReadJson(elem, DataObjectDto.class)
                                        .map(toDocumentConverter)
                                        .map(dao::insert)
                                        .orElseThrow(() -> new JsonParseException("invalid json!"))
                        ).toFlowable(BackpressureStrategy.BUFFER)
                ).doOnError(err -> log.error(err.getMessage()));


//                .toFlowable()
//                .map(str -> str.split("\n"))
//                .flatMap(Flowable::fromArray)
//                .map(json -> JsonUtils.readJson(json, DataObjectDto.class))
//                // will fail if there will be some incorrect json objects
//                .map(Optional::get)
//                .map(toDocumentConverter)
//                .map(dao::insert)
//                // TODO check next line
//                .flatMap(Single::toFlowable);

//        return Flowable.fromIterable(IntStream.range(0, PARALLELISM).boxed().collect(Collectors.toList()))
//                .map(i -> FileUtils.readPart(file, i * partSize, partSize))
//                .map(Optional::get)
//                .collect(() -> "", String::concat)
//                .toFlowable()
//                .map(str -> str.split("\n"))
//                .flatMap(Flowable::fromArray)
//                .map(json -> JsonUtils.readJson(json, DataObjectDto.class))
//                // will fail if there will be some incorrect json objects
//                .map(Optional::get)
//                .map(toDocumentConverter)
//                .map(dao::insert)
//                // TODO check next line
//                .flatMap(Single::toFlowable);
    }

    @Override
    public Observable<Document<DataObject>> readPage(int page) {
        return dao.findAll(page);
    }
}
