package com.hnust.lx.controller;

import com.hnust.lx.dto.*;
import com.hnust.lx.result.Result;
import com.hnust.lx.service.PostTagService;
import com.hnust.lx.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post-tag")
@RequiredArgsConstructor
@Tag(name = "帖子标签关联", description = "帖子与标签关联管理")
public class PostTagController {

    private final PostTagService postTagService;

    @PostMapping("/add")
    @Operation(summary = "为帖子添加标签", description = "为帖子添加一个或多个标签")
    public Result<List<TagVO>> addPostTags(@RequestBody PostTagAddDTO dto) {
        return Result.success(postTagService.addPostTags(dto));
    }

    @GetMapping("/list/{postId}")
    @Operation(summary = "获取帖子标签", description = "获取指定帖子的所有标签")
    public Result<List<TagVO>> getPostTags(@PathVariable(name = "postId") Long postId) {
        return Result.success(postTagService.getPostTags(postId));
    }

    @PostMapping("/delete")
    @Operation(summary = "移除帖子标签", description = "移除帖子的指定标签")
    public Result<Void> deletePostTag(@RequestBody PostTagDeleteDTO dto) {
        postTagService.deletePostTag(dto);
        return Result.success();
    }
}
