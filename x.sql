/*
 Navicat Premium Dump SQL

 Source Server         : Docker
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : x

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 02/12/2025 14:30:55
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
) ENGINE = InnoDB AUTO_INCREMENT = 1131 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '报警信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of alert_msg
-- ----------------------------
INSERT INTO `alert_msg` VALUES (1071, 1, 7, 'DB1000.DBD104', '108', NULL, NULL, '2025-11-09 21:01:52', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1072, 1, 7, 'DB1000.DBD104', '108', NULL, NULL, '2025-11-09 21:01:53', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1073, 1, 7, 'DB1000.DBD104', '102', NULL, NULL, '2025-11-09 21:01:53', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1074, 1, 7, 'DB1000.DBD104', '102', NULL, NULL, '2025-11-09 21:01:53', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1075, 1, 4, 'DB1000.DBD101', '109', NULL, NULL, '2025-11-09 21:01:56', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1076, 1, 4, 'DB1000.DBD101', '109', NULL, NULL, '2025-11-09 21:01:56', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1077, 1, 3, 'DB1000.DBD100', '101', NULL, NULL, '2025-11-09 21:02:01', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1078, 1, 3, 'DB1000.DBD100', '101', NULL, NULL, '2025-11-09 21:02:01', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1079, 1, 8, 'DB1000.DBD105', '105.46014469081112', NULL, NULL, '2025-11-09 21:02:07', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1080, 1, 8, 'DB1000.DBD105', '105.46014469081112', NULL, NULL, '2025-11-09 21:02:07', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1081, 1, 3, 'DB1000.DBD100', '101', NULL, NULL, '2025-11-09 21:02:10', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1082, 1, 3, 'DB1000.DBD100', '101', NULL, NULL, '2025-11-09 21:02:10', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1083, 1, 5, 'DB1000.DBD102', '100', NULL, NULL, '2025-11-09 21:02:18', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1084, 1, 8, 'DB1000.DBD105', '101.92283129457108', NULL, NULL, '2025-11-09 21:02:18', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1085, 1, 8, 'DB1000.DBD105', '101.92283129457108', NULL, NULL, '2025-11-09 21:02:18', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1086, 1, 3, 'DB1000.DBD100', '109', NULL, NULL, '2025-11-09 21:02:19', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1087, 1, 5, 'DB1000.DBD102', '105', NULL, NULL, '2025-11-09 21:02:19', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1088, 1, 3, 'DB1000.DBD100', '109', NULL, NULL, '2025-11-09 21:02:19', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1089, 1, 5, 'DB1000.DBD102', '105', NULL, NULL, '2025-11-09 21:02:19', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1090, 1, 3, 'DB1000.DBD100', '104', NULL, NULL, '2025-11-09 21:02:21', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1091, 1, 8, 'DB1000.DBD105', '100.6467900746644', NULL, NULL, '2025-11-09 21:02:21', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1092, 1, 3, 'DB1000.DBD100', '104', NULL, NULL, '2025-11-09 21:02:21', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1093, 1, 8, 'DB1000.DBD105', '100.6467900746644', NULL, NULL, '2025-11-09 21:02:21', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1094, 1, 8, 'DB1000.DBD105', '105.01072584680827', NULL, NULL, '2025-11-09 21:02:22', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1095, 1, 8, 'DB1000.DBD105', '105.01072584680827', NULL, NULL, '2025-11-09 21:02:22', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1096, 1, 4, 'DB1000.DBD101', '105', NULL, NULL, '2025-11-09 21:02:24', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1097, 1, 4, 'DB1000.DBD101', '105', NULL, NULL, '2025-11-09 21:02:24', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1098, 1, 5, 'DB1000.DBD102', '106', NULL, NULL, '2025-11-09 21:02:29', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1099, 1, 5, 'DB1000.DBD102', '106', NULL, NULL, '2025-11-09 21:02:29', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1100, 1, 4, 'DB1000.DBD101', '100', NULL, NULL, '2025-11-09 21:02:30', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1101, 1, 7, 'DB1000.DBD104', '108', NULL, NULL, '2025-11-09 21:02:31', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1102, 1, 7, 'DB1000.DBD104', '108', NULL, NULL, '2025-11-09 21:02:31', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1103, 1, 3, 'DB1000.DBD100', '108', NULL, NULL, '2025-11-09 21:02:34', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1104, 1, 3, 'DB1000.DBD100', '108', NULL, NULL, '2025-11-09 21:02:34', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1105, 1, 5, 'DB1000.DBD102', '107', NULL, NULL, '2025-11-09 21:02:36', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1106, 1, 5, 'DB1000.DBD102', '107', NULL, NULL, '2025-11-09 21:02:36', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1107, 1, 5, 'DB1000.DBD102', '100', NULL, NULL, '2025-11-09 21:02:38', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1108, 1, 4, 'DB1000.DBD101', '104', NULL, NULL, '2025-11-09 21:02:39', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1109, 1, 8, 'DB1000.DBD105', '107.30395856832244', NULL, NULL, '2025-11-09 21:02:39', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1110, 1, 4, 'DB1000.DBD101', '104', NULL, NULL, '2025-11-09 21:02:39', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1111, 1, 8, 'DB1000.DBD105', '107.30395856832244', NULL, NULL, '2025-11-09 21:02:39', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1112, 1, 5, 'DB1000.DBD102', '100', NULL, NULL, '2025-11-09 21:02:40', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1113, 1, 8, 'DB1000.DBD105', '101.09472067211492', NULL, NULL, '2025-11-09 21:02:40', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1114, 1, 8, 'DB1000.DBD105', '101.09472067211492', NULL, NULL, '2025-11-09 21:02:40', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1115, 1, 3, 'DB1000.DBD100', '108', NULL, NULL, '2025-11-09 21:02:42', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1116, 1, 3, 'DB1000.DBD100', '108', NULL, NULL, '2025-11-09 21:02:42', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1117, 1, 5, 'DB1000.DBD102', '101', NULL, NULL, '2025-11-09 21:02:43', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1118, 1, 8, 'DB1000.DBD105', '109.71969436386208', NULL, NULL, '2025-11-09 21:02:43', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1119, 1, 5, 'DB1000.DBD102', '101', NULL, NULL, '2025-11-09 21:02:43', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1120, 1, 8, 'DB1000.DBD105', '109.71969436386208', NULL, NULL, '2025-11-09 21:02:43', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1121, 1, 4, 'DB1000.DBD101', '102', NULL, NULL, '2025-11-09 21:02:45', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1122, 1, 4, 'DB1000.DBD101', '102', NULL, NULL, '2025-11-09 21:02:45', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1123, 1, 5, 'DB1000.DBD102', '105', NULL, NULL, '2025-11-09 21:02:46', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1124, 1, 5, 'DB1000.DBD102', '105', NULL, NULL, '2025-11-09 21:02:46', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1125, 1, 4, 'DB1000.DBD101', '107', NULL, NULL, '2025-11-09 21:02:50', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1126, 1, 8, 'DB1000.DBD105', '106.32041929676424', NULL, NULL, '2025-11-09 21:02:50', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1127, 1, 4, 'DB1000.DBD101', '107', NULL, NULL, '2025-11-09 21:02:50', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1128, 1, 8, 'DB1000.DBD105', '106.32041929676424', NULL, NULL, '2025-11-09 21:02:50', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1129, 1, 7, 'DB1000.DBD104', '105', NULL, NULL, '2025-11-09 21:02:51', 'system', NULL, NULL, NULL, 0);
INSERT INTO `alert_msg` VALUES (1130, 1, 7, 'DB1000.DBD104', '105', NULL, NULL, '2025-11-09 21:02:51', 'system', NULL, NULL, NULL, 0);

-- ----------------------------
-- Table structure for device_info
-- ----------------------------
DROP TABLE IF EXISTS `device_info`;
CREATE TABLE `device_info`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备名称',
  `device_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备编码',
  `data_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据编码',
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
INSERT INTO `device_info` VALUES (1, '电铲1号', 'shovel001', 'original', NULL, '2025-10-30 15:49:07', 'system', '2025-12-02 01:30:56', 'system', NULL, 0);
INSERT INTO `device_info` VALUES (2, '电铲2号', 'shovel002', 'tzos', NULL, '2025-10-30 15:49:07', 'system', '2025-12-02 01:23:02', 'system', NULL, 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '设备表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of device_point_info
-- ----------------------------
INSERT INTO `device_point_info` VALUES (3, 1, 'shovel001', 'DB1000.DBD100', '温度', 2, NULL, '2025-11-09 18:29:39', 'system', '2025-11-09 12:25:02', NULL, NULL, 0);
INSERT INTO `device_point_info` VALUES (4, 1, 'shovel001', 'DB1000.DBD101', '湿度', 2, NULL, '2025-11-09 18:30:00', 'system', '2025-11-09 12:25:04', NULL, NULL, 0);
INSERT INTO `device_point_info` VALUES (5, 1, 'shovel001', 'DB1000.DBD102', '转速', 2, NULL, '2025-11-09 18:30:08', 'system', '2025-11-09 12:25:05', NULL, NULL, 0);
INSERT INTO `device_point_info` VALUES (6, 1, 'shovel001', 'DB1000.DBX103.0', '启动状态', 1, NULL, '2025-11-09 18:30:36', 'system', '2025-11-09 12:30:19', NULL, NULL, 0);
INSERT INTO `device_point_info` VALUES (7, 1, 'shovel001', 'DB1000.DBD104', '压力', 2, NULL, '2025-11-09 18:31:10', 'system', '2025-11-09 12:25:26', NULL, NULL, 0);
INSERT INTO `device_point_info` VALUES (8, 1, 'shovel001', 'DB1000.DBD105', '重量', 3, NULL, '2025-11-09 18:31:31', 'system', '2025-11-09 12:30:16', NULL, NULL, 0);
INSERT INTO `device_point_info` VALUES (9, 2, 'shovel002', 'DB1000.DBD100', '温度', 2, NULL, '2025-11-09 18:29:39', 'system', '2025-12-02 01:29:38', NULL, NULL, 0);
INSERT INTO `device_point_info` VALUES (10, 2, 'shovel002', 'DB1000.DBD101', '湿度', 2, NULL, '2025-11-09 18:30:00', 'system', '2025-11-09 12:25:04', NULL, NULL, 0);
INSERT INTO `device_point_info` VALUES (11, 2, 'shovel002', 'DB1000.DBD102', '转速', 2, NULL, '2025-11-09 18:30:08', 'system', '2025-11-09 12:25:05', NULL, NULL, 0);
INSERT INTO `device_point_info` VALUES (12, 2, 'shovel002', 'DB1000.DBX103.0', '启动状态', 1, NULL, '2025-11-09 18:30:36', 'system', '2025-11-09 12:30:19', NULL, NULL, 0);
INSERT INTO `device_point_info` VALUES (13, 2, 'shovel002', 'DB1000.DBD104', '压力', 2, NULL, '2025-11-09 18:31:10', 'system', '2025-11-09 12:25:26', NULL, NULL, 0);
INSERT INTO `device_point_info` VALUES (14, 2, 'shovel002', 'DB1000.DBD105', '重量', 3, NULL, '2025-11-09 18:31:31', 'system', '2025-11-09 12:30:16', NULL, NULL, 0);

SET FOREIGN_KEY_CHECKS = 1;
