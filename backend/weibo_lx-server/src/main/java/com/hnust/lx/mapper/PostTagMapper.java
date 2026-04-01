package com.hnust.lx.mapper;

import com.hnust.lx.entity.PostTag;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostTagMapper {

    @Insert("INSERT INTO post_tag(post_id, tag_id) VALUES(#{postId}, #{tagId})")
    @Options(useGeneratedKeys = true, keyProperty = "ptId")
    int insert(PostTag postTag);

    @Insert("<script>" +
            "INSERT INTO post_tag(post_id, tag_id) VALUES " +
            "<foreach collection='tagIds' item='tagId' separator=','>(#{postId}, #{tagId})</foreach>" +
            "</script>")
    int batchInsert(@Param("postId") Long postId, @Param("tagIds") List<Long> tagIds);

    @Delete("DELETE FROM post_tag WHERE post_id = #{postId} AND tag_id = #{tagId}")
    int delete(@Param("postId") Long postId, @Param("tagId") Long tagId);

    @Delete("DELETE FROM post_tag WHERE post_id = #{postId}")
    int deleteByPostId(Long postId);

    @Select("SELECT pt_id as ptId, post_id as postId, tag_id as tagId FROM post_tag WHERE post_id = #{postId}")
    List<PostTag> findByPostId(Long postId);

    @Select("SELECT pt_id as ptId, post_id as postId, tag_id as tagId FROM post_tag WHERE tag_id = #{tagId}")
    List<PostTag> findByTagId(Long tagId);

    @Select("SELECT EXISTS(SELECT 1 FROM post_tag WHERE post_id = #{postId} AND tag_id = #{tagId})")
    boolean exists(@Param("postId") Long postId, @Param("tagId") Long tagId);
}
