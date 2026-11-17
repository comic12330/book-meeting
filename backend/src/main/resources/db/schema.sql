-- ============================================================
-- 基于 Spring Boot 会议室预定系统  数据库初始化脚本
-- 执行方式：mysql -uroot -p < schema.sql
-- 说明：包含建库、建表、索引、外键与初始化数据，可一次执行完成
-- ============================================================

CREATE DATABASE IF NOT EXISTS `meeting_room`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `meeting_room`;

DROP TABLE IF EXISTS `meeting_book`;
DROP TABLE IF EXISTS `notice`;
DROP TABLE IF EXISTS `meeting_room`;
DROP TABLE IF EXISTS `user`;

-- ------------------------------------------------------------
-- 用户表
-- ------------------------------------------------------------
CREATE TABLE `user` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)     NOT NULL                COMMENT '登录账号',
    `password`    VARCHAR(64)     NOT NULL                COMMENT '密码（MD5 摘要，不存明文）',
    `real_name`   VARCHAR(50)     NOT NULL                COMMENT '真实姓名',
    `phone`       VARCHAR(20)     DEFAULT NULL            COMMENT '联系电话',
    `role`        TINYINT         NOT NULL DEFAULT 0      COMMENT '角色：0 普通用户，1 管理员',
    `status`      TINYINT         NOT NULL DEFAULT 1      COMMENT '账号状态：0 禁用，1 可用',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    KEY `idx_user_role` (`role`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ------------------------------------------------------------
-- 会议室表
-- ------------------------------------------------------------
CREATE TABLE `meeting_room` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `room_name`   VARCHAR(50)     NOT NULL                COMMENT '会议室名称',
    `capacity`    INT             NOT NULL DEFAULT 0      COMMENT '容纳人数',
    `location`    VARCHAR(100)    NOT NULL                COMMENT '所在位置',
    `equipment`   VARCHAR(200)    DEFAULT ''              COMMENT '可用设备，英文逗号分隔：projector,microphone,video',
    `status`      TINYINT         NOT NULL DEFAULT 1      COMMENT '状态：0 停用，1 启用（停用不参与预定）',
    `remark`      VARCHAR(255)    DEFAULT ''              COMMENT '备注',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_room_status` (`status`),
    KEY `idx_room_capacity` (`capacity`),
    KEY `idx_room_location` (`location`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '会议室表';

-- ------------------------------------------------------------
-- 预定订单表（核心业务表）
-- ------------------------------------------------------------
CREATE TABLE `meeting_book` (
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`         BIGINT UNSIGNED NOT NULL                COMMENT '申请人',
    `room_id`         BIGINT UNSIGNED NOT NULL                COMMENT '会议室',
    `start_time`      DATETIME        NOT NULL                COMMENT '使用开始时间（支持跨天）',
    `end_time`        DATETIME        NOT NULL                COMMENT '使用结束时间（支持跨天）',
    `purpose`         VARCHAR(200)    NOT NULL                COMMENT '会议用途',
    `participant_num` INT             NOT NULL DEFAULT 1      COMMENT '参会人数',
    `status`          TINYINT         NOT NULL DEFAULT 0      COMMENT '状态：0 待审核，1 通过，2 驳回，3 取消，4 已完成',
    `audit_msg`       VARCHAR(255)    DEFAULT NULL            COMMENT '审核意见 / 驳回理由',
    `audit_time`      DATETIME        DEFAULT NULL            COMMENT '审核时间',
    `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    PRIMARY KEY (`id`),
    KEY `idx_book_room_start` (`room_id`, `start_time`),
    KEY `idx_book_user` (`user_id`),
    KEY `idx_book_status` (`status`),
    CONSTRAINT `fk_book_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_book_room` FOREIGN KEY (`room_id`) REFERENCES `meeting_room` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '会议室预定订单表';

-- ------------------------------------------------------------
-- 公告表
-- ------------------------------------------------------------
CREATE TABLE `notice` (
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`        VARCHAR(100)    NOT NULL                COMMENT '公告标题',
    `content`      TEXT            NOT NULL                COMMENT '公告内容',
    `publisher_id` BIGINT UNSIGNED NOT NULL                COMMENT '发布人',
    `publish_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `is_show`      TINYINT         NOT NULL DEFAULT 1      COMMENT '是否首页展示：0 否，1 是',
    PRIMARY KEY (`id`),
    KEY `idx_notice_show` (`is_show`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '公告表';

-- ============================================================
-- 初始化数据（所有账号初始密码均为 123456，存储 MD5 摘要）
-- MD5('123456') = e10adc3949ba59abbe56e057f20f883e
-- ============================================================
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `role`, `status`) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', '13900000000', 1, 1),
('zhangsan', 'e10adc3949ba59abbe56e057f20f883e', '张三', '13900000001', 0, 1),
('lisi', 'e10adc3949ba59abbe56e057f20f883e', '李四', '13900000002', 0, 1);

INSERT INTO `meeting_room` (`room_name`, `capacity`, `location`, `equipment`, `status`, `remark`) VALUES
('第一会议室', 12, '行政楼 3 层 301', 'projector,microphone', 1, '周会与部门例会常用'),
('第二会议室', 30, '行政楼 3 层 305', 'projector,microphone,video', 1, '支持视频会议'),
('培训教室', 60, '教学楼 2 层 201', 'projector,microphone', 1, '需提前一天预定'),
('洽谈室 A', 6, '行政楼 1 层 108', '', 1, '小型洽谈，无设备'),
('贵宾接待室', 8, '行政楼 1 层 110', 'video', 0, '装修中，暂停使用');

INSERT INTO `meeting_book` (`user_id`, `room_id`, `start_time`, `end_time`, `purpose`, `participant_num`, `status`, `audit_msg`, `audit_time`) VALUES
(2, 1, '2026-11-18 09:00:00', '2026-11-18 10:30:00', '项目组周会', 10, 1, NULL, '2026-11-17 09:30:00'),
(3, 2, '2026-11-18 14:00:00', '2026-11-18 16:00:00', '客户方案评审', 20, 0, NULL, NULL),
(2, 3, '2026-11-19 19:00:00', '2026-11-20 01:00:00', '通宵联调（跨天场景）', 25, 0, NULL, NULL);

INSERT INTO `notice` (`title`, `content`, `publisher_id`, `is_show`) VALUES
('会议室预定系统上线通知', '即日起会议室预定统一在本系统提交，线下登记与群内口头预约不再生效。', 1, 1),
('预定冲突处理规范', '同一会议室同一时段仅允许一条有效记录，提交与审核环节均会校验，请勿重复提交。', 1, 1);
