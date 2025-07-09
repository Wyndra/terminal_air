package top.srcandy.terminal_air.utils;

import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.apistd.uni.Uni;
import com.apistd.uni.UniException;
import com.apistd.uni.UniResponse;
import com.apistd.uni.sms.UniMessage;
import com.apistd.uni.sms.UniSMS;

import java.util.HashMap;
import java.util.Map;

public class UniSMSUtils {
    public static UniResponse sendSms(String phoneNumber, String code) throws Exception {

        // RqM1g8nPg3PZP7AzErTNqnUr89gjoRha7Xz5BcaVd4buwqcvQ
        // 28Wc8hz2FVuzsukcUX1t8MUWT6RKf5i
        Uni.init(System.getenv("UNISMS_ACCESS_KEY_ID"), System.getenv("UNISMS_ACCESS_KEY_SECRET")); // 若使用简易验签模式仅传入第一个参数即可

        // 设置自定义参数 (变量短信)
        Map<String, String> templateData = new HashMap<String, String>();
        templateData.put("code", code);
        templateData.put("ttl","5");

        // 构建信息
        UniMessage message = UniSMS.buildMessage()
                .setTo(phoneNumber)
                .setSignature("UniSMS")
                .setTemplateId("pub_verif_ttl3")
                .setTemplateData(templateData);
        return message.send();
    }
}
