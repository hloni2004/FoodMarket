package com.llburgers.service;

import java.util.List;

public interface IService <T, ID>{
    T create(T entity);

    T read(ID id);

    T update(T entity);

    List<T> getAll();

    void delete(ID id);
}
