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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    @Operation(summary = "获取用户信息", description = "获取当前登录用户信息或指定用户信息")
    public Result<UserVO> getUserInfo(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return Result.success(userService.getUserInfo(userId));
        }
        Long currentUserId = BaseContext.getCurrentId();
        return Result.success(userService.getUserInfo(currentUserId));
    }

    @GetMapping("/info/query")
    @Operation(summary = "根据用户ID查询用户信息", description = "根据userId查询用户信息，不需要登录")
    public Result<UserVO> getUserInfoByUserId(@RequestParam(name = "userId") Long userId) {
        return Result.success(userService.getUserInfo(userId));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户信息", description = "更新用户资料")
    public Result<UserVO> updateUser(@RequestBody UserUpdateDTO dto) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(userService.updateUser(userId, dto));
    }

    @PostMapping("/avatar")
    @Operation(summary = "上传头像", description = "上传用户头像")
    public Result<String> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(userService.uploadAvatar(userId, file));
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门博主", description = "获取点赞数最多的用户")
    public Result<List<UserVO>> getHotUsers(@RequestParam(name = "limit", defaultValue = "5") Integer limit) {
        return Result.success(userService.getHotUsers(limit));
    }
}