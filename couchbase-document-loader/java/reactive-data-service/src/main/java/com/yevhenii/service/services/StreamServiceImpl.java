package com.yevhenii.service.services;

import com.yevhenii.service.configs.AppPropertyHolder;
import com.yevhenii.service.converters.Converters;
import com.yevhenii.service.dao.CouchbaseDao;
import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import com.yevhenii.service.models.dto.DataObjectDto;
import com.yevhenii.service.utils.FileUtils;
import com.yevhenii.service.utils.FunctionalUtils;
import com.yevhenii.service.utils.JsonUtils;
import com.yevhenii.service.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class StreamServiceImpl implements StreamService {

    private final String DEFAULT_FILE;
    private final int PARALLELISM;

    private final CouchbaseDao<DataObject> dao;

    @Autowired
    public StreamServiceImpl(CouchbaseDao<DataObject> dao, AppPropertyHolder propertyHolder) {
        DEFAULT_FILE = propertyHolder.getDatafile();
        PARALLELISM = propertyHolder.getParallelism();
        this.dao = dao;
    }

    @Override
    public List<Document<DataObject>> upload() throws IOException {
        String fileContent = readFileParallel().orElseThrow(IOException::new);

        Optional<List<DataObjectDto>> dtos = FunctionalUtils.traverse(
                Utils.splitByLines(fileContent)
                    .stream()
                    .parallel()
                    .map(line -> JsonUtils.readJson(line, DataObjectDto.class))
                    .collect(Collectors.toList())
        );

        return dtos.map(list -> list.stream()
                .parallel()
                .map(Converters.dtoToDocumentConverter)
                .map(dao::insert)
                .collect(Collectors.toList())
        ).orElseThrow(IOException::new);
    }

    @Override
    public List<Document<DataObject>> download(int page) {
        return null;
    }

    private Optional<String> readFileParallel() {
        File file = new File(DEFAULT_FILE);
        int partSize = (int) Math.ceil((double) file.length() / PARALLELISM);

        return FunctionalUtils.traverse(
                IntStream.range(0, PARALLELISM).parallel()
                        .boxed()
                        .map(i -> FileUtils.readPart(file, partSize * i, partSize))
                        .collect(Collectors.toList())
        ).map(list -> String.join("", list));
    }
}
