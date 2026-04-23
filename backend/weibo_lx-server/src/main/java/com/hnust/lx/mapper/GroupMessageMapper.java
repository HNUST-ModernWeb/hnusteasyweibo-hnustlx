package com.hnust.lx.mapper;

import com.hnust.lx.entity.GroupMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GroupMessageMapper {

    @Insert("INSERT INTO group_message(group_id, sender_id, content, send_time) " +
            "VALUES(#{groupId}, #{senderId}, #{content}, DEFAULT)")
    @Options(useGeneratedKeys = true, keyProperty = "messageId")
    int insert(GroupMessage message);

    @Select("SELECT message_id as messageId, group_id as groupId, sender_id as senderId, content, send_time as sendTime " +
            "FROM group_message WHERE group_id = #{groupId} ORDER BY send_time DESC")
    List<GroupMessage> findByGroupId(Long groupId);

    @Select("SELECT message_id as messageId, group_id as groupId, sender_id as senderId, content, send_time as sendTime " +
            "FROM group_message WHERE group_id = #{groupId} ORDER BY send_time DESC LIMIT #{limit}")
    List<GroupMessage> findRecentByGroupId(@Param("groupId") Long groupId, @Param("limit") Integer limit);

    @Select("SELECT message_id as messageId, group_id as groupId, sender_id as senderId, content, send_time as sendTime " +
            "FROM group_message WHERE group_id = #{groupId} ORDER BY send_time DESC LIMIT 1")
    GroupMessage findLastByGroupId(Long groupId);

    @Select("SELECT COUNT(*) FROM group_message WHERE group_id = #{groupId}")
    int countByGroupId(Long groupId);
}