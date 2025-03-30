package top.srcandy.candyterminal.request;

import lombok.Data;

@Data
public class GenerateInstallShellRequest {
    private String token;
    private Long id;
}
