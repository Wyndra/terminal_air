package top.srcandy.terminal_air.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import top.srcandy.terminal_air.exception.ServiceException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class TwoFactorAuthUtil {

    public static final String MICROSOFT_AUTH_BASE64_IMAGE = "data:image/png;base64,";

    /**
     * 生成二维码内容
     * @param secretKey 共享的密钥
     * @param account 用户账号
     * @param issuer 服务提供者名称（可选）
     * @return 生成的二维码 URL
     */
    public static String getQrCodeText(String secretKey, String account, String issuer) {
        // 去除空格并转换为大写
        String normalizedBase32Key = secretKey.replace(" ", "").toUpperCase();

        // 生成基础 URL 部分
        StringBuilder urlBuilder = new StringBuilder("otpauth://totp/");

        // 构建 account 部分
        String accountPart = (StringUtils.isNotEmpty(issuer) ? issuer + ":" : "") + account;
        urlBuilder.append(URLEncoder.encode(accountPart, StandardCharsets.UTF_8));

        // 添加 secret 参数
        urlBuilder.append("?secret=").append(URLEncoder.encode(normalizedBase32Key, StandardCharsets.UTF_8));

        // 如果提供了 issuer，添加到 URL 中
        if (StringUtils.isNotEmpty(issuer)) {
            urlBuilder.append("&issuer=").append(URLEncoder.encode(issuer, StandardCharsets.UTF_8));
        }

        return urlBuilder.toString();
    }

    /**
     * 生成二维码
     * @param loginName 登录名
     * @param newSecretKey 新的密钥
     * @return 生成的二维码
     */
    public String getQrCode(String loginName, String newSecretKey) {
        String base64Image = null;
        try {
            // 生成二维码内容
            String qrCodeText = getQrCodeText(newSecretKey, loginName, "TerminalAir");
            int width = 300; // 图片宽度
            int height = 300; // 图片高度
            try {
                // 将URL转换为BitMatrix
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, width, height);
                // 将BitMatrix转换为BufferedImage
                BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
                // 保存二维码图片到本地文件
                //File file = new File("D:\\图片\\qrcode.png");
                //ImageIO.write(bufferedImage, format, file);
                //log.info("QR Code image generated successfully!");

                // 生成二维码图像
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                MatrixToImageWriter.writeToStream(bitMatrix, "png", outputStream);

                // 获取图像的字节数组，并使用 Base64 编码转换成字符串
                byte[] imageData = outputStream.toByteArray();
                base64Image = MICROSOFT_AUTH_BASE64_IMAGE + java.util.Base64.getEncoder().encodeToString(imageData);
                return base64Image;
            } catch (WriterException e) {
                throw new ServiceException("生成二维码图像失败");
            } catch (Exception e) {
                throw new ServiceException("Unexpected error:" + e.getMessage());
            }
        } catch (ServiceException e) {
            throw new ServiceException("生成二维码发生异常：" + e.getMessage());
        }
    }
}
