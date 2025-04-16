package top.srcandy.terminal_air.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CredentialStatusShortTokenRequest {
    @Schema(description = "短令牌凭据")
    @NotEmpty
    private String short_token;
    @Schema(description = "凭据UUID")
    @NotEmpty
    private String uuid;

    @Schema(description = "凭据状态")
    @NotEmpty
    private Integer status;
}
