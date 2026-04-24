package com.hnust.lx.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hnust.lx.constant.MessageConstant;
import com.hnust.lx.dto.*;
import com.hnust.lx.entity.Post;
import com.hnust.lx.entity.PostLike;
import com.hnust.lx.entity.User;
import com.hnust.lx.exception.BaseException;
import com.hnust.lx.mapper.PostLikeMapper;
import com.hnust.lx.mapper.PostMapper;
import com.hnust.lx.mapper.UserMapper;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.service.LikeService;
import com.hnust.lx.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final PostLikeMapper postLikeMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final WebSocketNotificationService notificationService;

    @Override
    @CacheEvict(value = {"hotUsers"}, allEntries = true)
    @Transactional
    public boolean toggleLike(LikeToggleDTO dto) {
        Post post = postMapper.findById(dto.getPostId());
        if (post == null || post.getIsDeleted() == 1) {
            throw new BaseException(MessageConstant.POST_NOT_FOUND);
        }

        boolean exists = postLikeMapper.exists(dto.getPostId(), dto.getUserId());
        
        if (exists) {
            postLikeMapper.delete(dto.getPostId(), dto.getUserId());
            postMapper.updateLikeCount(dto.getPostId(), -1);
            notificationService.broadcastPostUpdate(Map.of("type", "like_update", "postId", dto.getPostId()));
            return false;
        } else {
            PostLike postLike = PostLike.builder()
                    .postId(dto.getPostId())
                    .userId(dto.getUserId())
                    .build();
            postLikeMapper.insert(postLike);
            postMapper.updateLikeCount(dto.getPostId(), 1);
            
            Map<String, Object> likeData = new HashMap<>();
            likeData.put("postId", dto.getPostId());
            likeData.put("userId", dto.getUserId());
            User liker = userMapper.findById(dto.getUserId());
            if (liker != null) {
                likeData.put("username", liker.getUsername());
                likeData.put("avatar", liker.getAvatar());
            }
            
            if (!dto.getUserId().equals(post.getUserId())) {
                notificationService.notifyNewLike(post.getUserId(), likeData);
            }
            notificationService.broadcastPostUpdate(Map.of("type", "like_update", "postId", dto.getPostId()));
            return true;
        }
    }

    @Override
    public PageResult getMyLikes(Long userId, Long page, Long pageSize) {
        PageHelper.startPage(page.intValue(), pageSize.intValue());
        List<PostLike> likes = postLikeMapper.findByUserId(userId);
        PageInfo<PostLike> pageInfo = new PageInfo<>(likes);
        
        List<Object> voList = likes.stream().map(like -> {
            Post post = postMapper.findById(like.getPostId());
            User user = userMapper.findById(like.getUserId());
            Map<String, Object> map = new HashMap<>();
            map.put("likeId", like.getLikeId());
            map.put("userId", like.getUserId());
            map.put("postId", like.getPostId());
            map.put("postContent", post != null ? post.getContent() : null);
            map.put("username", user != null ? user.getUsername() : null);
            map.put("avatar", user != null ? user.getAvatar() : null);
            map.put("createTime", like.getLikeTime());
            map.put("status", 1);
            return map;
        }).collect(Collectors.toList());
        
        return new PageResult(pageInfo.getTotal(), voList);
    }

    @Override
    public PageResult getPostLikes(Long postId, Long page, Long pageSize) {
        PageHelper.startPage(page.intValue(), pageSize.intValue());
        List<PostLike> likes = postLikeMapper.findByPostId(postId);
        PageInfo<PostLike> pageInfo = new PageInfo<>(likes);
        
        List<Object> voList = likes.stream().map(like -> {
            User user = userMapper.findById(like.getUserId());
            Map<String, Object> map = new HashMap<>();
            map.put("likeId", like.getLikeId());
            map.put("userId", like.getUserId());
            map.put("username", user != null ? user.getUsername() : null);
            map.put("avatar", user != null ? user.getAvatar() : null);
            map.put("createTime", like.getLikeTime());
            return map;
        }).collect(Collectors.toList());
        
        return new PageResult(pageInfo.getTotal(), voList);
    }

    @Override
    public PageResult getReceivedLikes(Long userId, Long page, Long pageSize) {
        List<Long> postIds = postMapper.findPostIdsByUserId(userId);
        if (postIds == null || postIds.isEmpty()) {
            return new PageResult(0, List.of());
        }

        PageHelper.startPage(page.intValue(), pageSize.intValue());
        List<PostLike> likes = postLikeMapper.findByPostIds(postIds);
        PageInfo<PostLike> pageInfo = new PageInfo<>(likes);

        List<Object> voList = likes.stream().map(like -> {
            User user = userMapper.findById(like.getUserId());
            Post post = postMapper.findById(like.getPostId());
            Map<String, Object> map = new HashMap<>();
            map.put("likeId", like.getLikeId());
            map.put("userId", like.getUserId());
            map.put("username", user != null ? user.getUsername() : null);
            map.put("avatar", user != null ? user.getAvatar() : null);
            map.put("postId", like.getPostId());
            map.put("postContent", post != null ? post.getContent() : null);
            map.put("createTime", like.getLikeTime());
            return map;
        }).collect(Collectors.toList());

        return new PageResult(pageInfo.getTotal(), voList);
    }
}
