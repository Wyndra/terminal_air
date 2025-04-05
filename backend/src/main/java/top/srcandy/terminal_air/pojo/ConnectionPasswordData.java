package top.srcandy.terminal_air.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.srcandy.terminal_air.enums.ExpireTime;

import java.sql.Timestamp;

@Data
public class ConnectionPasswordData {
    @Schema(description = "密码")
    private String password;
    @Schema(description = "签发时间")
    private Timestamp issueTime;
    @Schema(description = "过期时间")
    private ExpireTime expireTime;
    @Schema(description = "是否永不过期")
    private boolean neverExpire;
}
