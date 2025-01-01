package top.srcandy.candyterminal.service;

import top.srcandy.candyterminal.bean.vo.SmsCodeVO;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.request.SendVerificationCodeRequest;

public interface SmsService {

    // Send SMS code by phone
    ResponseResult<SmsCodeVO> sendSmsCode(SendVerificationCodeRequest request) throws Exception;


    // Verify the SMS code
    boolean verifySmsCode(String phone, String serial ,String code);

}
