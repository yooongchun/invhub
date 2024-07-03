package zoz.cool.apihub.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import zoz.cool.apihub.dao.domain.ApihubFileInfo;
import zoz.cool.apihub.dao.service.ApihubFileInfoService;
import zoz.cool.apihub.dao.mapper.ApihubFileInfoMapper;
import org.springframework.stereotype.Service;

/**
 * @author zhayongchun
 * @description 针对表【apihub_file_info(文件信息表)】的数据库操作Service实现
 * @createDate 2024-07-03 16:26:39
 */
@Service
public class ApihubFileInfoServiceImpl extends ServiceImpl<ApihubFileInfoMapper, ApihubFileInfo>
        implements ApihubFileInfoService {

    public ApihubFileInfo getByFileHash(String fileHash) {
        return getOne(new QueryWrapper<ApihubFileInfo>().eq("file_hash", fileHash));
    }
}




