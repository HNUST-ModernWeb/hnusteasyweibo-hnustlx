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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@Tag(name = "私信聊天", description = "私信聊天相关接口")
public class PrivateMessageController {

    private final ChatService chatService;

    @PostMapping("/private/send")
    @Operation(summary = "发送私信", description = "发送私信给指定用户")
    public Result<PrivateMessageVO> sendMessage(@RequestBody PrivateMessageSendDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId.equals(dto.getReceiverId())) {
            return Result.error("不能给自己发消息");
        }
        return Result.success(chatService.sendPrivateMessage(userId, dto));
    }

    @GetMapping("/private/list/{userId}")
    @Operation(summary = "获取私聊记录", description = "获取与指定用户的私聊记录")
    public Result<PageResult> getMessageList(
            @PathVariable("userId") Long otherUserId,
            @RequestParam(name = "page", defaultValue = "1") Long page,
            @RequestParam(name = "pageSize", defaultValue = "20") Long pageSize) {
        Long currentUserId = BaseContext.getCurrentId();
        return Result.success(chatService.getPrivateMessageList(currentUserId, otherUserId, page, pageSize));
    }

    @GetMapping("/private/conversations")
    @Operation(summary = "私聊会话列表", description = "获取私聊会话列表")
    public Result<PageResult> getConversations(
            @RequestParam(name = "page", defaultValue = "1") Long page,
            @RequestParam(name = "pageSize", defaultValue = "10") Long pageSize) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(chatService.getConversationList(userId, page, pageSize));
    }

    @GetMapping("/private/unread-count")
    @Operation(summary = "未读私信数", description = "获取未读私信总数")
    public Result<Integer> getUnreadCount() {
        Long userId = BaseContext.getCurrentId();
        return Result.success(chatService.getUnreadCount(userId));
    }

    @PutMapping("/private/mark-read")
    @Operation(summary = "标记已读", description = "标记指定用户发来的消息为已读")
    public Result<Integer> markAsRead(@RequestBody MarkReadDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        return Result.success(chatService.markAsRead(currentUserId, dto.getUserId()));
    }
}