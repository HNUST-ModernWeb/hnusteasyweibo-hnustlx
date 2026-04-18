package com.hnust.lx.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hnust.lx.constant.MessageConstant;
import com.hnust.lx.dto.*;
import com.hnust.lx.entity.Comment;
import com.hnust.lx.entity.Post;
import com.hnust.lx.entity.User;
import com.hnust.lx.exception.BaseException;
import com.hnust.lx.mapper.CommentMapper;
import com.hnust.lx.mapper.PostMapper;
import com.hnust.lx.mapper.UserMapper;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.service.CommentService;
import com.hnust.lx.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public CommentVO addComment(CommentAddDTO dto) {
        Post post = postMapper.findById(dto.getPostId());
        if (post == null || post.getIsDeleted() == 1) {
            throw new BaseException(MessageConstant.POST_NOT_FOUND);
        }

        Comment comment = Comment.builder()
                .postId(dto.getPostId())
                .userId(dto.getUserId())
                .content(dto.getContent())
                .isDeleted(0L)
                .build();
        commentMapper.insert(comment);
        
        postMapper.updateCommentCount(dto.getPostId(), 1);
        
        return buildCommentVO(comment);
    }

    @Override
    public PageResult getCommentList(Long postId, Long page, Long pageSize) {
        PageHelper.startPage(page.intValue(), pageSize.intValue());
        List<Comment> comments = commentMapper.findByPostIdPage(postId);
        PageInfo<Comment> pageInfo = new PageInfo<>(comments);
        
        List<CommentVO> voList = comments.stream().map(this::buildCommentVO).collect(Collectors.toList());
        
        return new PageResult(pageInfo.getTotal(), voList);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, CommentDeleteDTO dto) {
        Comment comment = commentMapper.findById(dto.getCommentId());
        if (comment == null) {
            throw new BaseException(MessageConstant.COMMENT_NOT_FOUND);
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.OPERATION_DENIED);
        }
        if (comment.getPostId() != null) {
            postMapper.updateCommentCount(comment.getPostId(), -1);
        }
        commentMapper.delete(dto.getCommentId());
    }

    @Override
    public PageResult getReceivedComments(Long userId, Long page, Long pageSize) {
        List<Long> postIds = postMapper.findPostIdsByUserId(userId);
        if (postIds == null || postIds.isEmpty()) {
            return new PageResult(0, List.of());
        }

        PageHelper.startPage(page.intValue(), pageSize.intValue());
        List<Comment> comments = commentMapper.findByPostIds(postIds);
        PageInfo<Comment> pageInfo = new PageInfo<>(comments);

        List<CommentVO> voList = comments.stream().map(this::buildCommentVO).collect(Collectors.toList());

        return new PageResult(pageInfo.getTotal(), voList);
    }

    private CommentVO buildCommentVO(Comment comment) {
        User user = userMapper.findById(comment.getUserId());
        Post post = postMapper.findById(comment.getPostId());
        return CommentVO.builder()
                .commentId(comment.getCommentId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .username(user != null ? user.getUsername() : null)
                .avatar(user != null ? user.getAvatar() : null)
                .content(comment.getContent())
                .postContent(post != null ? post.getContent() : null)
                .createTime(comment.getCommentTime())
                .status(comment.getIsDeleted() == 0 ? 1 : 0)
                .build();
    }
}
