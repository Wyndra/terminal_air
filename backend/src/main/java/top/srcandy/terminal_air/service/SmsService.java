package top.srcandy.terminal_air.service;

import top.srcandy.terminal_air.pojo.vo.SmsCodeVO;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.request.SendVerificationCodeRequest;

public interface SmsService {

    // Send SMS code by phone
    ResponseResult<SmsCodeVO> sendSmsCode(SendVerificationCodeRequest request) throws Exception;

    ResponseResult<SmsCodeVO> sendSmsCodeByToken() throws Exception;
    // Verify the SMS code
    boolean verifySmsCode(String phone, String serial ,String code);

}
