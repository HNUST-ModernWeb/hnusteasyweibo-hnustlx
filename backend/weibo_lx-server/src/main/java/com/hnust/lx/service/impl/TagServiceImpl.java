package com.hnust.lx.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hnust.lx.constant.MessageConstant;
import com.hnust.lx.dto.*;
import com.hnust.lx.entity.Tag;
import com.hnust.lx.exception.BaseException;
import com.hnust.lx.mapper.PostTagMapper;
import com.hnust.lx.mapper.TagMapper;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.service.TagService;
import com.hnust.lx.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final PostTagMapper postTagMapper;

    @Override
    public TagVO addTag(TagAddDTO dto) {
        Tag existTag = tagMapper.findByName(dto.getTagName());
        if (existTag != null) {
            throw new BaseException(MessageConstant.TAG_NAME_EXISTS);
        }

        Tag tag = Tag.builder()
                .tagName(dto.getTagName())
                .isDeleted(0)
                .build();
        tagMapper.insert(tag);
        
        return TagVO.builder()
                .tagId(tag.getTagId())
                .tagName(tag.getTagName())
                .postCount(0)
                .build();
    }

    @Override
    public PageResult getTagList(Long page, Long pageSize) {
        PageHelper.startPage(page.intValue(), pageSize.intValue());
        List<Tag> tags = tagMapper.findByPage();
        PageInfo<Tag> pageInfo = new PageInfo<>(tags);
        
        List<TagVO> voList = tags.stream()
                .map(tag -> TagVO.builder()
                        .tagId(tag.getTagId())
                        .tagName(tag.getTagName())
                        .postCount(0)
                        .build())
                .collect(Collectors.toList());
        
        return new PageResult(pageInfo.getTotal(), voList);
    }

    @Override
    public void deleteTag(TagDeleteDTO dto) {
        tagMapper.delete(dto.getTagId());
    }

    @Override
    @Cacheable(value = "hotTags", key = "'15'")
    public List<TagVO> getHotTags(Integer limit) {
        List<PostTagMapper.TagCount> tagCounts = postTagMapper.countByTagId(limit);
        
        return tagCounts.stream()
                .map(tc -> {
                    Tag tag = tagMapper.findById(tc.getTagId());
                    return TagVO.builder()
                            .tagId(tc.getTagId())
                            .tagName(tag != null ? tag.getTagName() : null)
                            .postCount(Math.toIntExact(tc.getPostCount()))
                            .build();
                })
                .collect(Collectors.toList());
    }
}
