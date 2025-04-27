package top.srcandy.terminal_air.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import top.srcandy.terminal_air.exception.ServiceException;
import top.srcandy.terminal_air.mapper.TwoFactorAuthMapper;
import top.srcandy.terminal_air.mapper.UserMapper;
import top.srcandy.terminal_air.pojo.model.User;
import top.srcandy.terminal_air.pojo.vo.EnableTwoFactorAuthVo;
import top.srcandy.terminal_air.request.EnableTwoFactorAuthCodeRequest;
import top.srcandy.terminal_air.request.VerifyOneTimeBackupCodeRequest;
import top.srcandy.terminal_air.request.VerifyTwoFactorAuthCodeRequest;
import top.srcandy.terminal_air.service.MultiFactorAuthenticationService;
import top.srcandy.terminal_air.utils.SecuritySessionUtils;
import top.srcandy.terminal_air.utils.TwoFactorAuthUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

@Service
@Slf4j
public class MultiFactorAuthenticationServiceImpl implements MultiFactorAuthenticationService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TwoFactorAuthMapper twoFactorAuthMapper;

    @Autowired
    private MicrosoftAuth microsoftAuth;

    @Override
    public String getTwoFactorAuthTitle() {
        Long userId = SecuritySessionUtils.getUserId();
        return twoFactorAuthMapper.getUserTwoFactorAuthTitle(userId);
    };

    @Override
    @Deprecated
    public Boolean switchTwoFactorAuth() {
        String username = SecuritySessionUtils.getUsername();
        User user = userMapper.selectByUserName(username);
        if (user.getIsTwoFactorAuth().equals("0")) {
            user.setIsTwoFactorAuth("1");
            log.info("用户 {} 开启了二次验证", username);
        } else {
            user.setIsTwoFactorAuth("0");
            log.info("用户 {} 关闭了二次验证", username);
        }
        userMapper.update(user);
        return user.getIsTwoFactorAuth().equals("1");
    }

    @Override
    public EnableTwoFactorAuthVo enableTwoFactorAuth(EnableTwoFactorAuthCodeRequest request) {
        Long userId = SecuritySessionUtils.getUserId();
        EnableTwoFactorAuthVo enableTwoFactorAuthVo = new EnableTwoFactorAuthVo();
        // 判断第一次输入的验证码是否正确
        if (microsoftAuth.checkCode(twoFactorAuthMapper.getUserTwoFactorAuthSecret(userId), request.getCode(), request.getTime())){
            twoFactorAuthMapper.updateTwoFactorAuthStatus(userId, "1");
            ArrayList<String> oneTimeCodeBackupList = generateOneTimeCodeBackupList();
            twoFactorAuthMapper.updateTwoFactorAuthTitleAndBackup(userId, request.getTitle(),combineCodeBackupList(oneTimeCodeBackupList));
            enableTwoFactorAuthVo.setStatus(true);
            enableTwoFactorAuthVo.setOneTimeCodeBackupList(oneTimeCodeBackupList);
        }else {
            log.warn("用户 {} 的二次验证失败", SecuritySessionUtils.getUsername());
            throw new ServiceException("验证失败");
        }
        return enableTwoFactorAuthVo;
    }

    @Override
    public void disableTwoFactorAuth() {
        twoFactorAuthMapper.deleteTwoFactorAuth(SecuritySessionUtils.getUserId());
    };

    @Override
    public String initTwoFactorAuth() {
        Long userId = SecuritySessionUtils.getUserId();
        // 所有用户在第一次使用时都需要初始化二次验证
        log.info("用户 {} 初始化二次验证", SecuritySessionUtils.getUsername());
        if (twoFactorAuthMapper.countTwoFactorAuthSecret(userId) > 0) {
            log.debug("用户 {} 已经初始化过二次验证", SecuritySessionUtils.getUsername());
            return "用户已初始化过二次验证";
        }
        twoFactorAuthMapper.initTwoFactorAuthSecret(userId,microsoftAuth.getSecretKey());
        return "初始化成功";
    }

    @Override
    public String getTwoFactorAuthSecretQRCode() {
        Long userId = SecuritySessionUtils.getUserId();
        if (twoFactorAuthMapper.countTwoFactorAuthSecret(userId) == 0) {
            throw new ServiceException("请先初始化二次验证");
        }
        String secret = twoFactorAuthMapper.getUserTwoFactorAuthSecret(userId);
        return new TwoFactorAuthUtil().getQrCode(SecuritySessionUtils.getUsername(), secret);
    }

    @Override
    public Boolean getCurrentTwoFactorAuthStatus() {
        Long userId = SecuritySessionUtils.getUserId();
        if (twoFactorAuthMapper.countTwoFactorAuthSecret(userId) == 0) {
            throw new ServiceException("请先初始化二次验证");
        }
        return twoFactorAuthMapper.getUserTwoFactorAuthStatus(userId).equals("1");
    }

    @Override
    public ResponseEntity<Resource> downloadOneTimeCodeBackup() {
        Long userId = SecuritySessionUtils.getUserId();
        String oneTimeCodeBackup = twoFactorAuthMapper.getUserOneTimeCodeBackup(userId);

        if (oneTimeCodeBackup == null || oneTimeCodeBackup.isEmpty()) {
            throw new ServiceException("暂无一次性备份码");
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z", Locale.US);

        oneTimeCodeBackup = oneTimeCodeBackup.replace(":0","");
        String result = "Terminal Air 备份代码\n" +
                "创建于" + zonedDateTime.format(formatter) + "\n\n" +
                oneTimeCodeBackup.replace(",", "\n") + "\n\n" +
                "请将这些代码存储在安全位置，并在使用时标记代码。\n";

        // 转成字节流
        byte[] fileBytes = result.getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(fileBytes);

        // 处理文件名（带空格要URL编码）
        String filename = "TerminalAir Backup Code.txt";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE + "; charset=UTF-8");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(fileBytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @Override
    public boolean verifyTwoFactorAuthCode(VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        String secret = twoFactorAuthMapper.getUserTwoFactorAuthSecret(SecuritySessionUtils.getUserId());
        return microsoftAuth.checkCode(secret, request.getCode(), request.getTime());
    }

    @Override
    public boolean verifyOneTimeBackupCode(VerifyOneTimeBackupCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        String oneTimeCodeBackup = twoFactorAuthMapper.getUserOneTimeCodeBackup(SecuritySessionUtils.getUserId());
        if (oneTimeCodeBackup == null || oneTimeCodeBackup.isEmpty()) {
            throw new ServiceException("暂无一次性备份码");
        }
        // 先将备份码转成数组
        String[] codes = oneTimeCodeBackup.split(",");
        for (int i = 0;i < codes.length;i++){
            String[] code = codes[i].split(":");
            if (code[0].equals(request.getCode()) && code[1].equals("0")){
                // 如果备份码正确，则将其置为已使用
                codes[i] = code[0] + ":1";
                StringBuilder sb = new StringBuilder();
                for (String s : codes) {
                    sb.append(s).append(",");
                }
                oneTimeCodeBackup = sb.substring(0, sb.length() - 1);
                twoFactorAuthMapper.updateTwoFactorAuthOneTimeCodeBackup(SecuritySessionUtils.getUserId(), oneTimeCodeBackup);
                return true;
            }
        }
        // 返回false说明 备份码错误 或者 已使用
        return false;
    }

    private static final char[] CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public ArrayList<String> generateOneTimeCodeBackupList() {
        ArrayList<String> result = new ArrayList<>();
        int BACKUP_CODE_COUNT = 6;
        for (int i = 0; i < BACKUP_CODE_COUNT; i++) {
            StringBuilder code = new StringBuilder();
            for (int j = 0; j < 9; j++) {
                if (j == 4) {
                    code.append('-');
                } else {
                    code.append(CHAR_POOL[RANDOM.nextInt(CHAR_POOL.length)]);
                }
            }
            result.add(code.toString());
        }
        return result;
    }

    @Override
    public ArrayList<String> refreshOneTimeCodeBackupList() throws GeneralSecurityException, UnsupportedEncodingException {
        // 要做限制
        Long userId = SecuritySessionUtils.getUserId();
        String currentOneTimeCodeBackup = twoFactorAuthMapper.getUserOneTimeCodeBackup(userId);
        if (currentOneTimeCodeBackup == null || currentOneTimeCodeBackup.isEmpty()) {
            throw new ServiceException("暂无一次性备份码");
        }

        String[] currentCodes = currentOneTimeCodeBackup.split(",");
        int usedCount = 0;
        for (String currentCode : currentCodes) {
            String[] code = currentCode.split(":");
            usedCount += Integer.parseInt(code[1]);
        }
        if (usedCount <= 3) {
            throw new ServiceException("请至少使用3个备份码后再刷新");
        }
        ArrayList<String> oneTimeCodeBackupList = generateOneTimeCodeBackupList();
        String oneTimeCodeBackup = combineCodeBackupList(oneTimeCodeBackupList);
        twoFactorAuthMapper.updateTwoFactorAuthOneTimeCodeBackup(SecuritySessionUtils.getUserId(), oneTimeCodeBackup);
        return oneTimeCodeBackupList;
    }

    public String combineCodeBackupList(ArrayList<String> codeBackupList) {
        StringBuilder result = new StringBuilder();
        if (codeBackupList == null || codeBackupList.isEmpty()) {
            return "";
        }
        for (int i = 0; i < codeBackupList.size(); i++) {
            String code = codeBackupList.get(i);
            result.append(code);
            result.append(":0");
            if (i < codeBackupList.size() - 1) {
                result.append(",");
            }
        }

        return result.toString();
    }
}
