package top.srcandy.terminal_air.pojo.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AvatarUploadVO {
    public String url;
    public String fileName;
    public String filePath;
}
