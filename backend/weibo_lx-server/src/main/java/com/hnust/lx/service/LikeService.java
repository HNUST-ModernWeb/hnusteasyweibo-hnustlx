package com.hnust.lx.service;

import com.hnust.lx.dto.*;
import com.hnust.lx.result.PageResult;

public interface LikeService {

    boolean toggleLike(LikeToggleDTO dto);

    PageResult getMyLikes(Long userId, Long page, Long pageSize);

    PageResult getPostLikes(Long postId, Long page, Long pageSize);

    PageResult getReceivedLikes(Long userId, Long page, Long pageSize);
}
