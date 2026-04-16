package com.pidev.services;

import java.util.List;

/**
 * Generic CRUD interface for all services.
 */
public interface IService<T> {
    void add(T t) throws Exception;
    void update(T t) throws Exception;
    void delete(int id) throws Exception;
    List<T> getAll() throws Exception;
    T getById(int id) throws Exception;
}
