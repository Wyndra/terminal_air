package top.srcandy.terminal_air.service.impl;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.srcandy.terminal_air.pojo.vo.AvatarUploadVO;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.exception.ServiceException;
import top.srcandy.terminal_air.service.MinioService;
import top.srcandy.terminal_air.utils.SecuritySessionUtils;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@Slf4j
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    public MinioServiceImpl(@Value("${minio.endpoint}") String endpoint,
                        @Value("${minio.accessKey}") String accessKey,
                        @Value("${minio.secretKey}") String secretKey) throws NoSuchAlgorithmException, KeyManagementException {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        // 临时解决方案
        this.minioClient.ignoreCertCheck();
    }

    @Override
    public ResponseResult<AvatarUploadVO> generatePresignedUrl() {
        String extra = SecuritySessionUtils.getUsername();
        try {
            String randomString = UUID.randomUUID().toString().replace("-", "");
            String filePath = "/avatar/%s-%s.png".formatted(randomString, extra);
            String fileName = "%s-%s.png".formatted(randomString, extra);
            minioClient.ignoreCertCheck();
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(filePath.substring(1))
                            .expiry(60 * 10)
                            .build()
            );
            log.info("{} 创建了头像上传的预签名连接", extra);
            return ResponseResult.success(AvatarUploadVO.builder().fileName(fileName).filePath(filePath).url(url).build());
        } catch (Exception e) {
            throw new ServiceException("预签名生成失败");
        }
    }

    @Override
    public String generateDisplaySignedUrl(String filePath) {
        try {
            minioClient.ignoreCertCheck();
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(filePath.substring(1))
                            .expiry(60 * 60 * 24)
                            .build()
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException("预签名生成失败");
        }
    }
}
