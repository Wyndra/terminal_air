package top.srcandy.terminal_air.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class EnableTwoFactorAuthVo {
    private Boolean status;
    private List<String> oneTimeCodeBackupList;
}
