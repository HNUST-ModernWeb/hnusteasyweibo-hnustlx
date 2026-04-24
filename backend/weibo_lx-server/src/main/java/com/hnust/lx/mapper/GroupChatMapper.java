package com.hnust.lx.mapper;

import com.hnust.lx.entity.GroupChat;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GroupChatMapper {

    @Insert("INSERT INTO group_chat(group_name, creator_id, avatar, create_time, is_deleted) " +
            "VALUES(#{groupName}, #{creatorId}, #{avatar}, DEFAULT, #{isDeleted})")
    @Options(useGeneratedKeys = true, keyProperty = "groupId")
    int insert(GroupChat groupChat);

    @Select("SELECT group_id as groupId, group_name as groupName, creator_id as creatorId, avatar, " +
            "create_time as createTime, is_deleted as isDeleted " +
            "FROM group_chat WHERE is_deleted = 0 AND group_id = #{groupId}")
    GroupChat findById(Long groupId);

    @Select("SELECT g.group_id as groupId, g.group_name as groupName, g.creator_id as creatorId, g.avatar, " +
            "g.create_time as createTime, g.is_deleted as isDeleted " +
            "FROM group_chat g JOIN group_member gm ON g.group_id = gm.group_id " +
            "WHERE gm.user_id = #{userId} AND g.is_deleted = 0 " +
            "ORDER BY g.create_time DESC")
    List<GroupChat> findByUserId(Long userId);

    @Update("UPDATE group_chat SET is_deleted = 1 WHERE group_id = #{groupId}")
    int delete(Long groupId);

    @Update("UPDATE group_chat SET group_name = #{groupName}, avatar = #{avatar} WHERE group_id = #{groupId}")
    int update(GroupChat groupChat);

    @Select("SELECT COUNT(*) FROM group_chat WHERE is_deleted = 0")
    long count();
}