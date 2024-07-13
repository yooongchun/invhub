package zoz.cool.apihub.vo;

import lombok.Data;

@Data
public class FilePreviewVo {
    private String fileType;
    private String url;

    public FilePreviewVo() {
    }

    public FilePreviewVo(String fileType, String url) {
        this.fileType = fileType;
        this.url = url;
    }
}
