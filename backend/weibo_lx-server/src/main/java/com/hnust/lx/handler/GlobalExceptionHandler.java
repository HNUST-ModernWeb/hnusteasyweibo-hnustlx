package com.hnust.lx.handler;

import com.hnust.lx.exception.*;
import com.hnust.lx.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PasswordErrorException.class)
    public Result<Void> handlePasswordError(PasswordErrorException e) {
        log.error("密码错误: {}", e.getMessage());
        return Result.error(e.getMessage(), 400);
    }

    @ExceptionHandler(AccountLockedException.class)
    public Result<Void> handleAccountLocked(AccountLockedException e) {
        log.error("账号被禁用: {}", e.getMessage());
        return Result.error(e.getMessage(), 403);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public Result<Void> handleAccountNotFound(AccountNotFoundException e) {
        log.error("账号不存在: {}", e.getMessage());
        return Result.error(e.getMessage(), 404);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Result<Void> handleResourceNotFound(ResourceNotFoundException e) {
        log.error("资源不存在: {}", e.getMessage());
        return Result.error(e.getMessage(), 404);
    }

    @ExceptionHandler(BaseException.class)
    public Result<Void> handleBaseException(BaseException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage(), 400);
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage());
        return Result.error(e.getMessage(), 500);
    }
}