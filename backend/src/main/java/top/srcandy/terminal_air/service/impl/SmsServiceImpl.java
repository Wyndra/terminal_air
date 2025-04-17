package top.srcandy.terminal_air.service.impl;

import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.srcandy.terminal_air.pojo.vo.SmsCodeVO;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.enums.SMSChannel;
import top.srcandy.terminal_air.exception.ServiceException;
import top.srcandy.terminal_air.exception.TooManyRequestsException;
import top.srcandy.terminal_air.mapper.UserMapper;
import top.srcandy.terminal_air.pojo.model.User;
import top.srcandy.terminal_air.request.SendVerificationCodeRequest;
import top.srcandy.terminal_air.service.RedisService;
import top.srcandy.terminal_air.service.SmsService;
import top.srcandy.terminal_air.utils.SMSUtils;
import top.srcandy.terminal_air.utils.SecuritySessionUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {


    @Autowired
    private RedisService redisService;

    @Autowired
    private UserMapper userMapper;

    /**
     * Sends an SMS code based on the request details.
     *
     * @param request The SendVerificationCodeRequest containing the phone number and other details.
     * @return The response containing phone and serial number, or failure if the phone number is already registered.
     * @throws Exception if there are any issues during SMS sending.
     */
    @Override
    public ResponseResult<SmsCodeVO> sendSmsCode(SendVerificationCodeRequest request) throws Exception {

        String phone = request.getPhone();

        // Check if the phone number is already registered (for registration flow)
        // 1021 is the channel for registration
        if (Objects.equals(request.getChannel(), SMSChannel.REGISTER.getServiceCode())) {
            User existingUser = userMapper.selectByUserPhone(phone);
            Optional<User> userOptional = Optional.ofNullable(existingUser);
            if (userOptional.isPresent()) {
                // Return failure response if phone is already registered
                log.warn("频道:{},手机号:{} 请求验证码失败，手机号已注册", request.getChannel(), phone);
                return ResponseResult.fail(null, "该手机号已注册，请尝试登录");
            }
        }

        if (Objects.equals(request.getChannel(), SMSChannel.LOGIN.getServiceCode())) {
            User existingUser = userMapper.selectByUserPhone(phone);
            Optional<User> userOptional = Optional.ofNullable(existingUser);
            if (userOptional.isEmpty()) {
                // Return failure response if phone is already registered
                log.warn("频道:{},手机号:{} 请求验证码失败，手机号未注册", request.getChannel(), phone);
                return ResponseResult.fail(null, "该手机号未注册，请先注册");
            }
        }

        // Apply rate-limiting for requests within a 5-minute window
        String redisKey = "sms_request_count:" + phone;
        long requestCount = redisService.increment(redisKey, 1);
        if (requestCount == 1) {
            // Set expiry for the key when it is first created (5 minutes)
            redisService.expire(redisKey, 5, TimeUnit.MINUTES);
        }

        // If more than 3 requests are made in 5 minutes, throw an exception for too many requests
        if (requestCount > 3) {
            log.warn("{} 请求验证码过于频繁，请稍后再试 ", phone);
            throw new TooManyRequestsException("请求验证码过于频繁，请稍后再试 " + phone);
        }

        // Send the SMS verification code
        SmsCodeVO smsCodeVO = sendVerificationCode(phone);
        return ResponseResult.success(smsCodeVO); // Return success response with SMS code and serial number
    }

    /**
     * Sends an SMS code based on the token provided.
     *
     * @return The response containing phone and serial number, or failure if the phone number is not found.
     * @throws Exception if there are any issues during SMS sending.
     */
    @Override
    public ResponseResult<SmsCodeVO> sendSmsCodeByToken() throws Exception {
        String username = SecuritySessionUtils.getUsername();
        User user = SecuritySessionUtils.getUser();
        String phone = user.getPhone();

        // Apply rate-limiting for requests within a 5-minute window
        String redisKey = "sms_request_count:" + phone;
        long requestCount = redisService.increment(redisKey, 1);
        if (requestCount == 1) {
            // Set expiry for the key when it is first created (5 minutes)
            redisService.expire(redisKey, 5, TimeUnit.MINUTES);
        }

        // If more than 3 requests are made in 5 minutes, throw an exception for too many requests
        if (requestCount > 3) {
            log.warn("{},{} 请求验证码过于频繁，请稍后再试 ", phone, username);
            throw new TooManyRequestsException("请求验证码过于频繁，请稍后再试 " + phone + " " + username);
        }

        // Send the SMS verification code
        SmsCodeVO smsCodeVO = sendVerificationCode(phone);
        return ResponseResult.success(smsCodeVO); // Return success response with SMS code and serial number

    }

    /**
     * Generates a random verification code, stores it in Redis, and sends it via SMS.
     *
     * @param phone The phone number to send the verification code to.
     * @return The SmsCodeVO object containing the phone and serial number.
     * @throws Exception if the SMS sending fails.
     */
    private SmsCodeVO sendVerificationCode(String phone) throws Exception {
        // Generate a random 6-digit verification code
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // Generate a random 4-digit serial number for tracking
        String serial = String.valueOf((int) ((Math.random() * 9 + 1) * 1000));

        // Store the verification code and serial number in Redis with a 5-minute expiration
        String verifyCodeKey = "verify_code:" + phone;
        String verifyCodeValue = serial + ":" + code; // Store serial and code together

        redisService.set(verifyCodeKey, verifyCodeValue, 5, TimeUnit.MINUTES);
        // Call the SMSUtils.sendSms method to send the verification code via SMS
        try {
            SendSmsResponse smsResponse = SMSUtils.sendSms(phone, code);
            log.info("短信请求响应: " + smsResponse.getBody().getMessage());
            if (smsResponse.getBody().getCode().equals("isv.BUSINESS_LIMIT_CONTROL")) {
                // If the response contains "BUSINESS_LIMIT_CONTROL", it means the SMS sending limit has been reached
                log.warn("短信发送频率过快，请稍后再试");
                throw new ServiceException("短信发送失败，请稍后重试");
            }
        } catch (Exception e) {
            // Handle SMS sending failure
            log.error("短信发送失败: " + e.getMessage());
            throw new ServiceException("短信发送失败，请稍后重试");
        }

        // Return the response containing the phone number and serial number
        return new SmsCodeVO(phone, serial);
    }

    /**
     * Verify the SMS code entered by the user.
     *
     * @param phone  The phone number to check.
     * @param serial The serial number sent with the code.
     * @param code   The verification code entered by the user.
     * @return true if the code is correct and has not expired, false otherwise.
     */
    @Override
    public boolean verifySmsCode(String phone, String serial, String code) {
        // Get the stored verification code and serial number from Redis
        String redisKey = "verify_code:" + phone;
        String storedValue = redisService.get(redisKey);

        // Check if the verification code exists (it may have expired)
        if (storedValue == null) {
            // If there is no value, it means the code is either expired or has not been sent
            return false;
        }

        // Extract serial and code from the stored value (format: serial:code)
        String[] storedValueParts = storedValue.split(":");
        String storedSerial = storedValueParts[0];
        String storedCode = storedValueParts[1];

        // Validate the entered serial and code
        if (storedSerial.equals(serial) && storedCode.equals(code)) {
            redisService.delete(redisKey);
            return true;
        } else {
            return false;
        }
    }
}
