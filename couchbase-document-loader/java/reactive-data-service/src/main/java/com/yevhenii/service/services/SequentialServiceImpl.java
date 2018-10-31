package com.yevhenii.service.services;

import com.yevhenii.service.configs.AppPropertyHolder;
import com.yevhenii.service.converters.Converters;
import com.yevhenii.service.dao.CouchbaseDao;
import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import com.yevhenii.service.models.dto.DataObjectDto;
import com.yevhenii.service.profiling.Profilers;
import com.yevhenii.service.utils.FileUtils;
import com.yevhenii.service.utils.JsonUtils;
import com.yevhenii.service.utils.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SequentialServiceImpl implements SequentialService {

    private final String DEFAULT_FILE;

    private final CouchbaseDao<DataObject> dao;
    private final Function<DataObjectDto, Document<DataObject>> toDocumentConverter = Converters.dtoToDocumentConverter;

    @Autowired
    public SequentialServiceImpl(CouchbaseDao<DataObject> dao,
                                 AppPropertyHolder properties) {
        this.dao = dao;
        this.DEFAULT_FILE = "data.txt";
    }

//    TODO rewrite
    @Override
    public List<Document<DataObject>> loadDataFromFile(Optional<String> filename) throws IOException {
        File file = new File(filename.orElse(DEFAULT_FILE));
        Optional<String> content = FileUtils.readPart(file, 0, (int) file.length());

        return content.map(str ->
                            Arrays.stream(str.split("\n"))
                                    .map(line -> JsonUtils.readJson(line, DataObjectDto.class))
                                    .map(Optional::get)
                                    .map(toDocumentConverter)
                                    .map(dao::insert)
                                    .collect(Collectors.toList()))
                .orElseThrow(() -> new IOException("Error reading file: " + file.getName()));
    }

    @Override
    public List<Document<DataObject>> readPage(int page) {
        return dao.findAll(page);
    }

    @Override
    public Pair<Long, List<Document<DataObject>>> loadDataFromFileProfiled(Optional<String> filename) throws IOException {
        File file = new File(filename.orElse(DEFAULT_FILE));
        Optional<String> content = FileUtils.readPart(file, 0, (int) file.length());

        return content.map(str ->
                        Profilers.withProfiler(() ->
                                Arrays.stream(str.split("\n"))
                                        .map(line -> JsonUtils.readJson(line, DataObjectDto.class))
                                        .map(Optional::get)
                                        .map(toDocumentConverter)
                                        .map(dao::insert)
                                        .collect(Collectors.toList())
                        ).get()

        ).orElseThrow(() -> new IOException("Error reading file: " + file.getName()));
    }

    @Override
    public Pair<Long, List<Document<DataObject>>> readPageProfiled(int page) {
        return Profilers.withProfiler(() ->
                dao.findAll(page)
        ).get();
    }

}
