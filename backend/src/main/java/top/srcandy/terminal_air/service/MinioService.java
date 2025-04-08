package top.srcandy.terminal_air.service;

import top.srcandy.terminal_air.pojo.vo.AvatarUploadVO;
import top.srcandy.terminal_air.constant.ResponseResult;

public interface MinioService {
    ResponseResult<AvatarUploadVO> generatePresignedUrl();

    String generateDisplaySignedUrl(String filePath);
}
