package com.hnust.lx.controller;

import com.hnust.lx.context.BaseContext;
import com.hnust.lx.dto.*;
import com.hnust.lx.result.Result;
import com.hnust.lx.service.UserService;
import com.hnust.lx.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册登录及信息管理")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户")
    public Result<UserVO> register(@RequestBody UserRegisterDTO dto) {
        return Result.success(userService.register(dto));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录验证")
    public Result<UserVO> login(@RequestBody UserLoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前用户信息")
    public Result<UserVO> getUserInfo() {
        Long userId = BaseContext.getCurrentId();
        return Result.success(userService.getUserInfo(userId));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户信息", description = "更新用户资料")
    public Result<UserVO> updateUser(@RequestBody UserUpdateDTO dto) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(userService.updateUser(userId, dto));
    }
}
