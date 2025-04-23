package top.srcandy.terminal_air.service;

import top.srcandy.terminal_air.pojo.vo.AvatarUploadVo;
import top.srcandy.terminal_air.constant.ResponseResult;

public interface MinioService {
    ResponseResult<AvatarUploadVo> generatePresignedUrl();

    String generateDisplaySignedUrl(String filePath);
}
