package top.srcandy.candyterminal.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CredentialConnectionRequest {
    @Schema(description = "凭据UUID")
    @NotEmpty
    private String uuid;

    @Schema(description = "连接ID")
    @NotEmpty
    private Long connectId;
}
