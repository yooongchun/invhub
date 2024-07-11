package zoz.cool.apihub.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 发票详情表
 * @TableName apihub_inv_check_info
 */
@TableName(value ="apihub_inv_check_info")
@Data
public class ApihubInvCheckInfo implements Serializable {
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
     * 文件ID
     */
    private Long fileId;

    /**
     * 途径：OCR->ocr，系统解析->auto
     */
    private String method;

    /**
     * 解析状态:0-->初始化，1-->解析中，2-->成功，3-->失败
     */
    private Integer status;

    /**
     * 是否删除:0-->否，1-->是
     */
    private Integer deleted;

    /**
     * 备注信息
     */
    private String remark;

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