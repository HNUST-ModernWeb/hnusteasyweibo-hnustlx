package com.hnust.lx.service;

import com.hnust.lx.dto.*;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.vo.CommentVO;

public interface CommentService {

    CommentVO addComment(CommentAddDTO dto);

    PageResult getCommentList(Long postId, Long page, Long pageSize);

    void deleteComment(Long userId, CommentDeleteDTO dto);
}
