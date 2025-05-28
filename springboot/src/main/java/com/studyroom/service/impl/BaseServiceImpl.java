package com.studyroom.service.impl;

import com.studyroom.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public abstract class BaseServiceImpl<T, M extends com.studyroom.mapper.BaseMapper<T>> implements BaseService<T> {
    
    @Autowired
    protected M baseMapper;
    
    @Override
    @Transactional
    public T create(T entity) {
        baseMapper.insert(entity);
        return entity;
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        baseMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public T update(T entity) {
        baseMapper.update(entity);
        return entity;
    }
    
    @Override
    public T getById(Long id) {
        return baseMapper.selectById(id);
    }
    
    @Override
    public List<T> getAll() {
        return baseMapper.selectAll();
    }
} 