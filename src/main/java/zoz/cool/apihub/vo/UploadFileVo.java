package zoz.cool.apihub.vo;

import lombok.Data;

@Data
public class UploadFileVo {
    private String fileHash;
    private String fileName;
    private Long fileSize;
    private String readableSize;
    private String savePath;
}
