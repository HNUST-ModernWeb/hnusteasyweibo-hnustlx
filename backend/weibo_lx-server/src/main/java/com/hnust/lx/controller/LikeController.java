package com.hnust.lx.controller;

import com.hnust.lx.context.BaseContext;
import com.hnust.lx.dto.*;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.result.Result;
import com.hnust.lx.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
@Tag(name = "点赞管理", description = "帖子点赞相关接口")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/toggle")
    @Operation(summary = "切换点赞状态", description = "对帖子点赞或取消点赞")
    public Result<Boolean> toggleLike(@RequestBody LikeToggleDTO dto) {
        Long userId = BaseContext.getCurrentId();
        dto.setUserId(userId);
        return Result.success(likeService.toggleLike(dto));
    }

    @GetMapping("/my-list")
    @Operation(summary = "获取我的点赞列表", description = "分页获取用户点赞的帖子")
    public Result<PageResult> getMyLikes(
            @RequestParam(name = "page", defaultValue = "1") Long page,
            @RequestParam(name = "pageSize", defaultValue = "10") Long pageSize) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(likeService.getMyLikes(userId, page, pageSize));
    }

    @GetMapping("/post-list/{postId}")
    @Operation(summary = "获取帖子点赞列表", description = "分页获取帖子的点赞用户")
    public Result<PageResult> getPostLikes(
            @PathVariable(name = "postId") Long postId,
            @RequestParam(name = "page", defaultValue = "1") Long page,
            @RequestParam(name = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.success(likeService.getPostLikes(postId, page, pageSize));
    }

    @GetMapping("/received")
    @Operation(summary = "获取我收到的点赞", description = "分页获取别人点赞我的帖子的列表")
    public Result<PageResult> getReceivedLikes(
            @RequestParam(name = "page", defaultValue = "1") Long page,
            @RequestParam(name = "pageSize", defaultValue = "10") Long pageSize) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(likeService.getReceivedLikes(userId, page, pageSize));
    }
}
