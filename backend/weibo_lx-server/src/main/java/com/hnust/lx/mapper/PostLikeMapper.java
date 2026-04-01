package com.hnust.lx.mapper;

import com.hnust.lx.entity.PostLike;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostLikeMapper {

    @Insert("INSERT INTO post_like(post_id, user_id, like_time) VALUES(#{postId}, #{userId}, DEFAULT)")
    @Options(useGeneratedKeys = true, keyProperty = "likeId")
    int insert(PostLike postLike);

    @Delete("DELETE FROM post_like WHERE post_id = #{postId} AND user_id = #{userId}")
    int delete(@Param("postId") Long postId, @Param("userId") Long userId);

    @Select("SELECT like_id as likeId, post_id as postId, user_id as userId, like_time as likeTime " +
            "FROM post_like WHERE post_id = #{postId} AND user_id = #{userId}")
    PostLike findByUserAndPost(@Param("postId") Long postId, @Param("userId") Long userId);

    @Select("SELECT like_id as likeId, post_id as postId, user_id as userId, like_time as likeTime " +
            "FROM post_like WHERE post_id = #{postId}")
    List<PostLike> findByPostId(Long postId);

    @Select("SELECT like_id as likeId, post_id as postId, user_id as userId, like_time as likeTime " +
            "FROM post_like WHERE user_id = #{userId}")
    List<PostLike> findByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM post_like WHERE post_id = #{postId}")
    long countByPostId(Long postId);

    @Select("SELECT EXISTS(SELECT 1 FROM post_like WHERE post_id = #{postId} AND user_id = #{userId})")
    boolean exists(@Param("postId") Long postId, @Param("userId") Long userId);
}
