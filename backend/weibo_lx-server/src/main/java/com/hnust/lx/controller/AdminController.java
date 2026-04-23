package com.hnust.lx.controller;

import com.hnust.lx.dto.AdminLoginDTO;
import com.hnust.lx.dto.UserStatusDTO;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.result.Result;
import com.hnust.lx.service.AdminService;
import com.hnust.lx.vo.StatsVO;
import com.hnust.lx.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "管理员管理", description = "管理员登录及用户管理")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理员登录验证")
    public Result<UserVO> login(@RequestBody AdminLoginDTO dto) {
        return Result.success(adminService.login(dto));
    }

    @GetMapping("/stats")
    @Operation(summary = "获取统计数据", description = "获取用户、动态、评论、标签数量")
    public Result<StatsVO> getStats() {
        return Result.success(adminService.getStats());
    }

    @GetMapping("/user/list")
    @Operation(summary = "用户列表", description = "分页获取用户列表")
    public Result<PageResult> listUsers(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        return Result.success(adminService.listUsers(page, pageSize));
    }

    @PutMapping("/user/status")
    @Operation(summary = "修改用户状态", description = "启用/禁用用户账号")
    public Result<Void> updateUserStatus(@RequestBody UserStatusDTO dto) {
        adminService.updateUserStatus(dto.getUserId(), dto.getIsDeleted());
        return Result.success();
    }

    @DeleteMapping("/user/{id}")
    @Operation(summary = "删除用户", description = "删除用户")
    public Result<Void> deleteUser(@PathVariable(name = "id") Long id) {
        adminService.deleteUser(id);
        return Result.success();
    }
}