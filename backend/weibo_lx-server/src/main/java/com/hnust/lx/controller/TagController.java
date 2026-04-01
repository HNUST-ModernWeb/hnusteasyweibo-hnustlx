package com.hnust.lx.controller;

import com.hnust.lx.dto.*;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.result.Result;
import com.hnust.lx.service.TagService;
import com.hnust.lx.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
@Tag(name = "标签管理", description = "帖子标签相关接口")
public class TagController {

    private final TagService tagService;

    @PostMapping("/add")
    @Operation(summary = "添加标签", description = "创建新的标签")
    public Result<TagVO> addTag(@RequestBody TagAddDTO dto) {
        return Result.success(tagService.addTag(dto));
    }

    @GetMapping("/list")
    @Operation(summary = "获取标签列表", description = "分页获取所有标签")
    public Result<PageResult> getTagList(
            @RequestParam(name = "page", defaultValue = "1") Long page,
            @RequestParam(name = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.success(tagService.getTagList(page, pageSize));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除标签", description = "删除指定标签")
    public Result<Void> deleteTag(@RequestBody TagDeleteDTO dto) {
        tagService.deleteTag(dto);
        return Result.success();
    }
}
