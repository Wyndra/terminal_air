package top.srcandy.candyterminal.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AddConnectionRequest {
    @Schema(description = "连接主机")
    private String host;
    @Schema(description = "连接端口")
    private String port;
    @Schema(description = "连接用户名")
    private String username;
    @Schema(description = "连接密码")
    private String password;
    @Schema(description = "连接名称")
    @Max(value = 10, message = "连接名称长度不能超过10个字符")
    @Min(value = 1, message = "连接名称长度不能小于1个字符")
    private String name;
    @Schema(description = "连接方式")
    private String method;
}
