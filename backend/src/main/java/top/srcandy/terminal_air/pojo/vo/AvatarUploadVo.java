package top.srcandy.terminal_air.pojo.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AvatarUploadVo {
    public String url;
    public String fileName;
    public String filePath;
}
