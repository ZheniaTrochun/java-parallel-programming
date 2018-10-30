package com.yevhenii.service.dao;

import io.reactivex.*;

public interface ReactiveDao<K, E> {

    Maybe<E> findById(K id);

    Flowable<E> findAll();

    Observable<E> findAll(int page);

    Observable<E> findAll(int offset, int size);

    Single<E> insert(E entity);

    Single<E> update(E entity);

    Completable delete(K id);

    int getPageSize();

    Observable<Boolean> close();

    Observable<Boolean> closeCurrentBucket();
}
