package com.yevhenii.service.controllers;

import com.yevhenii.service.models.dto.DataObjectDto;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface RxController {

    Single<Integer> load();

    Observable<DataObjectDto> read(Integer page);
}
