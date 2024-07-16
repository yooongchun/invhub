package zoz.cool.apihub.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogsVo {
    @Schema(description = "操作人")
    private String operator;
    @Schema(description = "IP地址")
    private String ip;
    @Schema(description = "地址")
    private String address;
    @Schema(description = "浏览器信息")
    private String userAgent;
    @Schema(description = "操作系统")
    private String os;
    @Schema(description = "日志内容")
    private String log;
    @Schema(description = "模块")
    private String module;
    @Schema(description = "操作时间")
    private LocalDateTime createTime;
}
