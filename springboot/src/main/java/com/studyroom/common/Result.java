package com.studyroom.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private boolean success;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        result.setSuccess(true);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        result.setSuccess(false);
        result.setData(null);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setSuccess(false);
        result.setData(null);
        return result;
    }

    // 添加一个用于登录成功的方法
    public static <T> Result<T> loginSuccess(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("登录成功");
        result.setData(data);
        result.setSuccess(true);
        return result;
    }
} 