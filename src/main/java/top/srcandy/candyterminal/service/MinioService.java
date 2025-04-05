package top.srcandy.candyterminal.service;

import top.srcandy.candyterminal.bean.vo.AvatarUploadVO;
import top.srcandy.candyterminal.constant.ResponseResult;

public interface MinioService {
    ResponseResult<AvatarUploadVO> generatePresignedUrl(String extra);

    String generateDisplaySignedUrl(String filePath);
}
