package com.hnust.lx.controller;

import com.hnust.lx.context.BaseContext;
import com.hnust.lx.dto.*;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.result.Result;
import com.hnust.lx.service.ChatService;
import com.hnust.lx.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
@Tag(name = "群聊", description = "群聊相关接口")
public class GroupChatController {

    private final ChatService chatService;

    @PostMapping("/create")
    @Operation(summary = "创建群聊", description = "创建一个新的群聊")
    public Result<GroupChatVO> createGroup(@RequestBody GroupCreateDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        return Result.success(chatService.createGroup(userId, dto));
    }

    @GetMapping("/list")
    @Operation(summary = "我的群聊", description = "获取当前用户加入的群聊列表")
    public Result<PageResult> getGroupList(
            @RequestParam(name = "page", defaultValue = "1") Long page,
            @RequestParam(name = "pageSize", defaultValue = "10") Long pageSize) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(chatService.getGroupList(userId, page, pageSize));
    }

    @PostMapping("/{groupId}/join")
    @Operation(summary = "加入群聊", description = "加入指定群聊")
    public Result<Void> joinGroup(@PathVariable("groupId") Long groupId) {
        Long userId = BaseContext.getCurrentId();
        chatService.joinGroup(userId, groupId);
        return Result.success();
    }

    @PostMapping("/{groupId}/leave")
    @Operation(summary = "退出群聊", description = "退出指定群聊")
    public Result<Void> leaveGroup(@PathVariable("groupId") Long groupId) {
        Long userId = BaseContext.getCurrentId();
        chatService.leaveGroup(userId, groupId);
        return Result.success();
    }

    @GetMapping("/{groupId}/members")
    @Operation(summary = "群成员列表", description = "获取群聊的成员列表")
    public Result<PageResult> getGroupMembers(
            @PathVariable("groupId") Long groupId,
            @RequestParam(name = "page", defaultValue = "1") Long page,
            @RequestParam(name = "pageSize", defaultValue = "50") Long pageSize) {
        return Result.success(chatService.getGroupMembers(groupId, page, pageSize));
    }

    @PostMapping("/{groupId}/message/send")
    @Operation(summary = "发送群消息", description = "在群聊中发送消息")
    public Result<GroupMessageVO> sendGroupMessage(
            @PathVariable("groupId") Long groupId,
            @RequestBody GroupMessageSendDTO dto) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(chatService.sendGroupMessage(userId, groupId, dto.getContent()));
    }

    @GetMapping("/{groupId}/message/list")
    @Operation(summary = "群消息记录", description = "获取群聊的消息记录")
    public Result<PageResult> getGroupMessageList(
            @PathVariable("groupId") Long groupId,
            @RequestParam(name = "page", defaultValue = "1") Long page,
            @RequestParam(name = "pageSize", defaultValue = "20") Long pageSize) {
        return Result.success(chatService.getGroupMessageList(groupId, page, pageSize));
    }

    @PostMapping("/{groupId}/invite")
    @Operation(summary = "邀请好友进群", description = "邀请好友加入群聊")
    public Result<Void> inviteToGroup(
            @PathVariable("groupId") Long groupId,
            @RequestBody Map<String, Long> body) {
        Long userId = BaseContext.getCurrentId();
        Long inviteUserId = body.get("userId");
        chatService.inviteToGroup(userId, groupId, inviteUserId);
        return Result.success();
    }

    @GetMapping("/invites")
    @Operation(summary = "待处理邀请", description = "获取待处理的群邀请列表")
    public Result<PageResult> getPendingInvites(
            @RequestParam(name = "page", defaultValue = "1") Long page,
            @RequestParam(name = "pageSize", defaultValue = "20") Long pageSize) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(chatService.getPendingInvites(userId, page, pageSize));
    }

    @PostMapping("/invite/accept")
    @Operation(summary = "接受邀请", description = "接受群邀请")
    public Result<Void> acceptInvite(@RequestBody Map<String, Long> body) {
        Long userId = BaseContext.getCurrentId();
        Long inviteId = body.get("inviteId");
        chatService.acceptInvite(userId, inviteId);
        return Result.success();
    }

    @PostMapping("/invite/reject")
    @Operation(summary = "拒绝邀请", description = "拒绝群邀请")
    public Result<Void> rejectInvite(@RequestBody Map<String, Long> body) {
        Long userId = BaseContext.getCurrentId();
        Long inviteId = body.get("inviteId");
        chatService.rejectInvite(userId, inviteId);
        return Result.success();
    }

    @GetMapping("/invite/count")
    @Operation(summary = "邀请数量", description = "获取待处理邀请数量")
    public Result<Integer> getInviteCount() {
        Long userId = BaseContext.getCurrentId();
        return Result.success(chatService.getInviteCount(userId));
    }
}