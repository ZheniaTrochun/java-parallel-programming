package com.yevhenii.service.controllers;

import com.yevhenii.service.models.dto.DataObjectDto;
import com.yevhenii.service.profiling.ProfilingResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

public interface SequentialController {

    ResponseEntity<ProfilingResult<Integer>> loadProfiled();

    ResponseEntity<ProfilingResult<List<DataObjectDto>>> readProfiled(Integer page);
}
