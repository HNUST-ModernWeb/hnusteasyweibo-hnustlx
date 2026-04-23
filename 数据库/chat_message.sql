-- 对话聊天功能数据库设计
-- 私聊消息表
CREATE TABLE IF NOT EXISTS `private_message` (
  `message_id` int NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` int NOT NULL COMMENT '发送者ID',
  `receiver_id` int NOT NULL COMMENT '接收者ID',
  `content` varchar(500) CHARACTER SET utf8mb4 NOT NULL COMMENT '消息内容',
  `send_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `is_read` tinyint NULL DEFAULT 0 COMMENT '0未读1已读',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0未删除1已删除',
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_sender`(`sender_id`) USING BTREE,
  INDEX `idx_receiver`(`receiver_id`) USING BTREE,
  INDEX `idx_conversation`(`sender_id`, `receiver_id`) USING BTREE,
  CONSTRAINT `fk_pm_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pm_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '私聊消息表';

-- 群聊表
CREATE TABLE IF NOT EXISTS `group_chat` (
  `group_id` int NOT NULL AUTO_INCREMENT COMMENT '群ID',
  `group_name` varchar(50) CHARACTER SET utf8mb4 NOT NULL COMMENT '群名称',
  `creator_id` int NOT NULL COMMENT '创建者ID',
  `avatar` varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '群头像',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0未删除1已删除',
  PRIMARY KEY (`group_id`) USING BTREE,
  INDEX `idx_creator`(`creator_id`) USING BTREE,
  CONSTRAINT `fk_gc_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '群聊表';

-- 群成员表
CREATE TABLE IF NOT EXISTS `group_member` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `group_id` int NOT NULL COMMENT '群ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `role` tinyint NULL DEFAULT 0 COMMENT '0成员1管理员2群主',
  `join_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_user`(`group_id`, `user_id`) USING BTREE,
  INDEX `idx_member_user`(`user_id`) USING BTREE,
  CONSTRAINT `fk_gm_member_group` FOREIGN KEY (`group_id`) REFERENCES `group_chat` (`group_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_gm_member_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '群成员表';

-- 群消息表
CREATE TABLE IF NOT EXISTS `group_message` (
  `message_id` int NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `group_id` int NOT NULL COMMENT '群ID',
  `sender_id` int NOT NULL COMMENT '发送者ID',
  `content` varchar(500) CHARACTER SET utf8mb4 NOT NULL COMMENT '消息内容',
  `send_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_gm_group`(`group_id`) USING BTREE,
  INDEX `idx_gm_sender`(`sender_id`) USING BTREE,
  CONSTRAINT `fk_gm_message_group` FOREIGN KEY (`group_id`) REFERENCES `group_chat` (`group_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_gm_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '群消息表';

-- 群邀请表
CREATE TABLE IF NOT EXISTS `group_invite` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `group_id` int NOT NULL COMMENT '群ID',
  `invited_user_id` int NOT NULL COMMENT '被邀请用户ID',
  `inviter_id` int NOT NULL COMMENT '邀请人ID',
  `status` tinyint NULL DEFAULT 0 COMMENT '0待处理1已接受2已拒绝',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '邀请时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_invited`(`invited_user_id`) USING BTREE,
  INDEX `idx_group`(`group_id`) USING BTREE,
  CONSTRAINT `fk_gi_group` FOREIGN KEY (`group_id`) REFERENCES `group_chat` (`group_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_gi_user` FOREIGN KEY (`invited_user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '群邀请表';