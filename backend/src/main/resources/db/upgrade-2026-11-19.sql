-- ============================================================
-- 增量升级脚本（2026-11-19）
-- 适用：已经在跑旧版库的环境，执行一次即可；全新环境直接用 schema.sql。
-- 重复执行会因「列已存在」报错，属预期，无需处理。
-- ============================================================

USE `meeting_room`;

-- ------------------------------------------------------------
-- 1. 操作日志表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `operator_id`   BIGINT UNSIGNED NOT NULL                COMMENT '操作人',
    `operator_name` VARCHAR(50)     NOT NULL                COMMENT '操作人姓名（操作当时的快照）',
    `module`        VARCHAR(16)     NOT NULL                COMMENT '所属模块：ROOM/BOOK/USER/NOTICE/CONFIG',
    `op_type`       VARCHAR(40)     NOT NULL                COMMENT '操作类型，与 OperationType 枚举一一对应',
    `op_result`     TINYINT         NOT NULL DEFAULT 1      COMMENT '结果：1 成功，0 失败',
    `result_msg`    VARCHAR(255)    DEFAULT NULL            COMMENT '失败原因',
    `target_desc`   VARCHAR(200)    DEFAULT NULL            COMMENT '操作对象的可读描述',
    `op_time`       DATETIME        NOT NULL                COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_log_time` (`op_time`),
    KEY `idx_log_operator` (`operator_id`, `op_time`),
    KEY `idx_log_type` (`module`, `op_type`, `op_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '操作日志表';

-- ------------------------------------------------------------
-- 2. 预定表增加取消理由与取消时间
-- 管理员取消他人预定时理由必填，申请人可在「我的预定」里看到原因；
-- 本人取消时 cancel_reason 为空。
-- ------------------------------------------------------------
ALTER TABLE `meeting_book`
    ADD COLUMN `cancel_reason` VARCHAR(255) DEFAULT NULL
        COMMENT '取消理由：管理员取消他人预定时必填，本人取消为空' AFTER `audit_time`,
    ADD COLUMN `cancel_time` DATETIME DEFAULT NULL
        COMMENT '取消时间' AFTER `cancel_reason`;
