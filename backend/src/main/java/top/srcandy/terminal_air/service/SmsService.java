package top.srcandy.terminal_air.service;

import top.srcandy.terminal_air.pojo.vo.SmsCodeVo;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.request.SendVerificationCodeRequest;

public interface SmsService {

    // Send SMS code by phone
    ResponseResult<SmsCodeVo> sendSmsCode(SendVerificationCodeRequest request) throws Exception;

    ResponseResult<SmsCodeVo> sendSmsCodeByToken() throws Exception;
    // Verify the SMS code
    boolean verifySmsCode(String phone, String serial ,String code);

}
