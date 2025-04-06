package top.srcandy.terminal_air.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DeleteConnectRequest {
    @Schema(description = "连接id")
    private String uuid;
}
