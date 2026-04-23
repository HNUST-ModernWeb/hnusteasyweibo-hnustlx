package com.hnust.lx.mapper;

import com.hnust.lx.entity.GroupMember;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GroupMemberMapper {

    @Insert("INSERT INTO group_member(group_id, user_id, role, join_time) " +
            "VALUES(#{groupId}, #{userId}, #{role}, DEFAULT)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GroupMember member);

    @Select("SELECT id, group_id as groupId, user_id as userId, role, join_time as joinTime " +
            "FROM group_member WHERE group_id = #{groupId} AND user_id = #{userId}")
    GroupMember findByUserAndGroup(@Param("groupId") Long groupId, @Param("userId") Long userId);

    @Select("SELECT gm.id, gm.group_id as groupId, gm.user_id as userId, gm.role, gm.join_time as joinTime " +
            "FROM group_member gm JOIN user u ON gm.user_id = u.user_id " +
            "WHERE gm.group_id = #{groupId} AND u.is_deleted = 0")
    List<GroupMember> findByGroupId(Long groupId);

    @Select("SELECT user_id FROM group_member WHERE group_id = #{groupId}")
    List<Long> findUserIdsByGroupId(Long groupId);

    @Delete("DELETE FROM group_member WHERE group_id = #{groupId} AND user_id = #{userId}")
    int delete(@Param("groupId") Long groupId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM group_member WHERE group_id = #{groupId}")
    int countByGroupId(Long groupId);

    @Select("SELECT COUNT(*) FROM group_member WHERE group_id = #{groupId} AND user_id = #{userId}")
    int exists(@Param("groupId") Long groupId, @Param("userId") Long userId);
}