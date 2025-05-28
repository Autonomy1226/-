package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.entity.User;
import com.studyroom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        try {
            return Result.success(userService.register(user));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public Result<User> login(@RequestParam String username, @RequestParam String password) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return Result.error(400, "用户名不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                return Result.error(400, "密码不能为空");
            }
            
            User user = userService.login(username, password);
            if (user == null) {
                return Result.error(401, "用户名或密码错误");
            }
            
            // 清除密码等敏感信息
            user.setPassword(null);
            return Result.loginSuccess(user);
        } catch (Exception e) {
            return Result.error(500, "登录失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }
    
    @GetMapping("/username/{username}")
    public Result<User> getByUsername(@PathVariable String username) {
        return Result.success(userService.getByUsername(username));
    }
    
    @GetMapping("/role/{role}")
    public Result<List<User>> getByRole(@PathVariable String role) {
        return Result.success(userService.getByRole(role));
    }
    
    @GetMapping
    public Result<List<User>> getAll() {
        return Result.success(userService.getAll());
    }
    
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return Result.success(userService.update(user));
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        userService.updateStatus(id, status);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
} 