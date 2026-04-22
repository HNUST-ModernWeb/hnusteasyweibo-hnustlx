package com.hnust.lx.controller;

import com.hnust.lx.context.BaseContext;
import com.hnust.lx.dto.*;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.result.Result;
import com.hnust.lx.service.PostService;
import com.hnust.lx.vo.PostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@Tag(name = "微博帖子", description = "帖子相关接口")
public class PostController {

    private final PostService postService;

    @PostMapping("/add")
    @Operation(summary = "发布帖子", description = "发布新的微博帖子")
    public Result<PostVO> addPost(@RequestBody PostAddDTO dto) {
        Long userId = BaseContext.getCurrentId();
        dto.setUserId(userId);
        return Result.success(postService.addPost(dto));
    }

    @PostMapping("/image")
    @Operation(summary = "上传帖子图片", description = "上传帖子图片并返回可访问URL")
    public Result<String> uploadPostImage(@RequestParam("image") MultipartFile file) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(postService.uploadPostImage(userId, file));
    }

    @GetMapping("/list")
    @Operation(summary = "获取帖子列表", description = "分页获取所有帖子")
    public Result<PageResult> getPostList(
            @RequestParam(name = "page", defaultValue = "1") Long page,
            @RequestParam(name = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.success(postService.getPostList(page, pageSize));
    }

    @GetMapping("/detail/{postId}")
    @Operation(summary = "获取帖子详情", description = "根据postId获取帖子详情")
    public Result<PostVO> getPostDetail(@PathVariable(name = "postId") Long postId) {
        return Result.success(postService.getPostDetail(postId));
    }

    @PutMapping("/update")
    @Operation(summary = "更新帖子", description = "更新帖子内容")
    public Result<PostVO> updatePost(@RequestBody PostUpdateDTO dto) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(postService.updatePost(userId, dto));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除帖子", description = "删除指定帖子")
    public Result<Void> deletePost(@RequestBody PostDeleteDTO dto) {
        Long userId = BaseContext.getCurrentId();
        postService.deletePost(userId, dto.getPostId());
        return Result.success();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户帖子", description = "获取指定用户的所有帖子")
    public Result<Long> getUserTotalLikes(@PathVariable(name = "userId") Long userId) {
        return Result.success(postService.getUserTotalLikes(userId));
    }
}
