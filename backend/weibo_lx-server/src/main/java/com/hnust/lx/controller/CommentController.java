package com.hnust.lx.controller;

import com.hnust.lx.context.BaseContext;
import com.hnust.lx.dto.*;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.result.Result;
import com.hnust.lx.service.CommentService;
import com.hnust.lx.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Tag(name = "评论管理", description = "帖子评论相关接口")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/add")
    @Operation(summary = "添加评论", description = "为帖子添加评论")
    public Result<CommentVO> addComment(@RequestBody CommentAddDTO dto) {
        Long userId = BaseContext.getCurrentId();
        dto.setUserId(userId);
        return Result.success(commentService.addComment(dto));
    }

    @GetMapping("/list/{postId}")
    @Operation(summary = "获取评论列表", description = "分页获取指定帖子的评论")
    public Result<PageResult> getCommentList(
            @PathVariable(name = "postId") Long postId,
            @RequestParam(name = "page", defaultValue = "1") Long page,
            @RequestParam(name = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.success(commentService.getCommentList(postId, page, pageSize));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除评论", description = "删除指定评论")
    public Result<Void> deleteComment(@RequestBody CommentDeleteDTO dto) {
        Long userId = BaseContext.getCurrentId();
        commentService.deleteComment(userId, dto);
        return Result.success();
    }

    @GetMapping("/received")
    @Operation(summary = "获取我收到的评论", description = "分页获取别人评论我的帖子的列表")
    public Result<PageResult> getReceivedComments(
            @RequestParam(name = "page", defaultValue = "1") Long page,
            @RequestParam(name = "pageSize", defaultValue = "10") Long pageSize) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(commentService.getReceivedComments(userId, page, pageSize));
    }
}
