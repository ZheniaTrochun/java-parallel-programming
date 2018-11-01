package com.yevhenii.service.services;

import com.google.gson.JsonParseException;
import com.yevhenii.service.configs.AppPropertyHolder;
import com.yevhenii.service.converters.Converters;
import com.yevhenii.service.dao.ReactiveCouchbaseDao;
import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import com.yevhenii.service.models.dto.DataObjectDto;
import com.yevhenii.service.utils.FileUtils;
import com.yevhenii.service.utils.JsonUtils;
import com.yevhenii.service.utils.Utils;
import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Service
@EnableConfigurationProperties(AppPropertyHolder.class)
public class RxServiceImpl implements RxService {

    private final String DEFAULT_FILE;
    private final int PARALLELISM;

    private final ReactiveCouchbaseDao<DataObject> dao;
    private final Function<DataObjectDto, Document<DataObject>> toDocumentConverter = Converters.dtoToDocumentConverter;

    @Autowired
    public RxServiceImpl(ReactiveCouchbaseDao<DataObject> dao,
                         AppPropertyHolder properties) {
        this.dao = dao;
        this.DEFAULT_FILE = properties.getDatafile();
        this.PARALLELISM = properties.getParallelism();
    }

    @Override
    public Flowable<Document<DataObject>> loadDataFromFile() {

        return readFileParallel()
                .toFlowable()
                .map(Utils::splitByLines)
                .flatMap(this::divideIntoParts)
                .flatMap(part -> deserializeAndSave(part).toFlowable())
                .doOnError(err -> log.error(err.getMessage()))
                .doOnComplete(dao::closeCurrentBucket);
    }

    @Override
    public Observable<Document<DataObject>> readPage(int page) {
        return dao.findAll(page);
    }


    private Maybe<String> readFileParallel() {
        File file = new File(DEFAULT_FILE);

        int partSize = (int) Math.ceil((double) file.length() / PARALLELISM);

        return Observable.range(0, PARALLELISM)
                .subscribeOn(Schedulers.computation())
                .map(i -> FileUtils.readPart(file, i * partSize, partSize))
                .map(Optional::get)
                .map(StringBuffer::new)
                .reduce(StringBuffer::append)
                .map(StringBuffer::toString);
    }

    private Flowable<String> divideIntoParts(List<String> list) {
        return Flowable.fromIterable(Utils.divideIntoParts(list, PARALLELISM))
                .concatMap(lst -> Flowable.fromIterable(lst).subscribeOn(Schedulers.computation()));
    }

    private Single<Document<DataObject>> deserializeAndSave(String str) {
        return JsonUtils.readJson(str, DataObjectDto.class)
                .map(toDocumentConverter)
                .map(dao::insert)
                .orElseGet(() -> Single.error(new JsonParseException(str)));
    }
}
