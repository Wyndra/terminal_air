package top.srcandy.candyterminal.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.srcandy.candyterminal.bean.vo.AvatarUploadVO;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.service.MinioService;
import top.srcandy.candyterminal.utils.JWTUtil;

@Slf4j
@RestController
@RequestMapping("/avatar")
public class MinioController {

    @Autowired
    private MinioService minioService;

    @PostMapping ("/presigned-url")
    public ResponseResult<AvatarUploadVO> getPresignedUrl(@RequestHeader("Authorization") String token) {
        String extra = JWTUtil.getTokenClaimMap(token.substring(7)).get("username").asString();
        return minioService.generatePresignedUrl(extra);
    }
}
