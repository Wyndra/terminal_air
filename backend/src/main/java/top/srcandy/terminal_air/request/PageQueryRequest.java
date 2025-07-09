package top.srcandy.terminal_air.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PageQueryRequest {
    @Schema(description = "页码", example = "1")
    private int current;
    @Schema(description = "每页数量", example = "10")
    private int pageSize;
//    @Schema(description = "排序字段", example = "createTime")
//    private String sort;
//    @Schema(description = "排序方式", example = "desc")
//    private String order;
}
