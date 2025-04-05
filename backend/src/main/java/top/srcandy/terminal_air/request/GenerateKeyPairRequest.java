package top.srcandy.terminal_air.request;

import lombok.Data;

@Data
public class GenerateKeyPairRequest {
    private String name;
    private String tags;
}
