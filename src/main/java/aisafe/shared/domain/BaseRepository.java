package aisafe.shared.domain;

import java.util.List;

public interface BaseRepository<T> {
    long count();
    List<T> findAll();
    void save(T entity);
    void delete(T entity);
}
