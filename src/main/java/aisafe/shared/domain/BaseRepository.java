package aisafe.shared.domain;

import java.util.List;

public interface BaseRepository<T> {
    long count();
    List<T> findAll();
    T save(T entity);
    void delete(T entity);
}
