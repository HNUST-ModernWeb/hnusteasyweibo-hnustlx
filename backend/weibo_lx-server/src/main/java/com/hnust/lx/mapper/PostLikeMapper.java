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

    @Select("SELECT pl.like_id as likeId, pl.post_id as postId, pl.user_id as userId, pl.like_time as likeTime " +
            "FROM post_like pl JOIN user u ON pl.user_id = u.user_id " +
            "WHERE pl.post_id = #{postId} AND u.is_deleted = 0")
    List<PostLike> findByPostId(Long postId);

    @Select("SELECT pl.like_id as likeId, pl.post_id as postId, pl.user_id as userId, pl.like_time as likeTime " +
            "FROM post_like pl JOIN user u ON pl.user_id = u.user_id " +
            "WHERE pl.user_id = #{userId} AND u.is_deleted = 0")
    List<PostLike> findByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM post_like WHERE post_id = #{postId}")
    long countByPostId(Long postId);

    @Select("SELECT EXISTS(SELECT 1 FROM post_like WHERE post_id = #{postId} AND user_id = #{userId})")
    boolean exists(@Param("postId") Long postId, @Param("userId") Long userId);

    @Select("<script>" +
            "SELECT pl.like_id as likeId, pl.post_id as postId, pl.user_id as userId, pl.like_time as likeTime " +
            "FROM post_like pl JOIN user u ON pl.user_id = u.user_id " +
            "WHERE pl.post_id IN " +
            "<foreach collection='postIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND u.is_deleted = 0 ORDER BY pl.like_time DESC" +
            "</script>")
    List<PostLike> findByPostIds(@Param("postIds") List<Long> postIds);
}
