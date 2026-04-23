package com.hnust.lx.mapper;

import com.hnust.lx.entity.Friend;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FriendMapper {

    @Insert("INSERT INTO friend(user_id, friend_id, friend_time) VALUES(#{userId}, #{friendId}, DEFAULT)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Friend friend);

    @Select("SELECT f.user_id as userId, f.friend_id as friendId, f.friend_time as friendTime " +
            "FROM friend f WHERE f.user_id = #{userId}")
    List<Friend> findByUserId(Long userId);

    @Select("SELECT f.user_id as userId, f.friend_id as friendId " +
            "FROM friend f WHERE f.user_id = #{userId} AND f.friend_id = #{friendId}")
    Friend findByUserAndFriend(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Delete("DELETE FROM friend WHERE user_id = #{userId} AND friend_id = #{friendId}")
    int delete(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Select("SELECT friend_id FROM friend WHERE user_id = #{userId}")
    List<Long> findFriendIdsByUserId(Long userId);
}