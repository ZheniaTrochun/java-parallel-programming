package com.yevhenii.service.dao;

import java.util.List;
import java.util.Optional;

public interface Dao<K, E> {

    Optional<E> findById(K id);

    List<E> findAll();

    List<E> findAll(int page);

    List<E> findAll(int offset, int size);

    E insert(E entity);

    E update(E entity);

    boolean delete(K id);

    boolean deleteAll();

    int getSize();

    int getPageSize();
}
