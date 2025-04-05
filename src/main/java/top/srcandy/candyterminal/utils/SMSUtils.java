package top.srcandy.candyterminal.utils;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.google.gson.Gson;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

public class SMSUtils {

    // 静态方法，发送短信
    public static SendSmsResponse sendSms(String phoneNumber, String code) throws Exception {
        // 配置认证信息（请确保环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID 和 ALIBABA_CLOUD_ACCESS_KEY_SECRET 已设置）
        StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                .accessKeyId(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID"))
                .accessKeySecret(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET"))
                .build());

        // 配置客户端
        AsyncClient client = AsyncClient.builder()
                .region("cn-qingdao") // 配置区域
                .credentialsProvider(provider)
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride("dysmsapi.aliyuncs.com")
                )
                .build();

        // 配置请求参数
        SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
                .signName("慕垂科技") // 短信签名
                .templateCode("SMS_476675621") // 短信模板编码
                .phoneNumbers(phoneNumber) // 手机号
                .templateParam("{\"code\":\"" + code + "\"}") // 动态短信模板参数
                .build();

        // 异步调用 API 获取返回结果
        CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);

        // 获取同步响应结果
        SendSmsResponse resp = response.get();
        String result = new Gson().toJson(resp); // 转换为 JSON 字符串返回

        // 关闭客户端
        client.close();

        return resp;
    }
}