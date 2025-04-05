package top.srcandy.terminal_air.service;

import top.srcandy.terminal_air.bean.vo.AvatarUploadVO;
import top.srcandy.terminal_air.constant.ResponseResult;

public interface MinioService {
    ResponseResult<AvatarUploadVO> generatePresignedUrl(String extra);

    String generateDisplaySignedUrl(String filePath);
}
