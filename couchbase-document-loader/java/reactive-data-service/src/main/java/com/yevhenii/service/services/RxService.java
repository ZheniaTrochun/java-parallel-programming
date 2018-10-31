package com.yevhenii.service.services;

import com.yevhenii.service.models.DataObject;
import com.yevhenii.service.models.Document;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import java.util.Optional;

public interface RxService {

    Flowable<Document<DataObject>> loadDataFromFile();

    Observable<Document<DataObject>> readPage(int page);
}
