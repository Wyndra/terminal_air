package top.srcandy.candyterminal.request;

import lombok.Data;

@Data
public class GenerateKeyPairRequest {
    private String name;
    private String tags;
}
