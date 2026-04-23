-- 好友关系表
CREATE TABLE IF NOT EXISTS `friend` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `friend_id` int NOT NULL COMMENT '好友ID',
  `friend_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '成为好友时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_friend`(`user_id`, `friend_id`) USING BTREE,
  INDEX `idx_friend`(`friend_id`) USING BTREE,
  CONSTRAINT `fk_friend_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_friend_friend` FOREIGN KEY (`friend_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '好友关系表';

-- 好友申请表
CREATE TABLE IF NOT EXISTS `friend_request` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `from_user_id` int NOT NULL COMMENT '申请人ID',
  `to_user_id` int NOT NULL COMMENT '接收人ID',
  `status` tinyint NULL DEFAULT 0 COMMENT '0待处理1已同意2已拒绝',
  `request_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_from_to`(`from_user_id`, `to_user_id`) USING BTREE,
  INDEX `idx_to_user`(`to_user_id`) USING BTREE,
  CONSTRAINT `fk_req_from` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_req_to` FOREIGN KEY (`to_user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '好友申请表';