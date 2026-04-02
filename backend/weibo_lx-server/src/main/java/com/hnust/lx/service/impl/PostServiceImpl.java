package com.hnust.lx.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hnust.lx.constant.MessageConstant;
import com.hnust.lx.dto.*;
import com.hnust.lx.entity.Post;
import com.hnust.lx.entity.User;
import com.hnust.lx.exception.BaseException;
import com.hnust.lx.exception.ResourceNotFoundException;
import com.hnust.lx.mapper.PostMapper;
import com.hnust.lx.mapper.UserMapper;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.service.PostService;
import com.hnust.lx.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;

    @Override
    public PostVO addPost(PostAddDTO dto) {
        Post post = Post.builder()
                .userId(dto.getUserId())
                .content(dto.getContent())
                .commentCount(0)
                .likeCount(0)
                .isDeleted(0)
                .build();
        postMapper.insert(post);
        return buildPostVO(post);
    }

    @Override
    public PageResult getPostList(Long page, Long pageSize) {
        PageHelper.startPage(page.intValue(), pageSize.intValue());
        List<Post> posts = postMapper.findByPage();
        PageInfo<Post> pageInfo = new PageInfo<>(posts);
        
        List<PostVO> voList = posts.stream().map(this::buildPostVO).collect(Collectors.toList());
        
        return new PageResult(pageInfo.getTotal(), voList);
    }

    @Override
    public PostVO getPostDetail(Long postId) {
        Post post = postMapper.findById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new ResourceNotFoundException(MessageConstant.POST_NOT_FOUND);
        }
        return buildPostVO(post);
    }

    @Override
    public PostVO updatePost(Long userId, PostUpdateDTO dto) {
        Post post = postMapper.findById(dto.getPostId());
        if (post == null || post.getIsDeleted() == 1) {
            throw new ResourceNotFoundException(MessageConstant.POST_NOT_FOUND);
        }
        if (!post.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.OPERATION_DENIED);
        }
        
        Post updatePost = Post.builder()
                .postId(dto.getPostId())
                .content(dto.getContent())
                .build();
        postMapper.update(updatePost);
        return getPostDetail(dto.getPostId());
    }

    @Override
    public void deletePost(Long userId, Long postId) {
        Post post = postMapper.findById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new ResourceNotFoundException(MessageConstant.POST_NOT_FOUND);
        }
        if (!post.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.OPERATION_DENIED);
        }
        postMapper.delete(postId);
    }

    @Override
    public Post getPostById(Long postId) {
        return postMapper.findById(postId);
    }

    @Override
    public List<Post> getUserPosts(Long userId) {
        return postMapper.findByUserId(userId);
    }

    @Override
    public Long getUserTotalLikes(Long userId) {
        List<Post> userPosts = postMapper.findByUserId(userId);
        return userPosts.stream()
                .mapToLong(p -> p.getLikeCount() != null ? p.getLikeCount() : 0)
                .sum();
    }

    private PostVO buildPostVO(Post post) {
        User user = userMapper.findById(post.getUserId());
        return PostVO.builder()
                .postId(post.getPostId())
                .userId(post.getUserId())
                .username(user != null ? user.getUsername() : null)
                .avatar(user != null ? user.getAvatar() : null)
                .content(post.getContent())
                .likeCount(Long.valueOf(post.getLikeCount()))
                .commentCount(Long.valueOf(post.getCommentCount()))
                .createTime(post.getPostTime())
                .status((long) (post.getIsDeleted() == 0 ? 1 : 0))
                .build();
    }
}
