DROP TABLE IF EXISTS `apihub_user`;
CREATE TABLE `apihub_user`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `uid`         bigint UNSIGNED NOT NULL COMMENT '用户ID',
    `username`    varchar(64)     NOT NULL COMMENT '用户名',
    `admin`               tinyint(1)      NOT NULL DEFAULT 0 COMMENT '是否是管理员',
    `balance` decimal(10, 2) NOT NULL DEFAULT 0 COMMENT '账户余额',
    `password`    varchar(64)     NOT NULL COMMENT 'hash密码',
    `phone`       varchar(20)     NOT NULL DEFAULT '' COMMENT '手机号',
    `email`       varchar(100)    NOT NULL DEFAULT '' COMMENT '邮箱',
    `icon`        varchar(500)    NOT NULL DEFAULT '' COMMENT '头像',
    `remark`        varchar(500)    NOT NULL DEFAULT '' COMMENT '备注信息',
    `deleted`      tinyint(1)      NOT NULL DEFAULT 0 COMMENT '软删除：0->正常；1->已删除',
    `create_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4 COMMENT = '用户表'
  ROW_FORMAT = DYNAMIC;

INSERT INTO `apihub_user`
VALUES (1, 1000,'admin', 1, 1.0, '$2a$10$.BTrIQ9NU8j6/ZQ8o.7dHeku8uon.vK3Y2BO1To7mfx4HtJCBnjce', 18217235290,
        '1729465178@qq.com', 'https://cravatar.cn/avatar/4dc14aeb1f51b357657b9a15da59dbd3?d=monsterid&s=200',
        '超管', 0, NOW(), NOW());
INSERT INTO `apihub_user`
VALUES (2, 1001, 'apihub', 0, 1.2, '$2a$10$.BTrIQ9NU8j6/ZQ8o.7dHeku8uon.vK3Y2BO1To7mfx4HtJCBnjce', '', 'yooongchun@qq.com',
        'https://cravatar.cn/avatar/4dc14aeb1f51b357657b9a15da59dbd3?d=monsterid&s=200', '用户', 0, NOW(), NOW());

DROP TABLE IF EXISTS `apihub_login_log`;
CREATE TABLE `apihub_login_log`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    bigint          NOT NULL COMMENT '用户ID',
    `ip`          varchar(255)    NOT NULL DEFAULT '' COMMENT '登陆IP',
    `address`     varchar(255)   NOT  NULL DEFAULT '' COMMENT '登陆地址',
    `user_agent`  varchar(255)   NOT NULL DEFAULT '' COMMENT '浏览器信息',
    `create_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4 COMMENT = '用户登录日志';

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
    `remark`             varchar(255)  NOT NULL DEFAULT '' COMMENT '备注信息',
    `create_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4 COMMENT = '用户交易记录表'
  ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `apihub_alipay_order`;
CREATE TABLE `apihub_alipay_order`
(
    `id`               bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`          bigint UNSIGNED NOT NULL COMMENT '用户ID',
    `order_id`         varchar(64)     NOT NULL COMMENT '订单ID',
    `qr_code`          varchar(255)   NOT NULL DEFAULT '' COMMENT '二维码链接',
    `subject`          varchar(255)   NOT NULL DEFAULT '' COMMENT '订单标题/商品标题/交易标题',
    `amount`     decimal(10, 2) NOT NULL DEFAULT 0.0 COMMENT '订单总金额',
    `trade_status`     varchar(255)   NOT NULL DEFAULT '' COMMENT '交易状态',
    `trade_no`         varchar(255)   NOT NULL DEFAULT ''COMMENT '支付宝交易号',
    `buyer_id`         varchar(255)   NOT NULL DEFAULT '' COMMENT '买家支付宝账号',
    `gmt_payment` datetime    DEFAULT NULL COMMENT '交易付款时间',
    `buyer_pay_amount` decimal(10, 2) NOT NULL DEFAULT 0.0 COMMENT '用户在交易中支付的金额',
    `create_time`    datetime     NOT NULL  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime     NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4 COMMENT = '订单表'
  ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `apihub_file_info`;
CREATE TABLE `apihub_file_info`
(
    `id`          bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     bigint UNSIGNED     NOT NULL COMMENT '用户ID',
    `file_name`   varchar(500)   NOT NULL DEFAULT '' COMMENT '文件名',
    `file_hash`   varchar(128)   NOT NULL DEFAULT '' COMMENT '文件hash',
    `file_path`   varchar(1000)  NOT NULL DEFAULT '' COMMENT '文件路径',
    `file_type`   varchar(64)    NOT NULL DEFAULT '' COMMENT '文件类型',
    `file_size`    bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '文件大小',
    `bucket_name` varchar(50)    NOT NULL DEFAULT '' COMMENT 'minio bucket name',
    `object_name` varchar(1000)  NOT NULL DEFAULT '' COMMENT 'minio object name',
    `deleted`   tinyint(1)     NOT NULL DEFAULT 0 COMMENT '是否删除:0-->否，1-->是',
    `remark`      varchar(255)   DEFAULT NULL COMMENT '备注信息',
    `create_time` datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4 COMMENT = '文件信息表'
  ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `apihub_invoice_info`;
CREATE TABLE `apihub_invoice_info`
(
    `id`          bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     bigint UNSIGNED     NOT NULL COMMENT '用户ID',
    `file_id`     bigint UNSIGNED     NOT NULL COMMENT '文件ID',
    `status`      tinyint(1)     NOT NULL DEFAULT 0 COMMENT '解析状态,0-->初始化，1-->处理中，2-->成功，-1-->失败',
    `checked`     tinyint(1)     NOT NULL DEFAULT 0 COMMENT '是否已人工校验,0-->否，1-->是',
    `reimbursed`  tinyint(1)     NOT NULL DEFAULT 0 COMMENT '是否已报销：0->未报销；1->已报销',

    `inv_code`    varchar(32)    NOT NULL DEFAULT '' COMMENT '发票代码',
    `inv_num`     varchar(32)    NOT NULL DEFAULT '' COMMENT '发票号码',
    `inv_chk`     varchar(32)    NOT NULL DEFAULT '' COMMENT '校验码',
    `inv_date`    date            DEFAULT NULL COMMENT '开票日期',
    `inv_money`   decimal(10, 2) NOT NULL DEFAULT 0.0 COMMENT '开具金额',
    `inv_tax`     varchar(32)    NOT NULL DEFAULT '' COMMENT '税额',
    `inv_total`   varchar(32)   NOT NULL  DEFAULT '' COMMENT '价税合计',
    `inv_type`    varchar(64)    NOT NULL DEFAULT '' COMMENT '发票类型:增值税专用发票、增值税电子专用发票、增值税普通发票、增值税电子普通发票、增值税普通发票(卷票)、增值税电子普通发票(通行费)',
    `inv_detail`  text            COMMENT '详细信息',

    `method`      varchar(32)    DEFAULT NULL COMMENT '解析方式',
    `deleted`   tinyint(1)     NOT NULL DEFAULT 0 COMMENT '是否删除:0-->否，1-->是',
    `remark`      varchar(255)   NOT NULL DEFAULT '' COMMENT '备注信息',
    `create_time` datetime       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4 COMMENT = '发票信息表'
  ROW_FORMAT = DYNAMIC;


DROP TABLE IF EXISTS `apihub_product_price`;
CREATE TABLE `apihub_product_price`
(
    `id`          bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_code` varchar(64) NOT NULL DEFAULT '' COMMENT '产品编码',
    `product_name` varchar(255) NOT NULL DEFAULT '' COMMENT '产品名称',
    `price` decimal(10, 4) NOT NULL DEFAULT 0.0 COMMENT '产品定价',

    `deleted`   tinyint(1)     NOT NULL DEFAULT 0 COMMENT '是否删除:0-->否，1-->是',
    `remark`      varchar(255)   NOT NULL DEFAULT '' COMMENT '备注信息',
    `create_time` datetime       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4 COMMENT = '产品定价表'
  ROW_FORMAT = DYNAMIC;
