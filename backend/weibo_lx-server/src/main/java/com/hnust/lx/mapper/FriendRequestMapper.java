package com.hnust.lx.mapper;

import com.hnust.lx.entity.FriendRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FriendRequestMapper {

    @Insert("INSERT INTO friend_request(from_user_id, to_user_id, status, request_time) VALUES(#{fromUserId}, #{toUserId}, 0, DEFAULT)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FriendRequest request);

    @Select("SELECT id, from_user_id as fromUserId, to_user_id as toUserId, status, request_time as requestTime, handle_time as handleTime " +
            "FROM friend_request WHERE to_user_id = #{userId} AND status = 0 ORDER BY request_time DESC")
    List<FriendRequest> findPendingByUserId(Long userId);

    @Select("SELECT id, from_user_id as fromUserId, to_user_id as toUserId, status, request_time as requestTime " +
            "FROM friend_request WHERE from_user_id = #{fromUserId} AND to_user_id = #{toUserId}")
    FriendRequest findPending(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    @Update("UPDATE friend_request SET status = #{status}, handle_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Delete("DELETE FROM friend_request WHERE id = #{id}")
    int delete(Long id);

    @Select("SELECT COUNT(*) FROM friend_request WHERE to_user_id = #{userId} AND status = 0")
    int countPending(Long userId);
}