package com.hnust.lx.service;

import com.hnust.lx.dto.*;
import com.hnust.lx.vo.TagVO;

import java.util.List;

public interface PostTagService {

    List<TagVO> addPostTags(PostTagAddDTO dto);

    List<TagVO> getPostTags(Long postId);

    void deletePostTag(PostTagDeleteDTO dto);
}
