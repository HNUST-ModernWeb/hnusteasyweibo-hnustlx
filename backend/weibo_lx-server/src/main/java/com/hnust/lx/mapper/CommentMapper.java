package com.hnust.lx.mapper;

import com.hnust.lx.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Insert("INSERT INTO comment(post_id, user_id, content, comment_time, is_deleted) " +
            "VALUES(#{postId}, #{userId}, #{content}, DEFAULT, #{isDeleted})")
    @Options(useGeneratedKeys = true, keyProperty = "commentId")
    int insert(Comment comment);

    @Select("SELECT c.comment_id as commentId, c.post_id as postId, c.user_id as userId, c.content, " +
            "c.comment_time as commentTime, c.is_deleted as isDeleted FROM comment c " +
            "JOIN user u ON c.user_id = u.user_id " +
            "WHERE c.post_id = #{postId} AND c.is_deleted = 0 AND u.is_deleted = 0 ORDER BY c.comment_time DESC")
    List<Comment> findByPostId(Long postId);

    @Select("SELECT c.comment_id as commentId, c.post_id as postId, c.user_id as userId, c.content, " +
            "c.comment_time as commentTime, c.is_deleted as isDeleted FROM comment c " +
            "JOIN user u ON c.user_id = u.user_id " +
            "WHERE c.post_id = #{postId} AND c.is_deleted = 0 AND u.is_deleted = 0 ORDER BY c.comment_time DESC")
    List<Comment> findByPostIdPage(Long postId);

    @Select("SELECT comment_id as commentId, post_id as postId, user_id as userId, content, " +
            "comment_time as commentTime, is_deleted as isDeleted FROM comment " +
            "WHERE comment_id = #{commentId}")
    Comment findById(Long commentId);

    @Update("UPDATE comment SET is_deleted = 1 WHERE comment_id = #{commentId}")
    int delete(Long commentId);

    @Select("SELECT COUNT(*) FROM comment WHERE post_id = #{postId} AND is_deleted = 0")
    long countByPostId(Long postId);

    @Select("<script>" +
            "SELECT c.comment_id as commentId, c.post_id as postId, c.user_id as userId, c.content, " +
            "c.comment_time as commentTime, c.is_deleted as isDeleted FROM comment c " +
            "JOIN user u ON c.user_id = u.user_id " +
            "WHERE c.post_id IN " +
            "<foreach collection='postIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND c.is_deleted = 0 AND u.is_deleted = 0 ORDER BY c.comment_time DESC" +
            "</script>")
    List<Comment> findByPostIds(@Param("postIds") List<Long> postIds);
}
