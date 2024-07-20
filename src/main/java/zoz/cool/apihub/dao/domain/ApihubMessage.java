package zoz.cool.apihub.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 消息表
 * @TableName apihub_message
 */
@TableName(value ="apihub_message")
@Data
public class ApihubMessage implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 父级ID
     */
    private Long parentId;

    /**
     * 内容
     */
    private String text;

    /**
     * 0->message 1->feedback
     */
    private Integer type;

    /**
     * 是否删除:0-->否，1-->是
     */
    private Integer deleted;

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