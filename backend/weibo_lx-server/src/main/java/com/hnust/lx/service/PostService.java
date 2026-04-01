package com.hnust.lx.service;

import com.hnust.lx.dto.*;
import com.hnust.lx.entity.Post;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.vo.PostVO;

public interface PostService {

    PostVO addPost(PostAddDTO dto);

    PageResult getPostList(Long page, Long pageSize);

    PostVO getPostDetail(Long postId);

    PostVO updatePost(Long userId, PostUpdateDTO dto);

    void deletePost(Long userId, Long postId);

    Post getPostById(Long postId);
}
