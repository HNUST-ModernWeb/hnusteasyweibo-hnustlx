package com.hnust.lx.mapper;

import com.hnust.lx.entity.PrivateMessage;
import com.hnust.lx.vo.PrivateMessageVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PrivateMessageMapper {

    @Insert("INSERT INTO private_message(sender_id, receiver_id, content, send_time, is_read, is_deleted) " +
            "VALUES(#{senderId}, #{receiverId}, #{content}, NOW(), #{isRead}, #{isDeleted})")
    @Options(useGeneratedKeys = true, keyProperty = "messageId")
    int insert(PrivateMessage message);

    @Select("SELECT message_id as messageId, sender_id as senderId, receiver_id as receiverId, content, " +
            "send_time as sendTime, is_read as isRead, is_deleted as isDeleted " +
            "FROM private_message WHERE is_deleted = 0 AND " +
            "((sender_id = #{senderId} AND receiver_id = #{receiverId}) " +
            "OR (sender_id = #{receiverId} AND receiver_id = #{senderId})) " +
            "ORDER BY send_time ASC")
    List<PrivateMessage> findConversation(@Param("senderId") Long senderId,
                                         @Param("receiverId") Long receiverId);

    @Select("SELECT message_id as messageId, sender_id as senderId, receiver_id as receiverId, content, " +
            "send_time as sendTime, is_read as isRead, is_deleted as isDeleted " +
            "FROM private_message WHERE is_deleted = 0 AND message_id = #{messageId}")
    PrivateMessage findById(Long messageId);

    @Update("UPDATE private_message SET is_read = 1 WHERE sender_id = #{senderId} AND receiver_id = #{receiverId} AND is_read = 0")
    int markAsRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    @Select("SELECT COUNT(*) FROM private_message WHERE receiver_id = #{receiverId} AND sender_id = #{senderId} AND is_read = 0 AND is_deleted = 0")
    int countUnread(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    @Select("SELECT DISTINCT CASE WHEN sender_id = #{currentUserId} THEN receiver_id ELSE sender_id END as userId " +
            "FROM private_message WHERE is_deleted = 0 AND (sender_id = #{currentUserId} OR receiver_id = #{currentUserId})")
    List<Long> findConversationUsers(Long currentUserId);

    @Select("SELECT message_id, sender_id, receiver_id, content, send_time, is_read, is_deleted " +
            "FROM private_message WHERE message_id IN " +
            "(SELECT MAX(message_id) FROM private_message WHERE is_deleted = 0 AND " +
            "(sender_id = #{currentUserId} OR receiver_id = #{currentUserId}) GROUP BY " +
            "CASE WHEN sender_id = #{currentUserId} THEN receiver_id ELSE sender_id END)")
    List<PrivateMessage> findLatestMessages(Long currentUserId);

    @Select("SELECT COUNT(*) FROM private_message WHERE is_deleted = 0 AND receiver_id = #{userId} AND is_read = 0")
    int countTotalUnread(Long userId);
}