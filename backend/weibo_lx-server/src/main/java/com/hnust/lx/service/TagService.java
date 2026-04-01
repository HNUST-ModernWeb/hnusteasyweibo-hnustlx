package com.hnust.lx.service;

import com.hnust.lx.dto.*;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.vo.TagVO;

public interface TagService {

    TagVO addTag(TagAddDTO dto);

    PageResult getTagList(Long page, Long pageSize);

    void deleteTag(TagDeleteDTO dto);
}
