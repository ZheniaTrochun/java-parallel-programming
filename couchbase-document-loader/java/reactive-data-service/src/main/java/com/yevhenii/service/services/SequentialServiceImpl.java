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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.File;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableConfigurationProperties(AppPropertyHolder.class)
public class SequentialServiceImpl implements SequentialService {

    private final String DEFAULT_FILE;

    private final CouchbaseDao<DataObject> dao;
    private final Function<DataObjectDto, Document<DataObject>> toDocumentConverter = Converters.dtoToDocumentConverter;

    @Autowired
    public SequentialServiceImpl(CouchbaseDao<DataObject> dao,
                                 AppPropertyHolder properties) {
        this.dao = dao;
        this.DEFAULT_FILE = properties.getDatafile();
    }

//    TODO rewrite
    @Override
    public List<Document<DataObject>> loadDataFromFile() throws IOException {
        File file = new File(DEFAULT_FILE);
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
}
