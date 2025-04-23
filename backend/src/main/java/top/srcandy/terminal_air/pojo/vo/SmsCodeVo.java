package top.srcandy.terminal_air.pojo.vo;

import lombok.Data;

@Data
public class SmsCodeVo {
    private String phone;
    private String serial;

    public SmsCodeVo(String phone, String serial) {
        this.phone = phone;
        this.serial = serial;
    }
}
