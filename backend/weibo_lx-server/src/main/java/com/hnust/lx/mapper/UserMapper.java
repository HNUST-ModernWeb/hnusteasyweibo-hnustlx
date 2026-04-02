package com.hnust.lx.mapper;

import com.hnust.lx.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO user(username, password, avatar, user_type, is_deleted) " +
            "VALUES(#{username}, #{password}, #{avatar}, #{userType}, #{isDeleted})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    int insert(User user);

    @Select("SELECT user_id as userId, username, password, avatar, register_time as registerTime, " +
            "user_type as userType, is_deleted as isDeleted FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT user_id as userId, username, password, avatar, register_time as registerTime, " +
            "user_type as userType, is_deleted as isDeleted FROM user WHERE user_id = #{userId}")
    User findById(Long userId);

    @Select("SELECT user_id as userId, username, password, avatar, register_time as registerTime, " +
            "user_type as userType, is_deleted as isDeleted FROM user WHERE is_deleted = 0 " +
            "ORDER BY register_time DESC")
    List<User> findAll();

    @Update("UPDATE user SET username = #{username}, avatar = #{avatar} WHERE user_id = #{userId}")
    int update(User user);

    @Update("UPDATE user SET is_deleted = #{isDeleted} WHERE user_id = #{userId}")
    int updateStatus(@Param("userId") Long userId, @Param("isDeleted") Integer isDeleted);

    @Delete("DELETE FROM user WHERE user_id = #{userId}")
    int delete(Long userId);

    @Select("SELECT user_id as userId, username, password, avatar, register_time as registerTime, " +
            "user_type as userType, is_deleted as isDeleted FROM user WHERE is_deleted = 0 " +
            "AND username LIKE CONCAT('%', #{keyword}, '%') LIMIT #{limit}")
    List<User> searchByKeyword(@Param("keyword") String keyword, @Param("limit") Integer limit);
}
