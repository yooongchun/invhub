-- ----------------------------
-- 1. 创建数据库
-- ----------------------------
CREATE DATABASE IF NOT EXISTS apihub DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_general_ci;


-- ----------------------------
-- 2. 创建表 && 数据初始化
-- ----------------------------
use apihub;


-- ----------------------------
-- Table structure for apihub_user
-- ----------------------------
DROP TABLE IF EXISTS `apihub_user`;
CREATE TABLE `apihub_user`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `uid`         bigint UNSIGNED NOT NULL COMMENT '用户ID',
    `username`    varchar(64)     NOT NULL COMMENT '用户名',
    `admin`       tinyint(1)      NOT NULL DEFAULT 0 COMMENT '是否是管理员：0->否；1->是',
    `balance`     decimal(10, 2)  NOT NULL DEFAULT 0 COMMENT '账户余额',
    `password`    varchar(64)     NOT NULL COMMENT 'hash密码',
    `phone`       varchar(20)     NOT NULL DEFAULT '' COMMENT '手机号',
    `email`       varchar(100)    NOT NULL DEFAULT '' COMMENT '邮箱',
    `avatar`      varchar(500)    NOT NULL DEFAULT '' COMMENT '头像',
    `remark`      varchar(500)    NOT NULL DEFAULT '' COMMENT '备注信息',
    `deleted`     tinyint(1)      NOT NULL DEFAULT 0 COMMENT '软删除：0->正常；1->已删除',
    `last_msg_read_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最新消息阅读时间',
    `last_notify_read_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最新通知阅读时间',
    `create_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1  CHARACTER SET = utf8mb4 COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

INSERT INTO `apihub_user` VALUES (1, 1000,'admin', 1, 1.0, '$2a$10$.BTrIQ9NU8j6/ZQ8o.7dHeku8uon.vK3Y2BO1To7mfx4HtJCBnjce', 18217235290,'1729465178@qq.com', 'https://cravatar.cn/avatar/4dc14aeb1f51b357657b9a15da59dbd3?d=monsterid&s=200','超管', 0, NOW(),NOW(),NOW(), NOW());
INSERT INTO `apihub_user` VALUES (2, 1001, 'apihub', 0, 1.2, '$2a$10$.BTrIQ9NU8j6/ZQ8o.7dHeku8uon.vK3Y2BO1To7mfx4HtJCBnjce', '', 'yooongchun@qq.com', 'https://cravatar.cn/avatar/4dc14aeb1f51b357657b9a15da59dbd3?d=monsterid&s=200', '用户', 0, NOW(), NOW(),NOW(),NOW());


-- ----------------------------
-- Table structure for apihub_login_log
-- ----------------------------
DROP TABLE IF EXISTS `apihub_login_log`;
CREATE TABLE `apihub_login_log`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     bigint          NOT NULL COMMENT '用户ID',
    `ip`          varchar(255)    NOT NULL DEFAULT '' COMMENT '登陆IP',
    `address`     varchar(255)    NOT NULL DEFAULT '' COMMENT '登陆地址',
    `user_agent`  varchar(255)    NOT NULL DEFAULT '' COMMENT '浏览器信息',
    `create_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COMMENT = '用户登录日志';


-- ----------------------------
-- Table structure for apihub_transaction_record
-- ----------------------------
DROP TABLE IF EXISTS `apihub_transaction_record`;
CREATE TABLE `apihub_transaction_record`
(
    `id`                 bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`            bigint UNSIGNED NOT NULL COMMENT '用户ID',
    `transaction_id`     varchar(255)    NOT NULL COMMENT '交易ID',
    `transaction_amount` decimal(10, 2)  NOT NULL COMMENT '交易金额',
    `transaction_type`   varchar(255)    NOT NULL COMMENT '交易类型',
    `transaction_time`   datetime        NOT NULL COMMENT '交易时间',
    `transaction_status` varchar(255)    NOT NULL COMMENT '交易状态:SUCCEED-成功，FAILED-失败，CANCELED-取消',
    `remark`             varchar(255)    NOT NULL DEFAULT '' COMMENT '备注信息',
    `create_time`        datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COMMENT = '用户交易记录表' ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for apihub_alipay_order
-- ----------------------------
DROP TABLE IF EXISTS `apihub_alipay_order`;
CREATE TABLE `apihub_alipay_order`
(
    `id`               bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`          bigint UNSIGNED NOT NULL COMMENT '用户ID',
    `order_id`         varchar(64)     NOT NULL COMMENT '订单ID',
    `qr_code`          varchar(255)    NOT NULL DEFAULT '' COMMENT '二维码链接',
    `subject`          varchar(255)    NOT NULL DEFAULT '' COMMENT '订单标题/商品标题/交易标题',
    `amount`           decimal(10, 2)  NOT NULL DEFAULT 0.0 COMMENT '订单总金额',
    `trade_status`     varchar(255)    NOT NULL DEFAULT '' COMMENT '交易状态',
    `trade_no`         varchar(255)    NOT NULL DEFAULT ''COMMENT '支付宝交易号',
    `buyer_id`         varchar(255)    NOT NULL DEFAULT '' COMMENT '买家支付宝账号',
    `gmt_payment`      datetime        DEFAULT NULL COMMENT '交易付款时间',
    `buyer_pay_amount` decimal(10, 2)  NOT NULL DEFAULT 0.0 COMMENT '用户在交易中支付的金额',
    `create_time`      datetime        NOT NULL  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime        NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COMMENT = '订单表' ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for apihub_file_info
-- ----------------------------
DROP TABLE IF EXISTS `apihub_file_info`;
CREATE TABLE `apihub_file_info`
(
    `id`          bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     bigint UNSIGNED     NOT NULL COMMENT '用户ID',
    `file_name`   varchar(500)        NOT NULL DEFAULT '' COMMENT '文件名',
    `file_hash`   varchar(128)        NOT NULL DEFAULT '' COMMENT '文件hash',
    `file_path`   varchar(1000)       NOT NULL DEFAULT '' COMMENT '文件路径',
    `file_type`   varchar(64)         NOT NULL DEFAULT '' COMMENT '文件类型',
    `file_size`   bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '文件大小',
    `bucket_name` varchar(50)         NOT NULL DEFAULT '' COMMENT 'minio bucket name',
    `object_name` varchar(1000)       NOT NULL DEFAULT '' COMMENT 'minio object name',
    `deleted`     tinyint(1)          NOT NULL DEFAULT 0 COMMENT '是否删除:0-->否，1-->是',
    `remark`      varchar(255)        NOT NULL DEFAULT '' COMMENT '备注信息',
    `create_time` datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COMMENT = '文件信息表' ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for apihub_invoice_info
-- ----------------------------
DROP TABLE IF EXISTS `apihub_inv_info`;
CREATE TABLE `apihub_inv_info`
(
    `id`          bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     bigint UNSIGNED     NOT NULL COMMENT '用户ID',
    `file_id`     bigint UNSIGNED     NOT NULL COMMENT '文件ID',
    `inv_detail_id`  bigint                COMMENT '解析任务ID',
    `inv_check_id`  bigint                COMMENT '查验任务ID',
    `checked`     tinyint(1)          NOT NULL DEFAULT 0 COMMENT '是否已人工校验,0-->否，1-->是',
    `reimbursed`  tinyint(1)          NOT NULL DEFAULT 0 COMMENT '是否已报销：0->未报销；1->已报销；2->在途；3->已驳回',
    `inv_code`    varchar(32)         NOT NULL DEFAULT '' COMMENT '发票代码',
    `inv_num`     varchar(32)         NOT NULL DEFAULT '' COMMENT '发票号码',
    `check_code`     varchar(32)         NOT NULL DEFAULT '' COMMENT '校验码',
    `inv_date`    date                DEFAULT NULL COMMENT '开票日期',
    `amount`   decimal(10, 2)      NOT NULL DEFAULT 0.0 COMMENT '开具金额',
    `tax`     varchar(32)         NOT NULL DEFAULT '' COMMENT '税额',
    `inv_type`    varchar(32)         NOT NULL DEFAULT '' COMMENT '发票类型:增值税专用发票:->01、增值税电子专用发票->02、增值税普通发票->03、增值税电子普通发票->04、增值税普通发票(卷票)->05、增值税电子普通发票(通行费)->06，未知->99',

    `method`      varchar(32)    DEFAULT NULL COMMENT '途径：手动上传->manual，系统解析->auto',
    `deleted`     tinyint(1)     NOT NULL DEFAULT 0 COMMENT '是否删除:0-->否，1-->是',
    `remark`      varchar(255)   NOT NULL DEFAULT '' COMMENT '备注信息',
    `create_time` datetime       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COMMENT = '发票信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for apihub_inv_detail
-- ----------------------------
DROP TABLE IF EXISTS `apihub_inv_detail`;
CREATE TABLE `apihub_inv_detail`
(
    `id`          bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     bigint UNSIGNED     NOT NULL COMMENT '用户ID',
    `file_id`     bigint UNSIGNED     NOT NULL COMMENT '文件ID',
    `method`      varchar(32)    DEFAULT NULL COMMENT '途径：OCR->ocr，系统解析->auto',
    `status`    tinyint(1)     NOT NULL DEFAULT 0 COMMENT '解析状态:0-->初始化，1-->解析中，2-->成功，3-->失败',
    `deleted`     tinyint(1)     NOT NULL DEFAULT 0 COMMENT '是否删除:0-->否，1-->是',
    `remark`      varchar(255)   NOT NULL DEFAULT '' COMMENT '备注信息',
    `create_time` datetime       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    `service_type` varchar(32) NOT NULL DEFAULT '' COMMENT '发票消费类型。不同消费类型输出：餐饮、电器设备、通讯、服务、日用品食品、医疗、交通、其他',
    `invoice_code` varchar(32) NOT NULL DEFAULT '' COMMENT '发票代码',
    `invoice_num` varchar(32) NOT NULL DEFAULT '' COMMENT '发票号码',
    `invoice_type` varchar(32) NOT NULL DEFAULT '' COMMENT '发票种类,不同类型发票输出：普通发票、专用发票、电子普通发票、电子专用发票、通行费电子普票、区块链发票、通用机打电子发票、电子发票(专用发票)、电子发票(普通发票)',
    `invoice_type_org` varchar(32) NOT NULL DEFAULT '' COMMENT '发票名称',
    `invoice_code_confirm` varchar(32) NOT NULL DEFAULT '' COMMENT '发票代码的辅助校验码，一般业务情景可忽略',
    `invoice_num_confirm` varchar(32) NOT NULL DEFAULT '' COMMENT '发票号码的辅助校验码，一般业务情景可忽略',
    `invoice_num_digit` varchar(32) NOT NULL DEFAULT '' COMMENT '数电票号，仅针对纸质的全电发票，在密码区有数电票号码的字段输出',
    `invoice_tag` varchar(32) NOT NULL DEFAULT '' COMMENT '增值税发票左上角标志。 包含：通行费、销项负数、代开、收购、成品油、其他',
    `machine_num` varchar(32) NOT NULL DEFAULT '' COMMENT '机打号码。仅增值税卷票含有此参数',
    `machine_code` varchar(32) NOT NULL DEFAULT '' COMMENT '机器编号。仅增值税卷票含有此参数',
    `check_code` varchar(32) NOT NULL DEFAULT '' COMMENT '校验码',
    `invoice_date` varchar(32) NOT NULL DEFAULT '' COMMENT '开票日期',
    `purchaser_name` varchar(256) NOT NULL DEFAULT '' COMMENT '购方名称',
    `purchaser_register_num` varchar(64) NOT NULL DEFAULT '' COMMENT '购方纳税人识别号',
    `purchaser_address` varchar(256) NOT NULL DEFAULT '' COMMENT '购方地址及电话',
    `purchaser_bank` varchar(256) NOT NULL DEFAULT '' COMMENT '购方开户行及账号',
    `seller_name` varchar(256) NOT NULL DEFAULT '' COMMENT '销售方名称',
    `seller_register_num` varchar(64) NOT NULL DEFAULT '' COMMENT '销售方纳税人识别号',
    `seller_address` varchar(256) NOT NULL DEFAULT '' COMMENT '销售方地址及电话',
    `seller_bank` varchar(256) NOT NULL DEFAULT '' COMMENT '销售方开户行及账号',
    `password` varchar(1024) NOT NULL DEFAULT '' COMMENT '密码区',
    `province` varchar(32) NOT NULL DEFAULT '' COMMENT '省',
    `city` varchar(32) NOT NULL DEFAULT '' COMMENT '市',
    `sheet_num` varchar(256) NOT NULL DEFAULT '' COMMENT '联次信息。专票第一联到第三联分别输出：第一联：记账联、第二联：抵扣联、第三联：发票联；普通发票第一联到第二联分别输出：第一联：记账联、第二联：发票联',
    `agent` varchar(32) NOT NULL DEFAULT '' COMMENT '是否代开',
    `commodity` text COMMENT '货物栏，json.string之后存储',
    `total_amount` decimal NOT NULL DEFAULT 0.0 COMMENT '合计金额',
    `total_tax` decimal NOT NULL DEFAULT 0.0 COMMENT '合计税额',
    `amount_in_words` varchar(128) NOT NULL DEFAULT '' COMMENT '价税合计(大写)',
    `amount_in_figuers` decimal NOT NULL DEFAULT 0.0 COMMENT '价税合计(小写)',
    `payee` varchar(32) NOT NULL DEFAULT '' COMMENT '收款人',
    `checker` varchar(32) NOT NULL DEFAULT '' COMMENT '复核',
    `note_drawer` varchar(32) NOT NULL DEFAULT '' COMMENT '开票人',
    `remarks` varchar(1024) NOT NULL DEFAULT '' COMMENT '备注',
    `company_seal` varchar(32) NOT NULL DEFAULT '' COMMENT '判断是否存在印章。返回“0或1”，1代表存在印章，0代表不存在印章，当 seal_tag=true 时返回该字段',
    `seal_info` varchar(128) NOT NULL DEFAULT '' COMMENT '印章识别结果内容。当 seal_tag=true 时返回该字段',
    `extra` text COMMENT '额外信息，json.string之后存储',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COMMENT = '发票详情表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for apihub_inv_check_info
-- ----------------------------
DROP TABLE IF EXISTS `apihub_inv_check_info`;
CREATE TABLE `apihub_inv_check_info`
(
    `id`          bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     bigint UNSIGNED     NOT NULL COMMENT '用户ID',
    `file_id`     bigint UNSIGNED     NOT NULL COMMENT '文件ID',
    `method`      varchar(32)    DEFAULT NULL COMMENT '途径：OCR->ocr，系统解析->auto',
    `status`    tinyint(1)     NOT NULL DEFAULT 0 COMMENT '解析状态:0-->初始化，1-->解析中，2-->成功，3-->失败',
    `deleted`     tinyint(1)     NOT NULL DEFAULT 0 COMMENT '是否删除:0-->否，1-->是',
    `remark`      varchar(255)   NOT NULL DEFAULT '' COMMENT '备注信息',
    `create_time` datetime       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COMMENT = '发票详情表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for apihub_product_price
-- ----------------------------
DROP TABLE IF EXISTS `apihub_product_price`;
CREATE TABLE `apihub_product_price`
(
    `id`           bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_code` varchar(64)         NOT NULL DEFAULT '' COMMENT '产品编码',
    `product_name` varchar(255)        NOT NULL DEFAULT '' COMMENT '产品名称',
    `price`        decimal(10, 4)      NOT NULL DEFAULT 0.0 COMMENT '产品定价',
    `deleted`      tinyint(1)          NOT NULL DEFAULT 0 COMMENT '是否删除:0-->否，1-->是',
    `remark`       varchar(255)        NOT NULL DEFAULT '' COMMENT '备注信息',
    `create_time`  datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COMMENT = '产品定价表' ROW_FORMAT = DYNAMIC;

INSERT INTO `apihub_product_price` VALUES (1, 'INV_PARSE', '发票解析', 0.05, 0, '发票解析', NOW(), NOW());

-- ----------------------------
-- Table structure for apihub_message
-- ----------------------------
DROP TABLE IF EXISTS `apihub_message`;
CREATE TABLE `apihub_message`
(
    `id`           bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    bigint(20)         NOT NULL COMMENT '用户ID',
    `parent_id` bigint(20)         COMMENT '父级ID',
    `text` text    COMMENT '内容',
    `type`        tinyint(1)      NOT NULL DEFAULT 0 COMMENT '0->message 1->notify',
    `deleted`      tinyint(1)          NOT NULL DEFAULT 0 COMMENT '是否删除:0-->否，1-->是',
    `create_time`  datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COMMENT = '消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for apihub_inv_check_info
-- ----------------------------
DROP TABLE IF EXISTS `apihub_inv_check_task`;
CREATE TABLE `apihub_inv_check_task`
(
    `id`          bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     bigint UNSIGNED     NOT NULL COMMENT '用户ID',
    `inv_id`     bigint UNSIGNED     NOT NULL COMMENT 'Inv ID',
    `status`    tinyint(1)     NOT NULL DEFAULT 0 COMMENT '解析状态:0-->初始化，1-->查验中，2-->成功，3-->失败',
    `deleted`     tinyint(1)     NOT NULL DEFAULT 0 COMMENT '是否删除:0-->否，1-->是',
    `remark`      varchar(255)   NOT NULL DEFAULT '' COMMENT '备注信息',
    `create_time` datetime       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COMMENT = '发票查验任务表' ROW_FORMAT = DYNAMIC;
