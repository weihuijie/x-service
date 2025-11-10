/*
 Navicat Premium Dump SQL

 Source Server         : docker-me
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : x

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 10/11/2025 22:41:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for alert_msg
-- ----------------------------
DROP TABLE IF EXISTS `alert_msg`;
CREATE TABLE `alert_msg`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '报警ID',
  `device_id` bigint NULL DEFAULT NULL COMMENT '设备ID',
  `point_id` bigint NULL DEFAULT NULL COMMENT '点位ID',
  `point_addr` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '点位地址',
  `point_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '点位值',
  `timestamp` bigint NULL DEFAULT NULL COMMENT '时间戳',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '更新人',
  `status` int NULL DEFAULT NULL COMMENT '状态',
  `is_deleted` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1582 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '报警信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of alert_msg
-- ----------------------------
INSERT INTO `alert_msg` VALUES (1556, 1, 4, 'DB1000.DBD101', '102', 1762785654049, NULL, '2025-11-10 22:40:54', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1557, 1, 7, 'DB1000.DBD104', '103', 1762785654049, NULL, '2025-11-10 22:40:54', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1558, 1, 4, 'DB1000.DBD101', '102', 1762785654049, NULL, '2025-11-10 22:40:54', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1559, 1, 7, 'DB1000.DBD104', '103', 1762785654049, NULL, '2025-11-10 22:40:54', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1560, 1, 3, 'DB1000.DBD100', '108', 1762785656038, NULL, '2025-11-10 22:40:56', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1561, 1, 3, 'DB1000.DBD100', '108', 1762785656038, NULL, '2025-11-10 22:40:56', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1562, 1, 8, 'DB1000.DBD105', '109.02265634361672', 1762785657042, NULL, '2025-11-10 22:40:57', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1563, 1, 8, 'DB1000.DBD105', '109.02265634361672', 1762785657042, NULL, '2025-11-10 22:40:57', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1564, 1, 3, 'DB1000.DBD100', '104', 1762785659037, NULL, '2025-11-10 22:40:59', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1565, 1, 3, 'DB1000.DBD100', '104', 1762785659037, NULL, '2025-11-10 22:40:59', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1566, 1, 3, 'DB1000.DBD100', '107', 1762785661017, NULL, '2025-11-10 22:41:01', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1567, 1, 3, 'DB1000.DBD100', '107', 1762785661017, NULL, '2025-11-10 22:41:01', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1568, 1, 4, 'DB1000.DBD101', '102', 1762785664033, NULL, '2025-11-10 22:41:04', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1569, 1, 4, 'DB1000.DBD101', '102', 1762785664033, NULL, '2025-11-10 22:41:04', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1570, 1, 3, 'DB1000.DBD100', '103', 1762785664993, NULL, '2025-11-10 22:41:05', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1571, 1, 3, 'DB1000.DBD100', '103', 1762785664993, NULL, '2025-11-10 22:41:05', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1572, 1, 8, 'DB1000.DBD105', '101.19461710832888', 1762785666016, NULL, '2025-11-10 22:41:06', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1573, 1, 8, 'DB1000.DBD105', '101.19461710832888', 1762785666016, NULL, '2025-11-10 22:41:06', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1574, 1, 3, 'DB1000.DBD100', '108', 1762785671028, NULL, '2025-11-10 22:41:11', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1575, 1, 3, 'DB1000.DBD100', '108', 1762785671028, NULL, '2025-11-10 22:41:11', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1576, 1, 4, 'DB1000.DBD101', '106', 1762785676979, NULL, '2025-11-10 22:41:17', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1577, 1, 4, 'DB1000.DBD101', '106', 1762785676979, NULL, '2025-11-10 22:41:17', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1578, 1, 3, 'DB1000.DBD100', '109', 1762785677967, NULL, '2025-11-10 22:41:18', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1579, 1, 8, 'DB1000.DBD105', '103.39870097093812', 1762785677967, NULL, '2025-11-10 22:41:18', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1580, 1, 3, 'DB1000.DBD100', '109', 1762785677967, NULL, '2025-11-10 22:41:18', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1581, 1, 8, 'DB1000.DBD105', '103.39870097093812', 1762785677967, NULL, '2025-11-10 22:41:18', 'system', NULL, NULL, NULL, 0);

-- ----------------------------
-- Table structure for device_info
-- ----------------------------
DROP TABLE IF EXISTS `device_info`;
CREATE TABLE `device_info`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备名称',
  `device_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备编码',
  `plc_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'PLC编码',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '更新人',
  `status` int NULL DEFAULT NULL COMMENT '状态',
  `is_deleted` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '设备表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of device_info
-- ----------------------------
INSERT INTO `device_info` VALUES (1, '电铲1号', 'shovel001', 'shovle_plc', NULL, '2025-10-30 15:49:07', 'system', '2025-10-30 16:14:06', 'system', NULL, 0);

-- ----------------------------
-- Table structure for device_point_info
-- ----------------------------
DROP TABLE IF EXISTS `device_point_info`;
CREATE TABLE `device_point_info`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '点位ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `device_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备编码',
  `point_addr` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '点位地址',
  `point_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '点位名称',
  `point_type` int NULL DEFAULT NULL COMMENT '点位类型',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '更新人',
  `status` int NULL DEFAULT NULL COMMENT '状态',
  `is_deleted` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '设备表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of device_point_info
-- ----------------------------
INSERT INTO `device_point_info` VALUES (3, 1, 'shovel001', 'DB1000.DBD100', '温度', 2, NULL, '2025-11-09 18:29:39', 'system', '2025-11-09 12:25:02', NULL, NULL, 0);
INSERT INTO `device_point_info` VALUES (4, 1, 'shovel001', 'DB1000.DBD101', '湿度', 2, NULL, '2025-11-09 18:30:00', 'system', '2025-11-09 12:25:04', NULL, NULL, 0);
INSERT INTO `device_point_info` VALUES (5, 1, 'shovel001', 'DB1000.DBD102', '转速', 2, NULL, '2025-11-09 18:30:08', 'system', '2025-11-09 12:25:05', NULL, NULL, 0);
INSERT INTO `device_point_info` VALUES (6, 1, 'shovel001', 'DB1000.DBX103.0', '启动状态', 1, NULL, '2025-11-09 18:30:36', 'system', '2025-11-09 12:30:19', NULL, NULL, 0);
INSERT INTO `device_point_info` VALUES (7, 1, 'shovel001', 'DB1000.DBD104', '压力', 2, NULL, '2025-11-09 18:31:10', 'system', '2025-11-09 12:25:26', NULL, NULL, 0);
INSERT INTO `device_point_info` VALUES (8, 1, 'shovel001', 'DB1000.DBD105', '重量', 3, NULL, '2025-11-09 18:31:31', 'system', '2025-11-09 12:30:16', NULL, NULL, 0);

SET FOREIGN_KEY_CHECKS = 1;
