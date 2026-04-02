package com.hnust.lx.mapper;

import com.hnust.lx.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AdminMapper {

    @Select("SELECT user_id as userId, username, password, avatar, register_time as registerTime, " +
            "user_type as userType, is_deleted as isDeleted FROM user " +
            "WHERE username = #{username} AND user_type = 1")
    User login(String username);

    @Select("SELECT user_id as userId, username, avatar, register_time as registerTime, " +
            "user_type as userType, is_deleted as isDeleted FROM user " +
            "ORDER BY register_time DESC LIMIT #{offset}, #{pageSize}")
    List<User> listUsers(@Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM user")
    Long countUsers();

    @Update("UPDATE user SET is_deleted = #{isDeleted} WHERE user_id = #{userId}")
    void updateStatus(@Param("userId") Long userId, @Param("isDeleted") Integer isDeleted);

    @Select("SELECT user_id as userId, username, avatar, register_time as registerTime, " +
            "user_type as userType, is_deleted as isDeleted FROM user WHERE user_id = #{userId}")
    User getById(@Param("userId") Long userId);

    @Delete("DELETE FROM user WHERE user_id = #{userId}")
    int deleteById(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM user WHERE is_deleted = 0")
    Long countAllUsers();

    @Select("SELECT COUNT(*) FROM post WHERE is_deleted = 0")
    Long countAllPosts();

    @Select("SELECT COUNT(*) FROM comment WHERE is_deleted = 0")
    Long countAllComments();

    @Select("SELECT COUNT(*) FROM tag WHERE is_deleted = 0")
    Long countAllTags();
}