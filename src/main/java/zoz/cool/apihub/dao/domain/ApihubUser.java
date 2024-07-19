package zoz.cool.apihub.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户表
 * @TableName apihub_user
 */
@TableName(value ="apihub_user")
@Data
public class ApihubUser implements Serializable {
    /**
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 用户名
     */
    private String username;

    /**
     * 是否是管理员：0->否；1->是
     */
    private Integer admin;

    /**
     * 账户余额
     */
    private BigDecimal balance;

    /**
     * hash密码
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 软删除：0->正常；1->已删除
     */
    private Integer deleted;

    /**
     * 最新消息阅读时间
     */
    private LocalDateTime lastMsgReadTime;

    /**
     * 最新通知阅读时间
     */
    private LocalDateTime lastNotifyReadTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}