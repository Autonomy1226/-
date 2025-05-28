package com.studyroom.mapper;

import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface BaseMapper<T> {
    // 插入数据
    int insert(T entity);
    
    // 根据ID删除
    int deleteById(@Param("id") Long id);
    
    // 更新数据
    int update(T entity);
    
    // 根据ID查询
    T selectById(@Param("id") Long id);
    
    // 查询所有
    List<T> selectAll();
} 