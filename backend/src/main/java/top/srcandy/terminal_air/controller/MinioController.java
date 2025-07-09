package top.srcandy.terminal_air.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.srcandy.terminal_air.pojo.vo.AvatarUploadVo;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.service.MinioService;

@Slf4j
@RestController
@RequestMapping("/api/avatar")
@Tag(name = "文件接口", description = "文件接口")
public class MinioController {

    @Autowired
    private MinioService minioService;

    @GetMapping ("/presigned-url")
    @Operation(summary = "获取Minio预签名URL")
    public ResponseResult<AvatarUploadVo> getPresignedUrl() {
        return minioService.generatePresignedUrl();
    }

}
