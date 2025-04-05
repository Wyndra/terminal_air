package top.srcandy.terminal_air.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PageQueryRequest {
    @Schema(description = "页码", example = "1")
    private int page;
    @Schema(description = "每页数量", example = "10")
    private int pageSize;
}
