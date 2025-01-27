package top.srcandy.candyterminal.request;

import lombok.Data;

@Data
public class GenerateKeyPairRequest {
    private Long connectId;

    private String name;

    private String passphrase;
}
