package com.hnust.lx.service.impl;

import com.hnust.lx.constant.MessageConstant;
import com.hnust.lx.dto.*;
import com.hnust.lx.entity.Post;
import com.hnust.lx.entity.PostTag;
import com.hnust.lx.entity.Tag;
import com.hnust.lx.exception.BaseException;
import com.hnust.lx.exception.ResourceNotFoundException;
import com.hnust.lx.mapper.PostMapper;
import com.hnust.lx.mapper.PostTagMapper;
import com.hnust.lx.mapper.TagMapper;
import com.hnust.lx.service.PostTagService;
import com.hnust.lx.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostTagServiceImpl implements PostTagService {

    private final PostTagMapper postTagMapper;
    private final PostMapper postMapper;
    private final TagMapper tagMapper;

    @Override
    @Transactional
    public List<TagVO> addPostTags(PostTagAddDTO dto) {
        Post post = postMapper.findById(dto.getPostId());
        if (post == null || post.getIsDeleted() == 1) {
            throw new BaseException(MessageConstant.POST_NOT_FOUND);
        }

        if (dto.getUserId() != null && !dto.getUserId().equals(post.getUserId())) {
            throw new BaseException(MessageConstant.OPERATION_DENIED);
        }

        postTagMapper.deleteByPostId(dto.getPostId());
        
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            postTagMapper.batchInsert(dto.getPostId(), dto.getTagIds());
        }

        return getPostTags(dto.getPostId());
    }

    @Override
    public List<TagVO> getPostTags(Long postId) {
        List<PostTag> postTags = postTagMapper.findByPostId(postId);
        
        List<TagVO> voList = new ArrayList<>();
        for (PostTag pt : postTags) {
            Tag tag = tagMapper.findById(pt.getTagId());
            if (tag != null && tag.getIsDeleted() == 0) {
                voList.add(TagVO.builder()
                        .tagId(tag.getTagId())
                        .tagName(tag.getTagName())
                        .build());
            }
        }
        
        return voList;
    }

    @Override
    @Transactional
    public void deletePostTag(PostTagDeleteDTO dto) {
        postTagMapper.delete(dto.getPostId(), dto.getTagId());
    }
}
