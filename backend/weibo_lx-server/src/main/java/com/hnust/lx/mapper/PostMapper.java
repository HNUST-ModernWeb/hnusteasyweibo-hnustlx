package com.hnust.lx.mapper;

import com.hnust.lx.entity.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostMapper {

    @Insert("INSERT INTO post(user_id, content, images, post_time, comment_count, like_count, is_deleted) " +
            "VALUES(#{userId}, #{content}, #{images}, DEFAULT, #{commentCount}, #{likeCount}, #{isDeleted})")
    @Options(useGeneratedKeys = true, keyProperty = "postId")
    int insert(Post post);

    @Select("SELECT p.post_id as postId, p.user_id as userId, p.content, p.images, p.post_time as postTime, " +
            "p.comment_count as commentCount, p.like_count as likeCount, p.is_deleted as isDeleted " +
            "FROM post p JOIN user u ON p.user_id = u.user_id " +
            "WHERE p.is_deleted = 0 AND u.is_deleted = 0 ORDER BY p.post_time DESC")
    List<Post> findAll();

    @Select("SELECT p.post_id as postId, p.user_id as userId, p.content, p.images, p.post_time as postTime, " +
            "p.comment_count as commentCount, p.like_count as likeCount, p.is_deleted as isDeleted " +
            "FROM post p JOIN user u ON p.user_id = u.user_id " +
            "WHERE p.is_deleted = 0 AND u.is_deleted = 0 ORDER BY p.post_time DESC")
    List<Post> findByPage();

    @Select("SELECT p.post_id as postId, p.user_id as userId, p.content, p.images, p.post_time as postTime, " +
            "p.comment_count as commentCount, p.like_count as likeCount, p.is_deleted as isDeleted " +
            "FROM post p JOIN user u ON p.user_id = u.user_id " +
            "WHERE p.post_id = #{postId} AND u.is_deleted = 0")
    Post findById(Long postId);

    @Select("SELECT p.post_id as postId, p.user_id as userId, p.content, p.images, p.post_time as postTime, " +
            "p.comment_count as commentCount, p.like_count as likeCount, p.is_deleted as isDeleted " +
            "FROM post p JOIN user u ON p.user_id = u.user_id " +
            "WHERE p.user_id = #{userId} AND p.is_deleted = 0 AND u.is_deleted = 0 ORDER BY p.post_time DESC")
    List<Post> findByUserId(Long userId);

    @Select("SELECT p.post_id FROM post p JOIN user u ON p.user_id = u.user_id " +
            "WHERE p.user_id = #{userId} AND p.is_deleted = 0 AND u.is_deleted = 0")
    List<Long> findPostIdsByUserId(Long userId);

    @Update("UPDATE post SET content = #{content}, images = #{images} WHERE post_id = #{postId}")
    int update(Post post);

    @Update("UPDATE post SET is_deleted = 1 WHERE post_id = #{postId}")
    int delete(Long postId);

    @Update("UPDATE post SET comment_count = comment_count + #{count} WHERE post_id = #{postId}")
    int updateCommentCount(@Param("postId") Long postId, @Param("count") Integer count);

    @Update("UPDATE post SET like_count = like_count + #{count} WHERE post_id = #{postId}")
    int updateLikeCount(@Param("postId") Long postId, @Param("count") Integer count);

    @Select("SELECT COUNT(*) FROM post WHERE is_deleted = 0")
    long count();
    
    @Select("SELECT p.user_id, SUM(p.like_count) as totalLikes FROM post p JOIN user u ON p.user_id = u.user_id " +
            "WHERE p.is_deleted = 0 AND u.is_deleted = 0 GROUP BY p.user_id ORDER BY totalLikes DESC LIMIT #{limit}")
    List<UserLikes> countLikesByUserId(@Param("limit") Integer limit);
    
    public static class UserLikes {
        private Long userId;
        private Long totalLikes;
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getTotalLikes() { return totalLikes; }
        public void setTotalLikes(Long totalLikes) { this.totalLikes = totalLikes; }
    }

    @Select("SELECT p.post_id as postId, p.user_id as userId, p.content, p.images, p.post_time as postTime, " +
            "p.comment_count as commentCount, p.like_count as likeCount, p.is_deleted as isDeleted " +
            "FROM post p JOIN user u ON p.user_id = u.user_id " +
            "WHERE p.is_deleted = 0 AND u.is_deleted = 0 AND p.content LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY p.post_time DESC LIMIT #{limit}")
    List<Post> searchByKeyword(@Param("keyword") String keyword, @Param("limit") Integer limit);
}
