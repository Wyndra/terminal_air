package top.srcandy.terminal_air.pojo.vo;

import lombok.Data;

@Data
public class SmsCodeVO {
    private String phone;
    private String serial;

    public SmsCodeVO(String phone, String serial) {
        this.phone = phone;
        this.serial = serial;
    }
}
