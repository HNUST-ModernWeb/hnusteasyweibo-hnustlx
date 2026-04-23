package com.hnust.lx.controller;

import com.hnust.lx.entity.Post;
import com.hnust.lx.entity.User;
import com.hnust.lx.mapper.PostMapper;
import com.hnust.lx.mapper.UserMapper;
import com.hnust.lx.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "搜索", description = "搜索用户和帖子")
public class SearchController {

    private final UserMapper userMapper;
    private final PostMapper postMapper;

    @GetMapping("/user")
    @Operation(summary = "搜索用户", description = "根据关键词搜索用户")
    public Result<List<User>> searchUsers(
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(name = "limit", defaultValue = "20") Integer limit) {
        List<User> users = userMapper.searchByKeyword(keyword, limit);
        return Result.success(users);
    }

    @GetMapping("/post")
    @Operation(summary = "搜索帖子", description = "根据关键词搜索帖子")
    public Result<List<Post>> searchPosts(
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(name = "limit", defaultValue = "20") Integer limit) {
        List<Post> posts = postMapper.searchByKeyword(keyword, limit);
        return Result.success(posts);
    }
}
