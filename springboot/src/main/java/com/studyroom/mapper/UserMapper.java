package com.studyroom.mapper;

import com.studyroom.entity.User;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface UserMapper extends BaseMapper<User> {
    // 根据用户名查询用户
    User selectByUsername(@Param("username") String username);
    
    // 根据角色查询用户列表
    List<User> selectByRole(@Param("role") String role);
    
    // 更新用户状态
    int updateStatus(@Param("id") Long id, @Param("status") String status);
} 