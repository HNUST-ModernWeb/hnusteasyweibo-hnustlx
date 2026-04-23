package com.hnust.lx.mapper;

import com.hnust.lx.entity.GroupInvite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GroupInviteMapper {

    @Insert("INSERT INTO group_invite(group_id, invited_user_id, inviter_id, status, create_time, update_time) " +
            "VALUES(#{groupId}, #{invitedUserId}, #{inviterId}, 0, DEFAULT, DEFAULT)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GroupInvite invite);

    @Select("SELECT id, group_id as groupId, invited_user_id as invitedUserId, inviter_id as inviterId, " +
            "status, create_time as createTime, update_time as updateTime " +
            "FROM group_invite WHERE id = #{id}")
    GroupInvite findById(Long id);

    @Select("SELECT id, group_id as groupId, invited_user_id as invitedUserId, inviter_id as inviterId, " +
            "status, create_time as createTime, update_time as updateTime " +
            "FROM group_invite WHERE invited_user_id = #{userId} AND status = 0")
    List<GroupInvite> findPendingByUserId(Long userId);

    @Update("UPDATE group_invite SET status = #{status}, update_time = DEFAULT WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM group_invite WHERE invited_user_id = #{userId} AND status = 0")
    int countPendingByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM group_invite WHERE group_id = #{groupId} AND invited_user_id = #{userId} AND status = 0")
    int existsPending(@Param("groupId") Long groupId, @Param("userId") Long userId);
}