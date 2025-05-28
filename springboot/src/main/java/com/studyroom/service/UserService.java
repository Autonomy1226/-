package com.studyroom.service;

import com.studyroom.entity.User;
import java.util.List;

public interface UserService extends BaseService<User> {
    // 根据用户名查询用户
    User getByUsername(String username);
    
    // 根据角色查询用户列表
    List<User> getByRole(String role);
    
    // 更新用户状态
    void updateStatus(Long id, String status);
    
    // 用户注册
    User register(User user);
    
    // 用户登录
    User login(String username, String password);
} 