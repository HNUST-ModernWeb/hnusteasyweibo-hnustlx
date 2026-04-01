package com.hnust.lx.mapper;

import com.hnust.lx.entity.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostMapper {

    @Insert("INSERT INTO post(user_id, content, post_time, comment_count, like_count, is_deleted) " +
            "VALUES(#{userId}, #{content}, DEFAULT, #{commentCount}, #{likeCount}, #{isDeleted})")
    @Options(useGeneratedKeys = true, keyProperty = "postId")
    int insert(Post post);

    @Select("SELECT post_id as postId, user_id as userId, content, post_time as postTime, " +
            "comment_count as commentCount, like_count as likeCount, is_deleted as isDeleted " +
            "FROM post WHERE is_deleted = 0 ORDER BY post_time DESC")
    List<Post> findAll();

    @Select("SELECT post_id as postId, user_id as userId, content, post_time as postTime, " +
            "comment_count as commentCount, like_count as likeCount, is_deleted as isDeleted " +
            "FROM post WHERE is_deleted = 0 ORDER BY post_time DESC")
    List<Post> findByPage();

    @Select("SELECT post_id as postId, user_id as userId, content, post_time as postTime, " +
            "comment_count as commentCount, like_count as likeCount, is_deleted as isDeleted " +
            "FROM post WHERE post_id = #{postId}")
    Post findById(Long postId);

    @Select("SELECT post_id as postId, user_id as userId, content, post_time as postTime, " +
            "comment_count as commentCount, like_count as likeCount, is_deleted as isDeleted " +
            "FROM post WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY post_time DESC")
    List<Post> findByUserId(Long userId);

    @Update("UPDATE post SET content = #{content} WHERE post_id = #{postId}")
    int update(Post post);

    @Update("UPDATE post SET is_deleted = 1 WHERE post_id = #{postId}")
    int delete(Long postId);

    @Update("UPDATE post SET comment_count = comment_count + #{count} WHERE post_id = #{postId}")
    int updateCommentCount(@Param("postId") Long postId, @Param("count") Integer count);

    @Update("UPDATE post SET like_count = like_count + #{count} WHERE post_id = #{postId}")
    int updateLikeCount(@Param("postId") Long postId, @Param("count") Integer count);

    @Select("SELECT COUNT(*) FROM post WHERE is_deleted = 0")
    long count();
}
