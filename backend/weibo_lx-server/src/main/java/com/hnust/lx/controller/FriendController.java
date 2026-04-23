package com.hnust.lx.controller;

import com.hnust.lx.context.BaseContext;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.result.Result;
import com.hnust.lx.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
@Tag(name = "好友", description = "好友相关接口")
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/request")
    @Operation(summary = "发送好友申请", description = "向其他用户发送好友申请")
    public Result<Void> sendRequest(@RequestBody Map<String, Long> body) {
        Long userId = BaseContext.getCurrentId();
        Long toUserId = body.get("userId");
        friendService.sendRequest(userId, toUserId);
        return Result.success();
    }

    @PostMapping("/accept")
    @Operation(summary = "同意好友申请", description = "同意好友申请")
    public Result<Void> acceptRequest(@RequestBody Map<String, Long> body) {
        Long userId = BaseContext.getCurrentId();
        Long requestId = body.get("requestId");
        friendService.acceptRequest(userId, requestId);
        return Result.success();
    }

    @PostMapping("/reject")
    @Operation(summary = "拒绝好友申请", description = "拒绝好友申请")
    public Result<Void> rejectRequest(@RequestBody Map<String, Long> body) {
        Long userId = BaseContext.getCurrentId();
        Long requestId = body.get("requestId");
        friendService.rejectRequest(userId, requestId);
        return Result.success();
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除好友", description = "删除指定好友")
    public Result<Void> deleteFriend(@RequestBody Map<String, Long> body) {
        Long userId = BaseContext.getCurrentId();
        Long friendId = body.get("userId");
        friendService.deleteFriend(userId, friendId);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "好友列表", description = "获取当前用户的好友列表")
    public Result<PageResult> getFriendList(
            @RequestParam(name = "page", defaultValue = "1") Long page,
            @RequestParam(name = "pageSize", defaultValue = "20") Long pageSize) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(friendService.getFriendList(userId, page, pageSize));
    }

    @GetMapping("/requests")
    @Operation(summary = "好友申请列表", description = "获取收到的好友申请列表")
    public Result<PageResult> getPendingRequests(
            @RequestParam(name = "page", defaultValue = "1") Long page,
            @RequestParam(name = "pageSize", defaultValue = "20") Long pageSize) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(friendService.getPendingRequests(userId, page, pageSize));
    }

    @GetMapping("/count")
    @Operation(summary = "未处理申请数", description = "获取未处理的好友申请数量")
    public Result<Integer> getPendingCount() {
        Long userId = BaseContext.getCurrentId();
        return Result.success(friendService.getPendingCount(userId));
    }

    @GetMapping("/check")
    @Operation(summary = "检查是否好友", description = "检查是否已经是好友")
    public Result<Boolean> checkFriend(@RequestParam("userId") Long otherUserId) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(friendService.isFriend(userId, otherUserId));
    }
}