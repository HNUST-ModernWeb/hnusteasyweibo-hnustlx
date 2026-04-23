-- 创建数据库：weibo_lx
CREATE DATABASE IF NOT EXISTS weibo_lx DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 使用该数据库
USE weibo_lx;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 用户�?
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
                         `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                         `username` varchar(50) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户�?,
                         `password` varchar(100) CHARACTER SET utf8mb4 NOT NULL COMMENT '密码',
                         `avatar` varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '头像URL',
                         `register_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                         `user_type` tinyint(1) NULL DEFAULT 0 COMMENT '0普通用�?1管理�?,
                         `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '0未删�?1已删�?,
                          `bio` varchar(200) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '个人简介',
                         PRIMARY KEY (`user_id`) USING BTREE,
                         UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '用户�?;

-- ----------------------------
-- 动态表
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post`  (
                         `post_id` int NOT NULL AUTO_INCREMENT COMMENT '动态ID',
                         `user_id` int NOT NULL COMMENT '发布者ID',
                         `content` varchar(200) CHARACTER SET utf8mb4 NOT NULL COMMENT '内容',
                         `post_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
                         `comment_count` int NULL DEFAULT 0 COMMENT '评论�?,
                         `like_count` int NULL DEFAULT 0 COMMENT '点赞�?,
                         `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '0未删�?,
                         PRIMARY KEY (`post_id`) USING BTREE,
                         INDEX `fk_post_user`(`user_id`) USING BTREE,
                         CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '动态表';

-- ----------------------------
-- 评论�?
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
                            `comment_id` int NOT NULL AUTO_INCREMENT COMMENT '评论ID',
                            `post_id` int NOT NULL COMMENT '所属动态ID',
                            `user_id` int NOT NULL COMMENT '评论者ID',
                            `content` varchar(200) CHARACTER SET utf8mb4 NOT NULL COMMENT '评论内容',
                            `comment_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
                            `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '0未删�?,
                            PRIMARY KEY (`comment_id`) USING BTREE,
                            INDEX `fk_comment_post`(`post_id`) USING BTREE,
                            INDEX `fk_comment_user`(`user_id`) USING BTREE,
                            CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE CASCADE,
                            CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '评论�?;

-- ----------------------------
-- 点赞�?
-- ----------------------------
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like`  (
                              `like_id` int NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
                              `post_id` int NOT NULL COMMENT '动态ID',
                              `user_id` int NOT NULL COMMENT '用户ID',
                              `like_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
                              PRIMARY KEY (`like_id`) USING BTREE,
                              UNIQUE INDEX `uk_user_post`(`user_id`, `post_id`) USING BTREE,
                              INDEX `fk_like_post`(`post_id`) USING BTREE,
                              CONSTRAINT `fk_like_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE CASCADE,
                              CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '点赞�?;

-- ----------------------------
-- 标签�?
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
                        `tag_id` int NOT NULL AUTO_INCREMENT COMMENT '标签ID',
                        `tag_name` varchar(30) CHARACTER SET utf8mb4 NOT NULL COMMENT '标签�?,
                        `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '0未删�?,
                        PRIMARY KEY (`tag_id`) USING BTREE,
                        UNIQUE INDEX `tag_name`(`tag_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '标签�?;

-- ----------------------------
-- 动态标签关联表
-- ----------------------------
DROP TABLE IF EXISTS `post_tag`;
CREATE TABLE `post_tag`  (
                             `pt_id` int NOT NULL AUTO_INCREMENT COMMENT '关联ID',
                             `post_id` int NOT NULL COMMENT '动态ID',
                             `tag_id` int NOT NULL COMMENT '标签ID',
                             PRIMARY KEY (`pt_id`) USING BTREE,
                             UNIQUE INDEX `uk_post_tag`(`post_id`, `tag_id`) USING BTREE,
                             INDEX `fk_pt_tag`(`tag_id`) USING BTREE,
                             CONSTRAINT `fk_pt_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE CASCADE,
                             CONSTRAINT `fk_pt_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tag_id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '动态标签关联表';

SET FOREIGN_KEY_CHECKS = 1;

-- upgrade script: add images column for post table
ALTER TABLE `post` 
    ADD COLUMN IF NOT EXISTS `images` text CHARACTER SET utf8mb4 NULL COMMENT 'post image urls json';

