/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : localhost:3306
 Source Schema         : smart_mall

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 28/09/2026 19:31:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_chat_message
-- ----------------------------
DROP TABLE IF EXISTS `t_chat_message`;
CREATE TABLE `t_chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话 UUID',
  `seq` int NOT NULL COMMENT '窗口内顺序（每次覆盖重排 0..n-1）',
  `role` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色：system/user/ai/tool',
  `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '纯文本内容（供展示）',
  `message_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '完整 ChatMessage JSON（供记忆重建）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_seq`(`session_id` ASC, `seq` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '会话消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_chat_message
-- ----------------------------

-- ----------------------------
-- Table structure for t_order
-- ----------------------------
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '下单用户 id（关联 t_user.id）',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '下单用户名（冗余，便于 AI 查询与展示）',
  `total_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态：已支付/已发货/已完成',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_order
-- ----------------------------
INSERT INTO `t_order` VALUES (1, 'SO20260101001', 2, '张三', 8498.00, '已发货', '2026-09-28 19:30:49');
INSERT INTO `t_order` VALUES (2, 'SO20260101002', 2, '张三', 1499.00, '已完成', '2026-09-28 19:30:49');
INSERT INTO `t_order` VALUES (3, 'SO20260101003', 3, '李四', 9999.00, '已支付', '2026-09-28 19:30:49');

-- ----------------------------
-- Table structure for t_order_item
-- ----------------------------
DROP TABLE IF EXISTS `t_order_item`;
CREATE TABLE `t_order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '所属订单 id（关联 t_order.id）',
  `product_id` bigint NULL DEFAULT NULL COMMENT '商品 id（关联 t_product.id）',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称（下单时快照）',
  `price` decimal(10, 2) NOT NULL COMMENT '单价（下单时快照）',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '购买数量',
  `subtotal` decimal(10, 2) NOT NULL COMMENT '小计 = price × quantity',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_order_item
-- ----------------------------
INSERT INTO `t_order_item` VALUES (1, 1, 1, '华为Mate 60 Pro', 6999.00, 1, 6999.00);
INSERT INTO `t_order_item` VALUES (2, 1, 2, '华为FreeBuds Pro 3', 1499.00, 1, 1499.00);
INSERT INTO `t_order_item` VALUES (3, 2, 2, '华为FreeBuds Pro 3', 1499.00, 1, 1499.00);
INSERT INTO `t_order_item` VALUES (4, 3, 3, 'iPhone 15 Pro Max', 9999.00, 1, 9999.00);

-- ----------------------------
-- Table structure for t_product
-- ----------------------------
DROP TABLE IF EXISTS `t_product`;
CREATE TABLE `t_product`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称（与订单明细 product_name 对齐）',
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '售价',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类别：手机/耳机/笔记本…',
  `brand` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品牌',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0 下架，1 上架',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常，1 已删',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_product
-- ----------------------------
INSERT INTO `t_product` VALUES (1, '华为Mate 60 Pro', 6999.00, '手机', '华为', '麒麟9000S芯片，支持卫星通话，6.82英寸OLED屏幕，5000mAh大电池', 1, 0, '2026-09-28 19:30:49', '2026-09-28 19:30:49');
INSERT INTO `t_product` VALUES (2, '华为FreeBuds Pro 3', 1499.00, '耳机', '华为', '智慧动态降噪3.0，高清音频传输，30小时超长续航', 1, 0, '2026-09-28 19:30:49', '2026-09-28 19:30:49');
INSERT INTO `t_product` VALUES (3, 'iPhone 15 Pro Max', 9999.00, '手机', '苹果', 'A17 Pro芯片，钛金属机身，4800万像素主摄，USB-C接口', 1, 0, '2026-09-28 19:30:49', '2026-09-28 19:30:49');
INSERT INTO `t_product` VALUES (4, '小米14 Ultra', 6499.00, '手机', '小米', '骁龙8 Gen3处理器，徕卡光学镜头，90W快充', 1, 0, '2026-09-28 19:30:49', '2026-09-28 19:30:49');
INSERT INTO `t_product` VALUES (5, '索尼WH-1000XM5', 2899.00, '耳机', '索尼', '行业领先降噪技术，30小时续航，轻量化设计', 1, 0, '2026-09-28 19:30:49', '2026-09-28 19:30:49');
INSERT INTO `t_product` VALUES (6, '联想ThinkPad X1 Carbon', 10999.00, '笔记本', '联想', '第13代Intel酷睿i7，16GB内存，1TB固态硬盘，商务办公首选', 1, 0, '2026-09-28 19:30:49', '2026-09-28 19:30:49');

-- ----------------------------
-- Table structure for t_session
-- ----------------------------
DROP TABLE IF EXISTS `t_session`;
CREATE TABLE `t_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话 UUID（= LangChain4j 的 @MemoryId）',
  `user_id` bigint NOT NULL COMMENT '所属用户 id',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '新会话' COMMENT '会话标题（首问截断）',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常，1 已删',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_session
-- ----------------------------

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（MD5 + 盐 mini_mall_2026）',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称（与订单表 username 对齐）',
  `role` tinyint NOT NULL DEFAULT 0 COMMENT '角色：0 普通用户，1 管理员',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0 禁用，1 启用',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常，1 已删',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES (1, 'admin', '7eca1b0a854dd5dc29995c68a9a4bb02', '系统管理员', 1, 1, 0, '2026-09-28 19:30:49', '2026-09-28 19:30:49');
INSERT INTO `t_user` VALUES (2, 'zhangsan', '61bd06a2dc8d29ab50f2b8726f65d4fb', '张三', 0, 1, 0, '2026-09-28 19:30:49', '2026-09-28 19:30:49');
INSERT INTO `t_user` VALUES (3, 'lisi', '61bd06a2dc8d29ab50f2b8726f65d4fb', '李四', 0, 1, 0, '2026-09-28 19:30:49', '2026-09-28 19:30:49');

SET FOREIGN_KEY_CHECKS = 1;
