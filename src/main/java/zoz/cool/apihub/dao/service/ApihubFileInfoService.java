package zoz.cool.apihub.dao.service;

import zoz.cool.apihub.dao.domain.ApihubFileInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author zhayongchun
* @description 针对表【apihub_file_info(文件信息表)】的数据库操作Service
* @createDate 2024-07-03 16:26:39
*/
public interface ApihubFileInfoService extends IService<ApihubFileInfo> {
    ApihubFileInfo getByFileHash(String fileHash);
}
