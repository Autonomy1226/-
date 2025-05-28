package com.studyroom.service;

import java.util.List;

public interface BaseService<T> {
    // 创建
    T create(T entity);
    
    // 删除
    void delete(Long id);
    
    // 更新
    T update(T entity);
    
    // 根据ID查询
    T getById(Long id);
    
    // 查询所有
    List<T> getAll();
} 