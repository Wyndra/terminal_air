package top.srcandy.terminal_air.service;

import top.srcandy.terminal_air.bean.vo.SmsCodeVO;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.request.SendVerificationCodeRequest;

public interface SmsService {

    // Send SMS code by phone
    ResponseResult<SmsCodeVO> sendSmsCode(SendVerificationCodeRequest request) throws Exception;

    ResponseResult<SmsCodeVO> sendSmsCodeByToken(String token_no_bearer) throws Exception;
    // Verify the SMS code
    boolean verifySmsCode(String phone, String serial ,String code);

}
