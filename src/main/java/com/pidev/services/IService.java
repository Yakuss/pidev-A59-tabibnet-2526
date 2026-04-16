package com.pidev.services;

import java.util.List;

public interface IService<T> {
    void add(T entity) throws Exception;
    void update(T entity) throws Exception;
    void delete(int id) throws Exception;
    T findById(int id) throws Exception;
    List<T> findAll() throws Exception;
}